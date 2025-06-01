import java.io.BufferedReader

plugins {
    kotlin("jvm") version "2.1.21"
    id("com.gradleup.shadow") version "8.3.6"
    eclipse
}

val commitHash = Runtime
    .getRuntime()
    .exec(arrayOf("git", "rev-parse", "--short=10", "HEAD"))
    .let { process ->
        process.waitFor()
        val output = process.inputStream.use {
            it.bufferedReader().use(BufferedReader::readText)
        }
        process.destroy()
        output.trim()
    }

val apiVersion = "1.19"

group = "nl.skbotnl.chatog"
version = "$apiVersion-$commitHash"

repositories {
    mavenCentral()
    maven {
        url = uri("https://repo.purpurmc.org/snapshots")
    }
    maven {
        url = uri("https://jitpack.io")
    }
    maven {
        url = uri("https://repo.extendedclip.com/content/repositories/placeholderapi/")
    }
    maven {
        url = uri("https://repo.essentialsx.net/releases/")
    }
}

dependencies {
    compileOnly("org.purpurmc.purpur:purpur-api:1.19.4-R0.1-SNAPSHOT")
    compileOnly("com.github.MilkBowl:VaultAPI:1.7.1")
    compileOnly("me.clip:placeholderapi:2.11.6")
    compileOnly("net.essentialsx:EssentialsX:2.21.0")
    compileOnly(files("libs/AnnouncerPlus-1.4.1.jar"))

    implementation("net.dv8tion:JDA:5.5.1") {
        exclude(module = "opus-java")
    }
    implementation(project(":libs:Utilities-OG"))
    implementation("club.minnced:jda-ktx:0.12.0")
    implementation("club.minnced:discord-webhooks:0.8.4")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("io.github.crackthecodeabhi:kreds:0.9.1")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
}

val targetJavaVersion = 17
kotlin {
    jvmToolchain(targetJavaVersion)
}

tasks.build {
    dependsOn("shadowJar")
}

tasks.jar {
    archiveClassifier.set("part")
}

tasks.shadowJar {
    archiveClassifier.set("")
    minimize()
}

tasks.named<ProcessResources>("processResources") {
    val props = mapOf(
        "version" to version,
        "apiVersion" to apiVersion,
    )
    inputs.properties(props)
    filteringCharset = "UTF-8"
    filesMatching("plugin.yml") {
        expand(props)
    }

    from("LICENSE") {
        into("/")
    }
}

tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(targetJavaVersion)
        vendor = JvmVendorSpec.GRAAL_VM
    }
}
