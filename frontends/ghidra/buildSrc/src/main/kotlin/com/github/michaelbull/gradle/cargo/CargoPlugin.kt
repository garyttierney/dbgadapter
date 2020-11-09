package com.github.michaelbull.gradle.cargo

import com.github.michaelbull.gradle.cargo.task.CargoBuildTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.register
import org.gradle.util.GUtil
import java.io.File

internal const val EXTENSION_NAME = "cargo"

class CargoPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val cargo = project.extensions.create(EXTENSION_NAME, CargoExtension::class.java, project)
        val targets = cargo.targets

        project.afterEvaluate {
            val tasks = targets.map {target ->
                val buildTaskName = "cargoBuild${target.name.toTaskName()}"

                tasks.register<CargoBuildTask>(buildTaskName) {
                    output.fileValue(File(buildDir, "rust/"))
                    profile.set(target.profile)

                    val fileTree = fileTree(cargo.cratePath) {
                        include("Cargo.toml")
                        include("**/*.rs")
                        exclude("target/**")
                    }

                    sources.from(fileTree)
                }
            }

            project.tasks["classes"].dependsOn(tasks)
        }
    }
}

internal fun Project.cargo(): CargoExtension =
    @Suppress("UNCHECKED_CAST")
    extensions.getByName(EXTENSION_NAME) as? CargoExtension
        ?: throw IllegalStateException("$EXTENSION_NAME is not of the correct type")

// https://github.com/FRI-DAY/elasticmq-gradle-plugin
internal fun String.toTaskName() =
    this.toLowerCase()
        .mapNotNull(::toValidTaskNameCharacters)
        .joinToString(separator = "")
        .toCamelCase()

private fun toValidTaskNameCharacters(char: Char): Char? =
    if (char != '_' && Character.isJavaIdentifierPart(char)) {
        char
    } else {
        null
    }

private fun String.toCamelCase() = GUtil.toCamelCase(this)