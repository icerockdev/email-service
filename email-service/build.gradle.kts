import java.util.Base64
import kotlin.text.String
/*
 * Copyright 2020 IceRock MAG Inc. Use of this source code is governed by the Apache 2.0 license.
 */

plugins {
    id("org.jetbrains.kotlin.jvm")
    id("kotlin-kapt")
    id("maven-publish")
    id("java-library")
    id("signing")
}

apply(plugin = "java")
apply(plugin = "kotlin")

group = "com.icerockdev.service"
version = "0.4.0"

val sourcesJar by tasks.registering(Jar::class) {
    archiveClassifier.set("sources")
    from(sourceSets.main.get().allSource)
}

dependencies {

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${properties["coroutines_version"]}")

    // https://mvnrepository.com/artifact/org.apache.commons/commons-email
    implementation("org.apache.commons:commons-email:${properties["common_email_version"]}")
    // logging
    implementation("ch.qos.logback:logback-classic:${properties["logback_version"]}")

    // tests
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:${properties["kotlin_version"]}")
    testImplementation(group = "com.github.kirviq", name = "dumbster", version = "1.7.1")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
    withJavadocJar()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_11.toString()
    }
}

repositories {
    mavenCentral()
}

publishing {
    repositories.maven("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/") {
        name = "OSSRH"

        credentials {
            username = System.getenv("OSSRH_USER")
            password = System.getenv("OSSRH_KEY")
        }
    }
    publications {
        register("mavenJava", MavenPublication::class) {
            from(components["java"])
            artifact(sourcesJar.get())
            pom {
                name.set("Email service")
                description.set("Email sending service for kotlin")
                url.set("https://github.com/icerockdev/email-service")
                licenses {
                    license {
                        url.set("https://github.com/icerockdev/email-service/blob/master/LICENSE.md")
                    }
                }

                developers {
                    developer {
                        id.set("YokiToki")
                        name.set("Stanislav")
                        email.set("skarakovski@icerockdev.com")
                    }

                    developer {
                        id.set("AlexeiiShvedov")
                        name.set("Alex Shvedov")
                        email.set("ashvedov@icerockdev.com")
                    }

                    developer {
                        id.set("oyakovlev")
                        name.set("Oleg Yakovlev")
                        email.set("oyakovlev@icerockdev.com")
                    }
                }

                scm {
                    connection.set("scm:git:ssh://github.com/icerockdev/email-service.git")
                    developerConnection.set("scm:git:ssh://github.com/icerockdev/email-service.git")
                    url.set("https://github.com/icerockdev/email-service")
                }
            }
        }

        signing {
            setRequired({!properties.containsKey("libraryPublishToMavenLocal")})
            val signingKeyId: String? = System.getenv("SIGNING_KEY_ID")
            val signingPassword: String? = System.getenv("SIGNING_PASSWORD")
            val signingKey: String? = System.getenv("SIGNING_KEY")?.let { base64Key ->
                String(Base64.getDecoder().decode(base64Key))
            }
            useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
            sign(publishing.publications["mavenJava"])
        }
    }
}
