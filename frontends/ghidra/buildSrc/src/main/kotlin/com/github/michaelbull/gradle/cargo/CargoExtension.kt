package com.github.michaelbull.gradle.cargo

import groovy.lang.Closure
import org.gradle.api.NamedDomainObjectContainer
import org.gradle.api.Project

internal typealias CargoTargetContainer = NamedDomainObjectContainer<CargoTarget>

open class CargoExtension(private val project: Project) {
    var cratePath = "${project.projectDir}/src/main/rust"
    var crossCompiling = false
    var cargoExecutable = if(crossCompiling) "cross" else "cargo"

    val targets = project.container(CargoTarget::class.java) { name ->
        CargoTarget(name, project)
    }

    fun targets(config: CargoTargetContainer.() -> Unit) {
        targets.configure(object : Closure<Unit>(this, this) {
            fun doCall() {
                (delegate as? CargoTargetContainer)?.let {
                    config(it)
                }
            }
        })
    }

    fun targets(config: Closure<Unit>) {
        targets.configure(config)
    }
}