plugins {
    application
    id("org.openjfx.javafxplugin") version "0.1.0"
    id("org.beryx.jlink") version "3.0.1"
    id("org.javamodularity.moduleplugin") version "1.8.15"
}

group = "miroshka"
version = "1.0-SNAPSHOT"

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

jlink {
    options = listOf("--strip-debug", "--compress", "2", "--no-header-files", "--no-man-pages")
    launcher {
        name = "RaschCalculator"
    }
    forceMerge("log4j")

    jpackage {
        installerType = "exe"
        installerName = "RaschModelCalculator"
        appVersion = "1.0.0"
        vendor = "Miroshka"
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.compilerArgs.addAll(listOf("--module-path", classpath.asPath))
}