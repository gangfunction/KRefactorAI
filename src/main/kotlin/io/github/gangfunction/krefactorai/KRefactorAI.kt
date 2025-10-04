package io.github.gangfunction.krefactorai

import io.github.gangfunction.krefactorai.ai.OpenAIClient
import io.github.gangfunction.krefactorai.analyzer.AutoProjectAnalyzer
import io.github.gangfunction.krefactorai.analyzer.AnalysisResult
import io.github.gangfunction.krefactorai.config.ApiKeyManager
import io.github.gangfunction.krefactorai.core.RefactoringOrderCalculator
import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.*
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.Path

private val logger = KotlinLogging.logger {}

/**
 * Main entry point for KRefactorAI library
 * 
 * Usage:
 * ```kotlin
 * val refactorAI = KRefactorAI()
 * val graph = DependencyGraph()
 * 
 * // Add modules and dependencies
 * graph.addModule(Module("ModuleA", "/path/to/A"))
 * graph.addModule(Module("ModuleB", "/path/to/B"))
 * graph.addDependency(Dependency(moduleA, moduleB))
 * 
 * // Analyze and get refactoring plan
 * val plan = refactorAI.analyze(graph)
 * println(plan)
 * ```
 */
class KRefactorAI(
    private val enableAI: Boolean = true,
    private val aiModel: String = "gpt-4"
) {
    
    private val openAIClient: OpenAIClient? = if (enableAI) {
        try {
            OpenAIClient(model = aiModel)
        } catch (e: IllegalStateException) {
            logger.warn { "OpenAI client initialization failed: ${e.message}" }
            logger.warn { "AI suggestions will be disabled. Analysis will continue without AI." }
            null
        }
    } else {
        null
    }

    init {
        logger.info { "KRefactorAI initialized (AI enabled: $enableAI)" }
        
        if (enableAI && openAIClient == null) {
            logger.warn { "AI features are disabled due to missing API key" }
            ApiKeyManager.printSetupInstructions()
        }
    }

    /**
     * Analyze a dependency graph and generate refactoring plan
     */
    fun analyze(graph: DependencyGraph, includeAISuggestions: Boolean = enableAI): RefactoringPlan {
        logger.info { "Starting dependency analysis" }
        
        if (graph.isEmpty()) {
            logger.warn { "Empty dependency graph provided" }
            return RefactoringPlan(
                modules = emptyList(),
                circularDependencies = emptyList(),
                totalComplexity = 0.0,
                estimatedTime = "0 minutes"
            )
        }

        // Calculate refactoring order
        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        // Add AI suggestions if enabled
        if (includeAISuggestions && openAIClient != null) {
            logger.info { "Generating AI suggestions for ${plan.modules.size} modules" }
            val enhancedSteps = addAISuggestions(plan.modules)
            
            return plan.copy(modules = enhancedSteps)
        }

        return plan
    }

    /**
     * Add AI suggestions to refactoring steps
     */
    private fun addAISuggestions(steps: List<RefactoringStep>): List<RefactoringStep> = runBlocking {
        steps.map { step ->
            try {
                val suggestion = openAIClient?.generateRefactoringSuggestion(
                    moduleName = step.module.name,
                    dependencies = step.dependencies.map { it.name },
                    dependents = step.dependents.map { it.name },
                    complexityScore = step.complexityScore
                )
                
                step.copy(aiSuggestion = suggestion)
            } catch (e: Exception) {
                logger.error(e) { "Failed to generate AI suggestion for ${step.module.name}" }
                step
            }
        }
    }

    /**
     * Quick analysis without AI suggestions
     */
    fun quickAnalyze(graph: DependencyGraph): RefactoringPlan {
        return analyze(graph, includeAISuggestions = false)
    }

    /**
     * Automatically analyze a project from its path
     * This will detect the project type (Gradle/Maven) and analyze source code
     */
    fun analyzeProject(projectPath: String, includeAISuggestions: Boolean = enableAI): AnalysisResult {
        return analyzeProject(Path(projectPath), includeAISuggestions)
    }

    /**
     * Automatically analyze a project from its path
     */
    fun analyzeProject(projectPath: Path, includeAISuggestions: Boolean = enableAI): AnalysisResult {
        logger.info { "Auto-analyzing project at: $projectPath" }

        val autoAnalyzer = AutoProjectAnalyzer()
        val result = autoAnalyzer.analyze(projectPath)

        logger.info { result.toString() }

        // Generate refactoring plan
        val plan = if (includeAISuggestions && openAIClient != null) {
            analyze(result.graph, includeAISuggestions = true)
        } else {
            analyze(result.graph, includeAISuggestions = false)
        }

        // Return enhanced result with refactoring plan
        return result.copy(
            refactoringPlan = plan,
            warnings = result.warnings + if (plan.circularDependencies.isNotEmpty()) {
                listOf("Refactoring plan contains ${plan.circularDependencies.size} circular dependencies")
            } else {
                emptyList()
            }
        )
    }

    /**
     * Automatically analyze a project and return the refactoring plan directly
     */
    fun analyzeProjectAndGetPlan(projectPath: String, includeAISuggestions: Boolean = enableAI): RefactoringPlan {
        return analyzeProjectAndGetPlan(Path(projectPath), includeAISuggestions)
    }

    /**
     * Automatically analyze a project and return the refactoring plan directly
     */
    fun analyzeProjectAndGetPlan(projectPath: Path, includeAISuggestions: Boolean = enableAI): RefactoringPlan {
        val result = analyzeProject(projectPath, includeAISuggestions)
        return result.refactoringPlan ?: throw IllegalStateException("Failed to generate refactoring plan")
    }

    /**
     * Get circular dependencies only
     */
    fun findCircularDependencies(graph: DependencyGraph): List<List<Module>> {
        return graph.detectCircularDependencies()
    }

    /**
     * Get refactoring layers (modules that can be refactored in parallel)
     */
    fun getRefactoringLayers(graph: DependencyGraph): List<Set<Module>>? {
        val calculator = RefactoringOrderCalculator(graph)
        return calculator.getRefactoringLayers()
    }

    /**
     * Test OpenAI API connection
     */
    suspend fun testAIConnection(): Boolean {
        return openAIClient?.testConnection() ?: false
    }

    /**
     * Check if AI features are available
     */
    fun isAIEnabled(): Boolean = openAIClient != null

    /**
     * Get API key status
     */
    fun getApiKeyStatus(): String {
        val status = ApiKeyManager.getApiKeyStatus()
        return status.message
    }

    /**
     * Print setup instructions
     */
    fun printSetupInstructions() {
        ApiKeyManager.printSetupInstructions()
    }

    /**
     * Close resources
     */
    fun close() {
        openAIClient?.close()
        logger.info { "KRefactorAI closed" }
    }

    companion object {
        /**
         * Create a simple example dependency graph for testing
         */
        fun createExampleGraph(): DependencyGraph {
            val graph = DependencyGraph()
            
            // Create modules
            val moduleA = Module("ModuleA", "/example/A", ModuleType.PACKAGE)
            val moduleB = Module("ModuleB", "/example/B", ModuleType.PACKAGE)
            val moduleC = Module("ModuleC", "/example/C", ModuleType.PACKAGE)
            val moduleD = Module("ModuleD", "/example/D", ModuleType.PACKAGE)
            val moduleE = Module("ModuleE", "/example/E", ModuleType.PACKAGE)
            
            // Add modules
            graph.addModule(moduleA)
            graph.addModule(moduleB)
            graph.addModule(moduleC)
            graph.addModule(moduleD)
            graph.addModule(moduleE)
            
            // Add dependencies
            // A depends on B and C
            graph.addDependency(Dependency(moduleA, moduleB, weight = 1.0))
            graph.addDependency(Dependency(moduleA, moduleC, weight = 1.0))
            
            // B depends on D
            graph.addDependency(Dependency(moduleB, moduleD, weight = 1.0))
            
            // C depends on D and E
            graph.addDependency(Dependency(moduleC, moduleD, weight = 1.0))
            graph.addDependency(Dependency(moduleC, moduleE, weight = 1.0))
            
            logger.info { "Created example graph with 5 modules and 5 dependencies" }
            return graph
        }

        /**
         * Get version information
         */
        fun getVersion(): String = "0.1.0-SNAPSHOT"

        /**
         * Get library information
         */
        fun getInfo(): String = """
            |KRefactorAI v${getVersion()}
            |Untangle Dependencies with AI and Math
            |
            |GitHub: https://github.com/gangfunction/KRefactorAI
            |Documentation: ${ApiKeyManager.getSetupGuideUrl()}
        """.trimMargin()
    }
}

