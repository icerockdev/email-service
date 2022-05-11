/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
        google()
        maven { // The google mirror is less flaky than mavenCentral()
            url = uri("https://maven-central.storage-download.googleapis.com/repos/central/data/")
        }
        maven { url = uri ("https://plugins.gradle.org/m2/") }
    }

    resolutionStrategy {
        eachPlugin {
            if (requested.id.id == "org.jetbrains.kotlin.jvm") {
                useVersion(gradle.rootProject.extra["kotlin_version"].toString())
            }
            if (requested.id.id == "kotlin-kapt") {
                useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${gradle.rootProject.extra["kotlin_version"]}")
            }
        }
    }
}

include(":email-service")
//
val properties = startParameter.projectProperties

// ./gradlew -PlibraryPublish publishToMavenLocal
// ./gradlew :email-service:publish -PlibraryPublish
val libraryPublish: Boolean = properties.containsKey("libraryPublish")
if(!libraryPublish) {
//    include(":sample")
}
