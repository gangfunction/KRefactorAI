plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    id("maven-publish")
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
}

kotlin {
    jvmToolchain(17)
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
}

