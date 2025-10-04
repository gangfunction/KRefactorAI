package io.github.gangfunction.krefactorai.analyzer

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Dependency
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.*

private val logger = KotlinLogging.logger {}

/**
 * Analyzes Maven projects
 */
class MavenProjectAnalyzer : ProjectAnalyzer {

    private val sourceCodeAnalyzer = SourceCodeAnalyzer()

    override fun canAnalyze(projectPath: Path): Boolean {
        return projectPath.resolve("pom.xml").exists()
    }

    override fun analyze(projectPath: Path): DependencyGraph {
        logger.info { "Analyzing Maven project at: $projectPath" }

        val graph = DependencyGraph()

        // Find all Maven modules
        val modules = findMavenModules(projectPath)
        logger.info { "Found ${modules.size} Maven modules" }

        // Analyze source code for each module
        modules.forEach { moduleInfo ->
            analyzeModule(moduleInfo, graph)
        }

        logger.info { "Maven project analysis complete" }
        return graph
    }

    override fun getProjectType(): ProjectType {
        return ProjectType.MAVEN
    }

    /**
     * Find all Maven modules in the project
     */
    private fun findMavenModules(projectPath: Path): List<MavenModuleInfo> {
        val modules = mutableListOf<MavenModuleInfo>()

        // Add root module
        val rootPom = projectPath.resolve("pom.xml")
        if (rootPom.exists()) {
            modules.add(
                MavenModuleInfo(
                    name = extractArtifactId(rootPom) ?: projectPath.fileName.toString(),
                    path = projectPath,
                    pomFile = rootPom,
                    isRoot = true
                )
            )

            // Find submodules from pom.xml
            val submodules = parseModulesFromPom(rootPom, projectPath)
            modules.addAll(submodules)
        }

        return modules
    }

    /**
     * Parse pom.xml to find submodules
     */
    private fun parseModulesFromPom(pomFile: Path, projectPath: Path): List<MavenModuleInfo> {
        val content = pomFile.readText()
        val modules = mutableListOf<MavenModuleInfo>()

        // Simple XML parsing for <module> tags
        val moduleRegex = Regex("""<module>([^<]+)</module>""")

        moduleRegex.findAll(content).forEach { match ->
            val moduleName = match.groupValues[1].trim()
            val modulePath = projectPath.resolve(moduleName)
            val modulePom = modulePath.resolve("pom.xml")

            if (modulePom.exists()) {
                modules.add(
                    MavenModuleInfo(
                        name = extractArtifactId(modulePom) ?: moduleName,
                        path = modulePath,
                        pomFile = modulePom,
                        isRoot = false
                    )
                )
            }
        }

        logger.info { "Found ${modules.size} submodules in pom.xml" }
        return modules
    }

    /**
     * Extract artifactId from pom.xml
     */
    private fun extractArtifactId(pomFile: Path): String? {
        val content = pomFile.readText()
        val artifactIdRegex = Regex("""<artifactId>([^<]+)</artifactId>""")
        return artifactIdRegex.find(content)?.groupValues?.get(1)?.trim()
    }

    /**
     * Analyze a single Maven module
     */
    private fun analyzeModule(moduleInfo: MavenModuleInfo, graph: DependencyGraph) {
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
     * Find source directories in a Maven module
     */
    private fun findSourceDirectories(modulePath: Path): List<Path> {
        val srcDirs = mutableListOf<Path>()

        // Standard Maven source directories
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
}

/**
 * Information about a Maven module
 */
data class MavenModuleInfo(
    val name: String,
    val path: Path,
    val pomFile: Path,
    val isRoot: Boolean
)

