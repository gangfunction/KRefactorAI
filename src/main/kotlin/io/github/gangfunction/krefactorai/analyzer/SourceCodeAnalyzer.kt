package io.github.gangfunction.krefactorai.analyzer

import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType
import mu.KotlinLogging
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

/**
 * Analyzes source code to extract package and import information
 */
class SourceCodeAnalyzer {
    /**
     * Scan a directory for Kotlin/Java source files and extract modules
     */
    fun scanSourceDirectory(sourcePath: Path): List<SourceFile> {
        if (!sourcePath.exists() || !sourcePath.isDirectory()) {
            logger.warn { "Source path does not exist or is not a directory: $sourcePath" }
            return emptyList()
        }

        val sourceFiles = mutableListOf<SourceFile>()

        Files.walk(sourcePath)
            .filter { it.isRegularFile() }
            .filter { it.extension == "kt" || it.extension == "java" }
            .forEach { file ->
                try {
                    val sourceFile = analyzeSourceFile(file)
                    sourceFiles.add(sourceFile)
                } catch (e: Exception) {
                    logger.error(e) { "Failed to analyze file: $file" }
                }
            }

        logger.info { "Scanned ${sourceFiles.size} source files in $sourcePath" }
        return sourceFiles
    }

    /**
     * Analyze a single source file
     */
    fun analyzeSourceFile(filePath: Path): SourceFile {
        val content = filePath.readText()
        val packageName = extractPackageName(content)
        val imports = extractImports(content)

        return SourceFile(
            path = filePath,
            packageName = packageName,
            imports = imports,
            isKotlin = filePath.extension == "kt",
        )
    }

    /**
     * Extract package name from source code
     */
    private fun extractPackageName(content: String): String? {
        val packageRegex = Regex("""^\s*package\s+([\w.]+)""", RegexOption.MULTILINE)
        return packageRegex.find(content)?.groupValues?.get(1)
    }

    /**
     * Extract import statements from source code
     */
    private fun extractImports(content: String): List<String> {
        val importRegex = Regex("""^\s*import\s+([\w.]+)(?:\.\*)?""", RegexOption.MULTILINE)
        return importRegex.findAll(content)
            .map { it.groupValues[1] }
            .filter { it.isNotBlank() }
            .toList()
    }

    /**
     * Group source files by package
     */
    fun groupByPackage(sourceFiles: List<SourceFile>): Map<String, List<SourceFile>> {
        return sourceFiles
            .filter { it.packageName != null }
            .groupBy { it.packageName!! }
    }

    /**
     * Extract modules from source files
     */
    fun extractModules(sourceFiles: List<SourceFile>): List<Module> {
        val packageGroups = groupByPackage(sourceFiles)

        return packageGroups.map { (packageName, files) ->
            val firstFile = files.first()
            Module(
                name = packageName,
                path = firstFile.path.parent.toString(),
                type = ModuleType.PACKAGE,
            )
        }
    }

    /**
     * Extract dependencies between packages
     */
    fun extractDependencies(sourceFiles: List<SourceFile>): List<PackageDependency> {
        val packageGroups = groupByPackage(sourceFiles)
        val dependencies = mutableListOf<PackageDependency>()

        packageGroups.forEach { (packageName, files) ->
            val importedPackages =
                files
                    .flatMap { it.imports }
                    .map { extractPackageFromImport(it) }
                    .filter { it != packageName } // Exclude self-dependencies
                    .distinct()

            importedPackages.forEach { importedPackage ->
                if (packageGroups.containsKey(importedPackage)) {
                    dependencies.add(
                        PackageDependency(
                            from = packageName,
                            to = importedPackage,
                        ),
                    )
                }
            }
        }

        return dependencies
    }

    /**
     * Extract package name from import statement
     * e.g., "com.example.MyClass" -> "com.example"
     */
    private fun extractPackageFromImport(importStatement: String): String {
        val parts = importStatement.split(".")
        return if (parts.size > 1) {
            // Remove the last part (class name) to get package
            parts.dropLast(1).joinToString(".")
        } else {
            importStatement
        }
    }

    /**
     * Find common package prefixes to group related modules
     */
    fun findPackagePrefixes(
        packages: List<String>,
        minDepth: Int = 2,
    ): List<String> {
        return packages
            .map { it.split(".").take(minDepth).joinToString(".") }
            .distinct()
            .sorted()
    }
}

/**
 * Represents a source file with its package and imports
 */
data class SourceFile(
    val path: Path,
    val packageName: String?,
    val imports: List<String>,
    val isKotlin: Boolean,
)

/**
 * Represents a dependency between two packages
 */
data class PackageDependency(
    val from: String,
    val to: String,
)
