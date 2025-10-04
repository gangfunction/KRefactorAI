package io.github.gangfunction.krefactorai.analyzer

import java.nio.file.Files
import kotlin.io.path.*
import kotlin.test.*

class SourceCodeAnalyzerTest {
    private lateinit var analyzer: SourceCodeAnalyzer
    private lateinit var tempDir: java.nio.file.Path

    @BeforeTest
    fun setup() {
        analyzer = SourceCodeAnalyzer()
        tempDir = Files.createTempDirectory("krefactorai-test")
    }

    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    @Test
    fun `test extract package name from Kotlin code`() {
        val code =
            """
            package com.example.myapp
            
            class MyClass {
                fun doSomething() {}
            }
            """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.myapp", sourceFile.packageName)
        assertTrue(sourceFile.isKotlin)
    }

    @Test
    fun `test extract imports from Kotlin code`() {
        val code =
            """
            package com.example.myapp
            
            import java.util.List
            import kotlin.collections.Map
            import com.example.other.SomeClass
            
            class MyClass {
                fun doSomething() {}
            }
            """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals(3, sourceFile.imports.size)
        assertTrue(sourceFile.imports.contains("java.util.List"))
        assertTrue(sourceFile.imports.contains("kotlin.collections.Map"))
        assertTrue(sourceFile.imports.contains("com.example.other.SomeClass"))
    }

    @Test
    fun `test extract package name from Java code`() {
        val code =
            """
            package com.example.javaapp;
            
            public class JavaClass {
                public void doSomething() {}
            }
            """.trimIndent()

        val tempFile = tempDir.resolve("Test.java")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.javaapp", sourceFile.packageName)
        assertFalse(sourceFile.isKotlin)
    }

    @Test
    fun `test group source files by package`() {
        // Create multiple source files
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("package com.example.pkg1\nclass Class1")

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText("package com.example.pkg1\nclass Class2")

        val file3 = tempDir.resolve("File3.kt")
        file3.writeText("package com.example.pkg2\nclass Class3")

        val sourceFiles =
            listOf(
                analyzer.analyzeSourceFile(file1),
                analyzer.analyzeSourceFile(file2),
                analyzer.analyzeSourceFile(file3),
            )

        val grouped = analyzer.groupByPackage(sourceFiles)

        assertEquals(2, grouped.size)
        assertEquals(2, grouped["com.example.pkg1"]?.size)
        assertEquals(1, grouped["com.example.pkg2"]?.size)
    }

    @Test
    fun `test extract modules from source files`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("package com.example.pkg1\nclass Class1")

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText("package com.example.pkg2\nclass Class2")

        val sourceFiles =
            listOf(
                analyzer.analyzeSourceFile(file1),
                analyzer.analyzeSourceFile(file2),
            )

        val modules = analyzer.extractModules(sourceFiles)

        assertEquals(2, modules.size)
        assertTrue(modules.any { it.name == "com.example.pkg1" })
        assertTrue(modules.any { it.name == "com.example.pkg2" })
    }

    @Test
    fun `test extract dependencies between packages`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText(
            """
            package com.example.pkg1
            import com.example.pkg2.Class2
            class Class1
            """.trimIndent(),
        )

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText(
            """
            package com.example.pkg2
            class Class2
            """.trimIndent(),
        )

        val sourceFiles =
            listOf(
                analyzer.analyzeSourceFile(file1),
                analyzer.analyzeSourceFile(file2),
            )

        val dependencies = analyzer.extractDependencies(sourceFiles)

        assertTrue(dependencies.isNotEmpty())
        assertTrue(dependencies.any { it.from == "com.example.pkg1" && it.to == "com.example.pkg2" })
    }

    @Test
    fun `test scan source directory`() {
        // Create a directory structure
        val srcDir = tempDir.resolve("src")
        srcDir.createDirectories()

        val file1 = srcDir.resolve("File1.kt")
        file1.writeText("package com.example\nclass Class1")

        val file2 = srcDir.resolve("File2.kt")
        file2.writeText("package com.example\nclass Class2")

        val sourceFiles = analyzer.scanSourceDirectory(srcDir)

        assertEquals(2, sourceFiles.size)
    }

    @Test
    fun `test handle file without package`() {
        val code =
            """
            class MyClass {
                fun doSomething() {}
            }
            """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertNull(sourceFile.packageName)
    }
}
