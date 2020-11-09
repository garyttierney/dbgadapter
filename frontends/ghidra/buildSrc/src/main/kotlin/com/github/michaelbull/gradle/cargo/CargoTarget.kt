package com.github.michaelbull.gradle.cargo

import org.gradle.api.Project

open class CargoTarget(val name: String, private val project: Project) {
    var triple: String? = null
    var profile: String = "debug"

    val features = mutableListOf<String>()
    val environment = mutableMapOf<String, String>()
}

fun test() {

}