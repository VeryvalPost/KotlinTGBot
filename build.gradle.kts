val telegramBotVersion = "6.0.4"
val url =  "https://jitpack.io"

plugins {
    kotlin("jvm") version "1.8.20"
}

group = "ru.veryval.kotlinTGBot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.4")
    implementation("org.apache.logging.log4j:log4j-api:2.17.1")
    implementation("org.apache.logging.log4j:log4j-core:2.17.1")
}

tasks.withType<Jar> {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    manifest {
        attributes["Main-Class"] = "ru.veryval.kotlinTGBot.ApplicationKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain {
    }
}


