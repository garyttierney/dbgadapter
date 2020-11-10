import com.github.michaelbull.gradle.cargo.task.CargoBuildTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("cargo")
}

val ghidraInstallationDir: String by project

apply(from = "$ghidraInstallationDir/support/buildExtension.gradle")

cargo {
    cratePath = "${rootDir}/../../"

    targets {
        create("ghidra-dbg") {
            features += "windbg"
            profile = "debug"
        }
    }
}

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compileOnly(fileTree(ghidraInstallationDir) {
        include("Ghidra/Framework/**/lib/*.jar", "Ghidra/Features/**/lib/*.jar")
    })

    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.4.1")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.0.0")

    implementation("com.weblookandfeel:weblaf-ui:1.2.13")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    withType<CargoBuildTask> {
        doLast {
            copy {
                from(fileTree(buildOutputDirectory()) {
                    include("*.exe")
                })

                into("$rootDir/os/${platformName()}")
            }
        }
    }

    getByName<Jar>("shadowJar") {
        exclude { entry ->
            entry.file != null && entry.file.absoluteFile.startsWith(ghidraInstallationDir) ?: false
        }

        archiveClassifier.set("shadow")
        isZip64 = true
    }

    getByName<Zip>("buildExtension") {
        exclude { entry ->
            entry.file in getByName<Jar>("jar").outputs.files
        }

        from(getByName<Jar>("shadowJar")) {
            into("${project.name}/lib")
        }

        dependsOn("shadowJar")
    }
}


fun platformName(): String {
    val os = System.getProperty("os.name")
    val arch = System.getProperty("os.arch")
    val isX86 = arch == "x86" || arch == "i386"
    val isX64 = arch == "x86_64" || arch == "amd64"

    return when {
        os.startsWith("windows", true) && isX86 -> "win32"
        os.startsWith("windows", true) && isX64 -> "win64"
        os.startsWith("linux", true) && isX64 -> "linux64"
        os.startsWith("mac", true) && isX64 -> "osx64"
        else -> error("Unsupported platform: ${os}-${arch}")
    }
}
