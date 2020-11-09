plugins {
    `kotlin-dsl`
}

gradlePlugin {
    plugins {
        register("cargo-plugin") {
            id = "cargo"
            implementationClass = "com.github.michaelbull.gradle.cargo.CargoPlugin"
        }
    }
}

dependencies {
    implementation(gradleApi())
    implementation(localGroovy())
}

repositories {
    jcenter()
}