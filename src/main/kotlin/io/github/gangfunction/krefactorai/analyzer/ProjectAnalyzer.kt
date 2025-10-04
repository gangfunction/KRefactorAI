package io.github.gangfunction.krefactorai.analyzer

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import java.nio.file.Path

/**
 * Interface for analyzing projects and building dependency graphs
 */
interface ProjectAnalyzer {
    
    /**
     * Check if this analyzer can handle the given project
     */
    fun canAnalyze(projectPath: Path): Boolean
    
    /**
     * Analyze the project and build a dependency graph
     */
    fun analyze(projectPath: Path): DependencyGraph
    
    /**
     * Get the type of project this analyzer handles
     */
    fun getProjectType(): ProjectType
}

/**
 * Type of project
 */
enum class ProjectType {
    GRADLE_KOTLIN,
    GRADLE_GROOVY,
    MAVEN,
    UNKNOWN
}

/**
 * Result of project analysis
 */
data class AnalysisResult(
    val graph: DependencyGraph,
    val projectType: ProjectType,
    val modulesFound: Int,
    val dependenciesFound: Int,
    val warnings: List<String> = emptyList()
) {
    override fun toString(): String = buildString {
        appendLine("=== Analysis Result ===")
        appendLine("Project Type: $projectType")
        appendLine("Modules Found: $modulesFound")
        appendLine("Dependencies Found: $dependenciesFound")
        if (warnings.isNotEmpty()) {
            appendLine("Warnings:")
            warnings.forEach { appendLine("  - $it") }
        }
    }
}

