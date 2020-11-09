package com.github.michaelbull.gradle.cargo.task

import com.github.michaelbull.gradle.cargo.cargo
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.plugins.BasePlugin
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.mapProperty
import java.io.File
import javax.inject.Inject

open class CargoBuildTask @Inject constructor(objects: ObjectFactory) : DefaultTask() {

    init {
        group = BasePlugin.BUILD_GROUP
    }

    fun buildOutputDirectory() =
        output.get().file(profile.get()).asFile

    @get:SkipWhenEmpty
    @get:InputFiles
    val sources = objects.fileCollection()

    @get:OutputDirectory
    val output = objects.directoryProperty()

    @get:Input
    val profile = objects.property(String::class.java)

    @get:Input
    val options = objects.mapProperty<String, String>()

    @get:Input
    val environment = objects.mapProperty<String, String>()

    @TaskAction
    fun build() {
        val cargo = project.cargo()
        val opts = options.get()
        val commandLineOptions = opts
            .flatMap { entry -> listOf(entry.key, entry.value) }
            .toMutableList()

        commandLineOptions += "build"

        when(val it = profile.get()) {
            "release" -> commandLineOptions += "--${it}"
            "debug" -> {}
            else -> {
                commandLineOptions += "--profile"
                commandLineOptions += it
            }
        }

        commandLineOptions += "--target-dir"
        commandLineOptions += output.get().asFile.absolutePath

        val result = project.exec {
            executable(cargo.cargoExecutable)
            args(commandLineOptions)
            workingDir(cargo.cratePath)
            environment(this@CargoBuildTask.environment.get())
        }

        result.assertNormalExitValue()
    }
}
