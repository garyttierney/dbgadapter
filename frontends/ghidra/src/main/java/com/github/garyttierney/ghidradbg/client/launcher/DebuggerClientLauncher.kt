package com.github.garyttierney.ghidradbg.client.launcher

import ghidra.framework.Application
import ghidra.framework.OperatingSystem
import ghidra.framework.Platform
import java.io.File
import java.nio.file.Files

object DebuggerClientLauncher {
    fun launch(executable: String, vararg options: String) : Process {
        val platform = Platform.CURRENT_PLATFORM
        val classpath = File(DebuggerClientLauncher::class.java.protectionDomain.codeSource.location.path).toPath()

        val executableName = if (platform.operatingSystem == OperatingSystem.WINDOWS) {
            "${executable}.exe"
        } else {
            executable
        }

        // Start from local file tree during development.
        val executablePath = if (Files.isDirectory(classpath) && classpath.endsWith("build/classes/kotlin/main")) {
            val path = classpath.resolve("../../../../os/${platform.directoryName}/${executableName}")
            path.toFile()
        } else {
            Application.getOSFile(executableName)
        }

        val processBuilder = ProcessBuilder(executablePath.toString(), *options)
        processBuilder.redirectInput(ProcessBuilder.Redirect.PIPE)
        processBuilder.redirectOutput(ProcessBuilder.Redirect.PIPE)
        processBuilder.redirectError(ProcessBuilder.Redirect.PIPE)

        return processBuilder.start()
    }

}