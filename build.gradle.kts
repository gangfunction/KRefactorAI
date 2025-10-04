plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("maven-publish")
    id("jacoco")
    id("org.jlleitschuh.gradle.ktlint") version "12.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
    application
}

group = "io.github.gangfunction"
version = "0.1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin Standard Library
    implementation(kotlin("stdlib"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

    // JSON Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.2")

    // HTTP Client (Ktor)
    implementation("io.ktor:ktor-client-core:2.3.7")
    implementation("io.ktor:ktor-client-cio:2.3.7")
    implementation("io.ktor:ktor-client-content-negotiation:2.3.7")
    implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.7")
    implementation("io.ktor:ktor-client-logging:2.3.7")

    // Graph Analysis (JGraphT)
    implementation("org.jgrapht:jgrapht-core:1.5.2")

    // Linear Algebra (Apache Commons Math)
    implementation("org.apache.commons:commons-math3:3.6.1")

    // Logging
    implementation("io.github.microutils:kotlin-logging-jvm:3.0.5")
    implementation("ch.qos.logback:logback-classic:1.4.14")

    // Testing
    testImplementation(kotlin("test"))
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testImplementation("io.mockk:mockk:1.13.8")
    testImplementation("io.ktor:ktor-client-mock:2.3.7")
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.70".toBigDecimal()
            }
        }
    }
}

kotlin {
    jvmToolchain(17)
}

ktlint {
    version.set("1.1.0")
    android.set(false)
    outputToConsole.set(true)
    ignoreFailures.set(true)  // Don't fail build, just report
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
    ignoreFailures = true  // Don't fail build, just report
}

application {
    mainClass.set("io.github.gangfunction.krefactorai.MainAutoKt")
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("KRefactorAI")
                description.set("Untangle Dependencies with AI and Math")
                url.set("https://github.com/gangfunction/KRefactorAI")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }

                developers {
                    developer {
                        id.set("gangfunction")
                        name.set("Lee Gangju")
                        email.set("gangfunction@gmail.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/gangfunction/KRefactorAI.git")
                    developerConnection.set("scm:git:ssh://github.com/gangfunction/KRefactorAI.git")
                    url.set("https://github.com/gangfunction/KRefactorAI")
                }
            }
        }
    }

    repositories {
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/gangfunction/KRefactorAI")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
    }
}
