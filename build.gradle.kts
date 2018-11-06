import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.plugins.ExtraPropertiesExtension
import org.gradle.api.tasks.Delete
import org.gradle.jvm.tasks.Jar

buildscript {
    repositories {
        jcenter()
    }

    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.3.0")
        classpath("com.github.jengelman.gradle.plugins:shadow:2.0.2")
    }
}

plugins {
    id("org.jetbrains.kotlin.jvm") version "1.3.0"
    id("com.github.johnrengelman.shadow") version "2.0.2"
}

repositories {
    jcenter()
}

dependencies {
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.0")
    compile("org.jetbrains.kotlin:kotlin-reflect:1.3.0")
    compile("org.jetbrains.kotlinx:kotlinx-support-jdk8:0.3")

    compile("org.eclipse.jetty:jetty-server:9.4.8.v20180619")
    compile("org.eclipse.jetty:jetty-servlet:9.4.8.v20180619")
    compile("org.eclipse.jetty:jetty-webapp:9.4.8.v20180619")

    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.4")

    compile("mysql:mysql-connector-java:6.0.6")
    compile("com.zaxxer:HikariCP:2.7.6")
}

task<Copy>("copyCertificatesKeystore") {
    tasks["jar"].dependsOn(this)

    from(rootDir) { include("cacerts") }
    into("$buildDir/resources/main")
}

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "rdstestapp.AppKt"
}

task<Copy>("copyOriginalJar") {
    val jarTask = tasks["jar"] as Jar
    val originalJarFile = jarTask.archivePath
    val originalPath = originalJarFile.absolutePath

    dependsOn(jarTask)
    from(originalPath)
    into(originalJarFile.parent)
    rename(".jar", ".original.jar")
}

tasks.withType<ShadowJar> {
    dependsOn("copyOriginalJar")
    tasks["assemble"].dependsOn(this)

    baseName = "rds-test-app-jetty"
    classifier = null
    version = null
}
