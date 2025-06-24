/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

allprojects {
    repositories {
        mavenLocal()
        mavenCentral()
    }

    configurations {
        maybeCreate("runtimeClasspath")
        maybeCreate("provided")
    }

    val copyLibsCompileTask = tasks.register("copyLibsCompile", Copy::class.java) {
        from(configurations["runtimeClasspath"])
        into(File(project.rootDir, "build/libs"))
    }

    plugins.withId("org.jetbrains.kotlin.jvm") {
        tasks.withType(JavaCompile::class.java).all {
            dependsOn(copyLibsCompileTask)
            sourceCompatibility = JavaVersion.VERSION_11.toString()
            targetCompatibility = JavaVersion.VERSION_11.toString()
        }
    }
}

tasks.register("clean", Delete::class.java) {
    delete(rootProject.layout.buildDirectory)
}
