package io.github.gangfunction.krefactorai.analyzer

import kotlin.test.*
import java.nio.file.Files
import kotlin.io.path.*

/**
 * Edge case tests for SourceCodeAnalyzer
 */
class SourceCodeAnalyzerEdgeCaseTest {

    private lateinit var analyzer: SourceCodeAnalyzer
    private lateinit var tempDir: java.nio.file.Path

    @BeforeTest
    fun setup() {
        analyzer = SourceCodeAnalyzer()
        tempDir = Files.createTempDirectory("krefactorai-edge-test")
    }

    @AfterTest
    fun cleanup() {
        tempDir.toFile().deleteRecursively()
    }

    // ========== Package Name Edge Cases ==========

    @Test
    fun `test package with multiple spaces`() {
        val code = """
            package    com.example.myapp
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.myapp", sourceFile.packageName)
    }

    @Test
    fun `test package with tabs`() {
        val code = "package\t\tcom.example.myapp\n\nclass MyClass"

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.myapp", sourceFile.packageName)
    }

    @Test
    fun `test package with trailing semicolon`() {
        val code = """
            package com.example.myapp;
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.myapp", sourceFile.packageName)
    }

    @Test
    fun `test very long package name`() {
        val longPackage = "com.example." + "verylongname".repeat(20)
        val code = """
            package $longPackage
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals(longPackage, sourceFile.packageName)
    }

    @Test
    fun `test package with numbers`() {
        val code = """
            package com.example.app123.module456
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.app123.module456", sourceFile.packageName)
    }

    @Test
    fun `test package with underscores`() {
        val code = """
            package com.example.my_app.my_module
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.my_app.my_module", sourceFile.packageName)
    }

    // ========== Import Edge Cases ==========

    @Test
    fun `test import with wildcard`() {
        val code = """
            package com.example.myapp

            import java.util.*
            import kotlin.collections.*

            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        // Wildcard imports should extract the package name without the asterisk
        assertTrue(sourceFile.imports.any { it.startsWith("java.util") })
        assertTrue(sourceFile.imports.any { it.startsWith("kotlin.collections") })
    }

    @Test
    fun `test import with alias`() {
        val code = """
            package com.example.myapp
            
            import java.util.List as JList
            import kotlin.collections.Map as KMap
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        // Should extract the original import path
        assertTrue(sourceFile.imports.any { it.contains("java.util.List") })
        assertTrue(sourceFile.imports.any { it.contains("kotlin.collections.Map") })
    }

    @Test
    fun `test multiple imports on same line`() {
        val code = """
            package com.example.myapp
            
            import java.util.List; import java.util.Map
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        // Should handle at least one import
        assertTrue(sourceFile.imports.isNotEmpty())
    }

