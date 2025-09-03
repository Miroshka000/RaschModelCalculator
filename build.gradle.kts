plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
    id("org.javamodularity.moduleplugin") version "1.8.15"
}

group = "miroshka"
version = "1.0.4"

repositories {
    mavenCentral()
}

application {
    mainModule.set("RaschModelCalculator")
    mainClass.set("miroshka.rasch.Main")
}

javafx {
    version = "17.0.10"
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.swing")
}

dependencies {
    implementation("org.apache.poi:poi:5.2.5")
    implementation("org.apache.poi:poi-ooxml:5.2.5")
    implementation("io.github.mkpaz:atlantafx-base:2.0.1")
    implementation("com.zaxxer:SparseBitSet:1.3")
    implementation("org.apache.commons:commons-math3:3.6.1")
}

tasks.withType<Jar> {
    manifest {
        attributes(
            "Implementation-Title" to "RaschModelCalculator",
            "Implementation-Version" to version,
            "Implementation-Vendor" to "Miroshka",
            "Created-By" to "JDK ${System.getProperty("java.version")} (${System.getProperty("java.vendor")})",
            "Specification-Title" to "Rasch Model Calculator",
            "Specification-Version" to version,
            "Specification-Vendor" to "Miroshka",
            "Main-Class" to "miroshka.rasch.Main"
        )
    }
}

jlink {
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "RaschCalculator"
        jvmArgs = listOf("-Xms256m", "-Xmx1024m")
    }
    forceMerge("log4j")

    jpackage {
        installerType = "exe"
        installerName = "RaschModelCalculator"
        appVersion = version.toString()
        vendor = "Miroshka"
        
        icon = "${projectDir}/src/main/resources/icon.ico"
        
        installerOptions = listOf(
            "--win-dir-chooser", 
            "--win-menu", 
            "--win-shortcut", 
            "--win-menu-group", "RaschModelCalculator",
            "--description", "Rasch Model Calculator", 
            "--copyright", "© 2025 Miroshka",
            "--win-per-user-install",
            "--win-shortcut-prompt",
            "--license-file", "${projectDir}/LICENSE",
            "--app-version", version.toString(),
            "--vendor", "Miroshka",
            "--verbose"
        )
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("--module-path", classpath.asPath))
}

tasks.register<Copy>("createReadme") {
    val readmeFile = file("${buildDir}/resources/readme/README.txt")
    readmeFile.parentFile.mkdirs()
    readmeFile.writeText("""
        RaschModelCalculator v${version}
        
        Программа для анализа данных с использованием модели Раша.
        
        (c) 2025 Miroshka
        Лицензия: MIT
        
        Данное программное обеспечение разработано в образовательных целях.
    """.trimIndent())
}

tasks.register("updateVersion") {
    doLast {
        val propertiesFile = file("src/main/resources/application.properties")
        val newContent = "version=${version}"
        propertiesFile.writeText(newContent)
    }
}

tasks.named("processResources") {
    dependsOn("updateVersion")
}
tasks.named("jpackage") {
    dependsOn("createReadme", "updateVersion")
}