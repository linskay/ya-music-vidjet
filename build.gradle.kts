plugins {
    application
    id("org.openjfx.javafxplugin") version "0.0.14"
}

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

application {
    mainClass.set("app.Main")
}

javafx {
    version = "21"
    modules = listOf("javafx.controls", "javafx.web")
}

// jpackage task
tasks.register<Exec>("jpackage") {
    dependsOn("build")
    commandLine(
        "jpackage",
        "--name", "YaMusicVidjet",
        "--input", "build/libs",
        "--main-jar", tasks.jar.get().archiveFileName.get(),
        "--main-class", "app.Main",
        "--type", "exe",
        "--win-console",
        "--dest", "build/jpackage"
    )
}
