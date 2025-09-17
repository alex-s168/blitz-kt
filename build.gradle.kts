plugins {
    kotlin("jvm") version "1.9.21"
    application
    `maven-publish`
}

group = "me.alex_s168"
version = "0.24.1"

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

java {
    withSourcesJar()
    withJavadocJar()
}

application {
    mainClass.set("blitz.FnpKt")
}

publishing {
    repositories {
        maven {
            name = "vxccLibs"
            url = uri("https://maven.vxcc.dev/libs")
            credentials(PasswordCredentials::class)
            authentication {
                create<BasicAuthentication>("basic")
            }
        }
    }

    publications {
        create<MavenPublication>("maven") {
            groupId = group.toString()
            artifactId = "blitz"
            version = version.toString()

            from(components["kotlin"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["javadocJar"])

            pom {
                name.set("blitz")
                url.set("https://github.com/alex_s168/blitz")
            }
        }
    }
}
