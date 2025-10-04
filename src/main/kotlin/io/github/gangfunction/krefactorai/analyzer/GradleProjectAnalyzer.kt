package io.github.gangfunction.krefactorai.analyzer

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

/**
 * Analyzes Gradle projects (both Kotlin DSL and Groovy)
 */
class GradleProjectAnalyzer : ProjectAnalyzer {

    private val sourceCodeAnalyzer = SourceCodeAnalyzer()

    override fun canAnalyze(projectPath: Path): Boolean {
        val buildGradleKts = projectPath.resolve("build.gradle.kts")
        val buildGradle = projectPath.resolve("build.gradle")
        val settingsGradleKts = projectPath.resolve("settings.gradle.kts")
        val settingsGradle = projectPath.resolve("settings.gradle")

        return buildGradleKts.exists() || buildGradle.exists() ||
               settingsGradleKts.exists() || settingsGradle.exists()
    }

    override fun analyze(projectPath: Path): DependencyGraph {
        logger.info { "Analyzing Gradle project at: $projectPath" }

        val graph = DependencyGraph()
        val projectType = detectProjectType(projectPath)

        // Find all submodules
        val modules = findGradleModules(projectPath)
        logger.info { "Found ${modules.size} Gradle modules" }

        // Analyze source code for each module
        modules.forEach { moduleInfo ->
            analyzeModule(moduleInfo, graph)
        }

        logger.info { "Gradle project analysis complete" }
        return graph
    }

    override fun getProjectType(): ProjectType {
        return ProjectType.GRADLE_KOTLIN
    }

    /**
     * Detect if project uses Kotlin DSL or Groovy
     */
    private fun detectProjectType(projectPath: Path): ProjectType {
        return if (projectPath.resolve("build.gradle.kts").exists()) {
            ProjectType.GRADLE_KOTLIN
        } else {
            ProjectType.GRADLE_GROOVY
        }
    }

    /**
     * Find all Gradle modules in the project
     */
    private fun findGradleModules(projectPath: Path): List<GradleModuleInfo> {
        val modules = mutableListOf<GradleModuleInfo>()

        // Add root module
        modules.add(
            GradleModuleInfo(
                name = projectPath.fileName.toString(),
                path = projectPath,
                isRoot = true
            )
        )

        // Find submodules from settings.gradle.kts or settings.gradle
        val settingsFile = projectPath.resolve("settings.gradle.kts")
            .takeIf { it.exists() }
            ?: projectPath.resolve("settings.gradle").takeIf { it.exists() }

        if (settingsFile != null) {
            val submodules = parseSettingsFile(settingsFile, projectPath)
            modules.addAll(submodules)
        }

        return modules
    }

    /**
     * Parse settings.gradle.kts or settings.gradle to find submodules
     */
    private fun parseSettingsFile(settingsFile: Path, projectPath: Path): List<GradleModuleInfo> {
        val content = settingsFile.readText()
        val modules = mutableListOf<GradleModuleInfo>()

        // Match include("module-name") or include(":module-name")
        val includeRegex = Regex("""include\s*\(\s*["']([^"']+)["']\s*\)""")
        
        includeRegex.findAll(content).forEach { match ->
            val moduleName = match.groupValues[1].removePrefix(":")
            val modulePath = projectPath.resolve(moduleName.replace(":", "/"))

            if (modulePath.exists()) {
                modules.add(
                    GradleModuleInfo(
                        name = moduleName,
                        path = modulePath,
                        isRoot = false
                    )
                )
            }
        }

        logger.info { "Found ${modules.size} submodules in settings file" }
        return modules
    }

    /**
     * Analyze a single Gradle module
     */
    private fun analyzeModule(moduleInfo: GradleModuleInfo, graph: DependencyGraph) {
        logger.debug { "Analyzing module: ${moduleInfo.name}" }

        // Find source directories
        val srcDirs = findSourceDirectories(moduleInfo.path)

        if (srcDirs.isEmpty()) {
            logger.warn { "No source directories found for module: ${moduleInfo.name}" }
            return
        }

        // Scan source files
        val allSourceFiles = srcDirs.flatMap { sourceCodeAnalyzer.scanSourceDirectory(it) }

        if (allSourceFiles.isEmpty()) {
            logger.warn { "No source files found for module: ${moduleInfo.name}" }
            return
        }

        // Extract modules (packages) from source files
        val packages = sourceCodeAnalyzer.extractModules(allSourceFiles)
        packages.forEach { graph.addModule(it) }

        // Extract dependencies between packages
        val dependencies = sourceCodeAnalyzer.extractDependencies(allSourceFiles)
        dependencies.forEach { dep ->
            val fromModule = packages.find { it.name == dep.from }
            val toModule = packages.find { it.name == dep.to }

            if (fromModule != null && toModule != null) {
                graph.addDependency(Dependency(fromModule, toModule))
            }
        }

        logger.info { "Module ${moduleInfo.name}: ${packages.size} packages, ${dependencies.size} dependencies" }
    }

    /**
     * Find source directories in a Gradle module
     */
    private fun findSourceDirectories(modulePath: Path): List<Path> {
        val srcDirs = mutableListOf<Path>()

        // Standard Gradle source directories
        val standardPaths = listOf(
            "src/main/kotlin",
            "src/main/java",
            "src/main/groovy"
        )

        standardPaths.forEach { relativePath ->
            val srcPath = modulePath.resolve(relativePath)
            if (srcPath.exists() && srcPath.isDirectory()) {
                srcDirs.add(srcPath)
            }
        }

        return srcDirs
    }

    /**
     * Parse build.gradle.kts to extract module dependencies (optional enhancement)
     */
    private fun parseModuleDependencies(buildFile: Path): List<String> {
        if (!buildFile.exists()) {
            return emptyList()
        }

        val content = buildFile.readText()
        val dependencies = mutableListOf<String>()

        // Match project dependencies: implementation(project(":module-name"))
        val projectDepRegex = Regex("""(?:implementation|api|compile)\s*\(\s*project\s*\(\s*["']([^"']+)["']\s*\)\s*\)""")

        projectDepRegex.findAll(content).forEach { match ->
            val moduleName = match.groupValues[1].removePrefix(":")
            dependencies.add(moduleName)
        }

        return dependencies
    }
}

/**
 * Information about a Gradle module
 */
data class GradleModuleInfo(
    val name: String,
    val path: Path,
    val isRoot: Boolean
)

