package io.github.gangfunction.krefactorai.analyzer

import java.nio.file.Files
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Edge case tests for AutoProjectAnalyzer
 */
class AutoProjectAnalyzerEdgeCaseTest {
    private lateinit var analyzer: AutoProjectAnalyzer
    private lateinit var tempDir: java.nio.file.Path

    @BeforeTest
    fun setup() {
        analyzer = AutoProjectAnalyzer()
        tempDir = Files.createTempDirectory("krefactorai-auto-test")
    }

    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    // ========== Empty Project Edge Cases ==========

    @Test
    fun `test analyze non-existent directory throws exception`() {
        val nonExistent = tempDir.resolve("does-not-exist")

        try {
            analyzer.analyze(nonExistent)
            assertTrue(false, "Should throw exception for non-existent path")
        } catch (e: IllegalArgumentException) {
            assertTrue(e.message!!.contains("does not exist"))
        }
    }

    // ========== Gradle Project Edge Cases ==========

    @Test
    fun `test analyze gradle project with build gradle kts`() {
        val buildFile = tempDir.resolve("build.gradle.kts")
        buildFile.toFile().writeText(
            """
            plugins {
                kotlin("jvm") version "1.9.0"
            }
            """.trimIndent(),
        )

        val srcDir = tempDir.resolve("src/main/kotlin")
        Files.createDirectories(srcDir)

        srcDir.resolve("Test.kt").toFile().writeText(
            """
            package com.example
            class Test
            """.trimIndent(),
        )

        val result = analyzer.analyze(tempDir)

        assertNotNull(result)
        assertNotNull(result.projectType)
        assertFalse(result.graph.isEmpty())
    }

    // ========== Maven Project Edge Cases ==========

    @Test
    fun `test analyze maven project with pom xml`() {
        val pomFile = tempDir.resolve("pom.xml")
        pomFile.toFile().writeText(
            """
            <project>
                <modelVersion>4.0.0</modelVersion>
                <groupId>com.example</groupId>
                <artifactId>test</artifactId>
                <version>1.0.0</version>
            </project>
            """.trimIndent(),
        )

        val srcDir = tempDir.resolve("src/main/kotlin")
        Files.createDirectories(srcDir)

        srcDir.resolve("Test.kt").toFile().writeText(
            """
            package com.example
            class Test
            """.trimIndent(),
        )

        val result = analyzer.analyze(tempDir)

        assertNotNull(result)
        assertTrue(result.projectType == ProjectType.MAVEN)
        assertFalse(result.graph.isEmpty())
    }
}

