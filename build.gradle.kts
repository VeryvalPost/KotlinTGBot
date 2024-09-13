
val telegramBotVersion = "6.0.4"
val url =  "https://jitpack.io"
plugins {
    kotlin("jvm") version "1.9.21"
}


group = "ru.veryval.kotlinTGBot"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven ("https://jitpack.io")
}

dependencies {
    testImplementation("org.jetbrains.kotlin:kotlin-test")
// https://mvnrepository.com/artifact/com.github.kotlin-telegram-bot.kotlin-telegram-bot/telegram
    implementation("com.github.kotlin-telegram-bot.kotlin-telegram-bot:telegram:6.0.4")
    implementation("com.squareup.okhttp3:okhttp:4.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")
}


tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(15)
}