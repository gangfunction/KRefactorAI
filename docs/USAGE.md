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
 *
```
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


Represents a single step in the refactoring plan.

```kotlin
data class RefactoringStep(
    val module: Module,
    val priority: Int,
    val complexityScore: Double,
    val dependencies: List<Module>,
    val dependents: List<Module>,
    val aiSuggestion: String? = null
)
```

---

## Examples

### Example 1: Simple Dependency Analysis

```kotlin
fun main() {
    val graph = DependencyGraph()
    
    // Create a simple dependency chain: A -> B -> C
    val moduleA = Module("A", "/src/A")
    val moduleB = Module("B", "/src/B")
    val moduleC = Module("C", "/src/C")
    
    graph.addDependency(Dependency(moduleA, moduleB))
    graph.addDependency(Dependency(moduleB, moduleC))
    
    val refactorAI = KRefactorAI(enableAI = false)
    val plan = refactorAI.quickAnalyze(graph)
    
    println(plan)
}
```

### Example 2: Detect Circular Dependencies

```kotlin
fun main() {
    val graph = DependencyGraph()
    
    // Create a circular dependency: A -> B -> C -> A
    val moduleA = Module("A", "/src/A")
    val moduleB = Module("B", "/src/B")
    val moduleC = Module("C", "/src/C")
    
    graph.addDependency(Dependency(moduleA, moduleB))
    graph.addDependency(Dependency(moduleB, moduleC))
    graph.addDependency(Dependency(moduleC, moduleA))
    
    val refactorAI = KRefactorAI(enableAI = false)
    val cycles = refactorAI.findCircularDependencies(graph)
    
    if (cycles.isNotEmpty()) {
        println("⚠️ Circular dependencies detected!")
        cycles.forEach { cycle ->
            println(cycle.joinToString(" -> ") { it.name })
        }
    }
}
```

### Example 3: AI-Powered Analysis

```kotlin
suspend fun main() {
    // Make sure OPENAI_API_KEY is set
    val refactorAI = KRefactorAI(enableAI = true, aiModel = "gpt-4")
    
    // Test API connection
    val isConnected = refactorAI.testAIConnection()
    if (!isConnected) {
        println("❌ Failed to connect to OpenAI API")
        return
    }
    
    val graph = createYourGraph()
    val plan = refactorAI.analyze(graph, includeAISuggestions = true)
    
    plan.modules.forEach { step ->
        println("Module: ${step.module.name}")
        println("Priority: ${step.priority}")
        println("Complexity: ${step.complexityScore}")
        println("AI Suggestion:")
        println(step.aiSuggestion)
        println()
    }
    
    refactorAI.close()
}
```

### Example 4: Using the Built-in Example

```kotlin
fun main() {
    // Use the built-in example graph
    val graph = KRefactorAI.createExampleGraph()
    
    val refactorAI = KRefactorAI(enableAI = false)
    val plan = refactorAI.analyze(graph)
    
    println(plan)
}
```

---

## Best Practices

1. **Always close resources**: Call `refactorAI.close()` when done to release HTTP client resources
2. **Handle API errors**: Wrap AI-enabled calls in try-catch blocks
3. **Use quick analysis for large graphs**: If you don't need AI suggestions, use `quickAnalyze()` for better performance
4. **Check for circular dependencies first**: Before refactoring, always check for circular dependencies
5. **Use layers for parallel work**: If possible, refactor modules in the same layer simultaneously

---

## Troubleshooting

### API Key Issues

If you encounter API key errors:

1. Verify the environment variable is set: `echo $OPENAI_API_KEY`
2. Check the API key format (should start with `sk-`)
3. Verify the key is active on OpenAI Platform
4. See [API Key Setup Guide](API_KEY_SETUP.md) for detailed instructions

### Performance Issues

For large graphs (>1000 modules):

1. Use `quickAnalyze()` instead of full analysis
2. Disable AI suggestions
3. Consider analyzing subgraphs separately

### Memory Issues

If you encounter OutOfMemoryError:

1. Increase JVM heap size: `-Xmx4g`
2. Process modules in batches
3. Use streaming analysis (future feature)

---

## Support

- GitHub Issues: [KRefactorAI Issues](https://github.com/gangfunction/KRefactorAI/issues)
- Documentation: [GitHub Wiki](https://github.com/gangfunction/KRefactorAI/wiki)
- Email: gangfunction@gmail.com

