plugins {
    kotlin("jvm") version "1.9.21"
    application
}

group = "me.alex_s168"
version = "0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("org.jetbrains.kotlinx:kotlinx-io-core:0.3.1")
    implementation("org.jetbrains.kotlinx:kotlinx-io-bytestring:0.3.1")
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    mainClass.set("me.alex_s168.kreflect.FnpKt")
}
