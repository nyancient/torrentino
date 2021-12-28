plugins {
    kotlin("jvm") version "1.6.10"
    id("com.github.johnrengelman.shadow") version "7.1.1"
    application
}

group = "com.github.foxolotl"
version = "0.1"

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(kotlin("stdlib"))
    val ktorVersion = "1.6.7"
    implementation("io.ktor:ktor:$ktorVersion")
    implementation("io.ktor:ktor-client:$ktorVersion")
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-cli:0.3.4")
    implementation("com.rometools:rome:1.16.0")
    implementation("org.slf4j:slf4j-api:1.7.32")
    implementation("org.slf4j:slf4j-simple:1.7.32")
    implementation("cc.ekblad:4koma:0.4.1")
    implementation("cc.ekblad:kotline:0.3.0")
}

tasks {
    application {
        mainClass.set("com.github.foxolotl.torrentino.MainKt")
    }
}
