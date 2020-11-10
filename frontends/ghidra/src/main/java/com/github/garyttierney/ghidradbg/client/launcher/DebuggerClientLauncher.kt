package com.github.garyttierney.ghidradbg.client.launcher

import com.github.garyttierney.ghidradbg.client.DebuggerClient
import com.github.garyttierney.ghidradbg.client.DebuggerEventListener
import com.github.garyttierney.ghidradbg.client.message.DebuggerCommandRequest
import com.github.garyttierney.ghidradbg.client.message.DebuggerNotification
import com.github.garyttierney.ghidradbg.plugin.DebuggerConnection
import ghidra.framework.Application
import ghidra.framework.OperatingSystem
import ghidra.framework.Platform
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.Channel.Factory.BUFFERED
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.nio.file.Files
import java.util.concurrent.Executors

object DebuggerClientLauncher {
    private const val EXE_NAME = "ghidra-dbg"

    fun launch(connection: DebuggerConnection, listener: DebuggerEventListener): DebuggerClient {
        val executablePath = find(EXE_NAME)

        val processBuilder = ProcessBuilder(executablePath.toString(), connection.type.id, connection.options)
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)

        // One thread per IO stream (stdin, stderr, stdout) read in a blocking loop.
        val executor = Executors.newFixedThreadPool(4)
        val scope = CoroutineScope(executor.asCoroutineDispatcher() + SupervisorJob())

        val logs = Channel<String>(BUFFERED)
        val notifications = Channel<DebuggerNotification>(BUFFERED)
        val commands = Channel<DebuggerCommandRequest>(BUFFERED)

        val proc = processBuilder.start()
        val errors = proc.errorStream.bufferedReader()
        val input = proc.inputStream.bufferedReader()
        val output = proc.outputStream.bufferedWriter()
        val client = DebuggerClient(scope, commands, notifications, logs, listener)

        scope.launch {
            try {
                client.run()
            } finally {
                client.shutdown()
            }
        }

        scope.launch {
            try {
                for (line in errors.lineSequence()) {
                    logs.send(line)
                }
            } finally {
                logs.close()
            }
        }

        scope.launch {
            try {
                for (line in input.lines()) {
                    notifications.send(Json.decodeFromString(line))
                }
            } finally {
                notifications.close()
            }
        }

        scope.launch {
            commands.consumeEach {
                output.write(Json.encodeToString(it))
            }
        }

        return client
    }

    private fun find(executable: String): File {
        val platform = Platform.CURRENT_PLATFORM
        val classpath = File(DebuggerClientLauncher::class.java.protectionDomain.codeSource.location.path).toPath()

        val executableName = if (platform.operatingSystem == OperatingSystem.WINDOWS) {
            "${executable}.exe"
        } else {
            executable
        }

        // Start from local file tree during development.
        return if (Files.isDirectory(classpath) && classpath.endsWith("build/classes/kotlin/main")) {
            val path = classpath.resolve("../../../../os/${platform.directoryName}/${executableName}")
            path.toFile()
        } else {
            Application.getOSFile(executableName)
        }
    }
}