    @Test
    fun `test import with comments`() {
        val code = """
            package com.example.myapp
            
            // This is a comment
            import java.util.List
            /* Multi-line comment */
            import java.util.Map
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertTrue(sourceFile.imports.contains("java.util.List"))
        assertTrue(sourceFile.imports.contains("java.util.Map"))
    }

    @Test
    fun `test no imports`() {
        val code = """
            package com.example.myapp
            
            class MyClass {
                fun doSomething() {}
            }
        """.trimIndent()

        val tempFile = tempDir.resolve("Test.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertTrue(sourceFile.imports.isEmpty())
    }

    // ========== File Content Edge Cases ==========

    @Test
    fun `test empty file`() {
        val tempFile = tempDir.resolve("Empty.kt")
        tempFile.writeText("")

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertNull(sourceFile.packageName)
        assertTrue(sourceFile.imports.isEmpty())
    }

    @Test
    fun `test file with only whitespace`() {
        val tempFile = tempDir.resolve("Whitespace.kt")
        tempFile.writeText("   \n\n\t\t\n   ")

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertNull(sourceFile.packageName)
        assertTrue(sourceFile.imports.isEmpty())
    }

    @Test
    fun `test file with only comments`() {
        val code = """
            // This is a comment
            /* This is a
               multi-line comment */
            // Another comment
        """.trimIndent()

        val tempFile = tempDir.resolve("Comments.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertNull(sourceFile.packageName)
    }

    @Test
    fun `test very large file`() {
        val code = buildString {
            appendLine("package com.example.large")
            appendLine()
            repeat(1000) {
                appendLine("import com.example.import$it.Class$it")
            }
            appendLine()
            appendLine("class LargeClass")
        }

        val tempFile = tempDir.resolve("Large.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.large", sourceFile.packageName)
        assertEquals(1000, sourceFile.imports.size)
    }

    @Test
    fun `test file with unicode characters`() {
        val code = """
            package com.example.unicode
            
            import com.example.日本語.クラス
            
            class MyClass
        """.trimIndent()

        val tempFile = tempDir.resolve("Unicode.kt")
        tempFile.writeText(code)

        val sourceFile = analyzer.analyzeSourceFile(tempFile)

        assertEquals("com.example.unicode", sourceFile.packageName)
    }

    // ========== Directory Scanning Edge Cases ==========

    @Test
    fun `test scan empty directory`() {
        val emptyDir = tempDir.resolve("empty")
        emptyDir.createDirectories()

        val sourceFiles = analyzer.scanSourceDirectory(emptyDir)

        assertTrue(sourceFiles.isEmpty())
    }

    @Test
    fun `test scan directory with nested subdirectories`() {
        val srcDir = tempDir.resolve("src")
        val subDir1 = srcDir.resolve("sub1")
        val subDir2 = srcDir.resolve("sub2/nested")
        
        subDir1.createDirectories()
        subDir2.createDirectories()

        subDir1.resolve("File1.kt").writeText("package com.example.sub1\nclass Class1")
        subDir2.resolve("File2.kt").writeText("package com.example.sub2\nclass Class2")

        val sourceFiles = analyzer.scanSourceDirectory(srcDir)

        assertEquals(2, sourceFiles.size)
    }

    @Test
    fun `test scan directory with mixed file types`() {
        val srcDir = tempDir.resolve("src")
        srcDir.createDirectories()

        srcDir.resolve("File1.kt").writeText("package com.example\nclass Class1")
        srcDir.resolve("File2.java").writeText("package com.example;\nclass Class2 {}")
        srcDir.resolve("File3.txt").writeText("This is not a source file")
        srcDir.resolve("File4.md").writeText("# README")

        val sourceFiles = analyzer.scanSourceDirectory(srcDir)

        // Should only include .kt and .java files
        assertEquals(2, sourceFiles.size)
    }

    @Test
    fun `test scan directory with hidden files`() {
        val srcDir = tempDir.resolve("src")
        srcDir.createDirectories()

        srcDir.resolve("File1.kt").writeText("package com.example\nclass Class1")
        srcDir.resolve(".hidden.kt").writeText("package com.example\nclass Hidden")

        val sourceFiles = analyzer.scanSourceDirectory(srcDir)

        // Behavior depends on implementation - might include or exclude hidden files
        assertTrue(sourceFiles.size >= 1)
    }

    // ========== Dependency Extraction Edge Cases ==========

    @Test
    fun `test extract dependencies with no matching packages`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("""
            package com.example.pkg1
            import java.util.List
            import kotlin.collections.Map
            class Class1
        """.trimIndent())

        val sourceFiles = listOf(analyzer.analyzeSourceFile(file1))
        val dependencies = analyzer.extractDependencies(sourceFiles)

        // Should not create dependencies to external packages
        assertTrue(dependencies.isEmpty())
    }

    @Test
    fun `test extract dependencies with self-import`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("""
            package com.example.pkg1
            import com.example.pkg1.SomeClass
            class Class1
        """.trimIndent())

        val sourceFiles = listOf(analyzer.analyzeSourceFile(file1))
        val dependencies = analyzer.extractDependencies(sourceFiles)

        // Should not create self-dependency
        assertTrue(dependencies.isEmpty())
    }

    @Test
    fun `test extract dependencies with circular imports`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("""
            package com.example.pkg1
            import com.example.pkg2.Class2
            class Class1
        """.trimIndent())

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText("""
            package com.example.pkg2
            import com.example.pkg1.Class1
            class Class2
        """.trimIndent())

        val sourceFiles = listOf(
            analyzer.analyzeSourceFile(file1),
            analyzer.analyzeSourceFile(file2)
        )

        val dependencies = analyzer.extractDependencies(sourceFiles)

        // Should detect both directions
        assertEquals(2, dependencies.size)
        assertTrue(dependencies.any { it.from == "com.example.pkg1" && it.to == "com.example.pkg2" })
        assertTrue(dependencies.any { it.from == "com.example.pkg2" && it.to == "com.example.pkg1" })
    }

    @Test
    fun `test extract dependencies with wildcard imports`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("""
            package com.example.pkg1
            import com.example.pkg2.*
            class Class1
        """.trimIndent())

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText("""
            package com.example.pkg2
            class Class2
        """.trimIndent())

        val sourceFiles = listOf(
            analyzer.analyzeSourceFile(file1),
            analyzer.analyzeSourceFile(file2)
        )

        val dependencies = analyzer.extractDependencies(sourceFiles)

        assertTrue(dependencies.any { it.from == "com.example.pkg1" && it.to == "com.example.pkg2" })
    }

    // ========== Grouping Edge Cases ==========

    @Test
    fun `test group by package with empty list`() {
        val grouped = analyzer.groupByPackage(emptyList())

        assertTrue(grouped.isEmpty())
    }

    @Test
    fun `test group by package with null package names`() {
        val file1 = tempDir.resolve("File1.kt")
        file1.writeText("class Class1")

        val file2 = tempDir.resolve("File2.kt")
        file2.writeText("class Class2")

        val sourceFiles = listOf(
            analyzer.analyzeSourceFile(file1),
            analyzer.analyzeSourceFile(file2)
        )

        val grouped = analyzer.groupByPackage(sourceFiles)

        // Files without package should be grouped together or excluded
        assertTrue(grouped.size <= 1)
    }

    @Test
    fun `test group by package with many files in same package`() {
        val files = (1..100).map { i ->
            val file = tempDir.resolve("File$i.kt")
            file.writeText("package com.example.same\nclass Class$i")
            analyzer.analyzeSourceFile(file)
        }

        val grouped = analyzer.groupByPackage(files)

        assertEquals(1, grouped.size)
        assertEquals(100, grouped["com.example.same"]?.size)
    }
}

