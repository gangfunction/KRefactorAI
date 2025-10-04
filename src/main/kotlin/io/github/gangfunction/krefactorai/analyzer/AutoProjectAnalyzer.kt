package io.github.gangfunction.krefactorai.analyzer

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

/**
 * Automatically detects project type and analyzes it
 */
class AutoProjectAnalyzer {

    private val analyzers = listOf(
        GradleProjectAnalyzer(),
        MavenProjectAnalyzer()
    )

    /**
     * Automatically detect and analyze a project
     */
    fun analyze(projectPath: Path): AnalysisResult {
        logger.info { "Auto-analyzing project at: $projectPath" }

        if (!projectPath.exists()) {
            throw IllegalArgumentException("Project path does not exist: $projectPath")
        }

        if (!projectPath.isDirectory()) {
            throw IllegalArgumentException("Project path is not a directory: $projectPath")
        }

        // Find suitable analyzer
        val analyzer = findAnalyzer(projectPath)
            ?: throw IllegalStateException("No suitable analyzer found for project at: $projectPath")

        logger.info { "Using ${analyzer.getProjectType()} analyzer" }

        // Analyze project
        val graph = analyzer.analyze(projectPath)

        // Create result
        val result = AnalysisResult(
            graph = graph,
            projectType = analyzer.getProjectType(),
            modulesFound = graph.getModules().size,
            dependenciesFound = graph.getDependencies().size,
            warnings = collectWarnings(graph)
        )

        logger.info { "Analysis complete: ${result.modulesFound} modules, ${result.dependenciesFound} dependencies" }
        return result
    }

    /**
     * Find a suitable analyzer for the project
     */
    private fun findAnalyzer(projectPath: Path): ProjectAnalyzer? {
        return analyzers.firstOrNull { it.canAnalyze(projectPath) }
    }

    /**
     * Detect project type without analyzing
     */
    fun detectProjectType(projectPath: Path): ProjectType {
        val analyzer = findAnalyzer(projectPath)
        return analyzer?.getProjectType() ?: ProjectType.UNKNOWN
    }

    /**
     * Check if a project can be analyzed
     */
    fun canAnalyze(projectPath: Path): Boolean {
        return findAnalyzer(projectPath) != null
    }

    /**
     * Collect warnings from the analysis
     */
    private fun collectWarnings(graph: DependencyGraph): List<String> {
        val warnings = mutableListOf<String>()

        // Check for empty graph
        if (graph.isEmpty()) {
            warnings.add("No modules found in the project")
        }

        // Check for circular dependencies
        val cycles = graph.detectCircularDependencies()
        if (cycles.isNotEmpty()) {
            warnings.add("Found ${cycles.size} circular dependencies")
        }

        // Check for isolated modules
        val isolatedModules = graph.getModules().filter { module ->
            graph.getInDegree(module) == 0 && graph.getOutDegree(module) == 0
        }
        if (isolatedModules.isNotEmpty()) {
            warnings.add("Found ${isolatedModules.size} isolated modules (no dependencies)")
        }

        return warnings
    }

    /**
     * Analyze multiple projects and merge their graphs
     */
    fun analyzeMultiple(projectPaths: List<Path>): AnalysisResult {
        logger.info { "Analyzing ${projectPaths.size} projects" }

        val mergedGraph = DependencyGraph()
        val allWarnings = mutableListOf<String>()
        var projectType = ProjectType.UNKNOWN

        projectPaths.forEach { projectPath ->
            try {
                val result = analyze(projectPath)
                
                // Merge graphs
                result.graph.getModules().forEach { mergedGraph.addModule(it) }
                result.graph.getDependencies().forEach { mergedGraph.addDependency(it) }
                
                // Collect warnings
                allWarnings.addAll(result.warnings)
                
                // Use first detected project type
                if (projectType == ProjectType.UNKNOWN) {
                    projectType = result.projectType
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to analyze project: $projectPath" }
                allWarnings.add("Failed to analyze $projectPath: ${e.message}")
            }
        }

        return AnalysisResult(
            graph = mergedGraph,
            projectType = projectType,
            modulesFound = mergedGraph.getModules().size,
            dependenciesFound = mergedGraph.getDependencies().size,
            warnings = allWarnings
        )
    }

    /**
     * Get project information without full analysis
     */
    fun getProjectInfo(projectPath: Path): ProjectInfo {
        val projectType = detectProjectType(projectPath)
        val canAnalyze = canAnalyze(projectPath)

        return ProjectInfo(
            path = projectPath,
            type = projectType,
            canAnalyze = canAnalyze,
            name = projectPath.fileName.toString()
        )
    }
}

/**
 * Basic project information
 */
data class ProjectInfo(
    val path: Path,
    val type: ProjectType,
    val canAnalyze: Boolean,
    val name: String
) {
    override fun toString(): String = buildString {
        appendLine("Project: $name")
        appendLine("Path: $path")
        appendLine("Type: $type")
        appendLine("Can Analyze: $canAnalyze")
    }
}

