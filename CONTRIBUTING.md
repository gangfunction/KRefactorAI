package io.github.gangfunction.krefactorai.ai

import io.github.gangfunction.krefactorai.config.ApiKeyManager
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.logging.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with OpenAI API
 */
class OpenAIClient(
    private val apiKey: String = ApiKeyManager.getApiKey(),
    private val model: String = "gpt-4",
    private val timeoutMillis: Long = 30_000,
) {
    private val client =
        HttpClient(CIO) {
            install(ContentNegotiation) {
                json(
                    Json {
                        ignoreUnknownKeys = true
                        isLenient = true
                        prettyPrint = true
                    },
                )
            }

            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }

            install(HttpTimeout) {
                requestTimeoutMillis = timeoutMillis
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = timeoutMillis
            }

            defaultRequest {
                url("https://api.openai.com/v1/")
                header("Authorization", "Bearer $apiKey")
                header("Content-Type", "application/json")
            }
        }

    /**
     * Generate refactoring suggestion using OpenAI API
     */
    suspend fun generateRefactoringSuggestion(
        moduleName: String,
        dependencies: List<String>,
        dependents: List<String>,
        complexityScore: Double,
    ): String {
        logger.info { "Generating refactoring suggestion for module: $moduleName" }

        val prompt = buildPrompt(moduleName, dependencies, dependents, complexityScore)

        return try {
            val suggestion = callOpenAIAPI(prompt)
            logger.info { "Successfully generated suggestion for $moduleName" }
            suggestion
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate suggestion for $moduleName" }
            "Error generating suggestion: ${e.message}"
        }
    }

    private suspend fun callOpenAIAPI(prompt: String): String =
        withTimeout(timeoutMillis) {
            val response =
                client.post("chat/completions") {
                    setBody(createChatCompletionRequest(prompt))
                }

            val completion: ChatCompletionResponse = response.body()
            completion.choices.firstOrNull()?.message?.content ?: "No suggestion generated"
        }

    private fun createChatCompletionRequest(userPrompt: String) =
        ChatCompletionRequest(
            model = model,
            messages =
                listOf(
                    ChatMessage(role = "system", content = getSystemPrompt()),
                    ChatMessage(role = "user", content = userPrompt),
                ),
            temperature = 0.3,
            maxTokens = 600,
        )

    private fun getSystemPrompt() =
        """You are a senior Kotlin architect creating actionable refactoring checklists.
        |
        |Output Format Requirements:
        |1. Use GitHub-flavored Markdown with task lists
        |2. Each action must be a checkbox item: - [ ] Action description
        |3. Include specific Kotlin code patterns and examples
        |4. Organize into clear sections with ### headers
        |5. Keep total response under 400 words
        |
        |Required Sections:
        |### 🎯 Refactoring Actions
        |- [ ] Specific action with Kotlin pattern (e.g., Extract interface, Use sealed class)
        |- [ ] Another specific action
        |
        |### 📝 Implementation Steps
        |- [ ] Step 1: Concrete action
        |- [ ] Step 2: Concrete action
        |
        |### ⚠️ Risks & Mitigation
        |- [ ] Risk to watch for
        |- [ ] Mitigation strategy
        |
        |Include brief Kotlin code examples in ```kotlin
blocks when helpful.
        |Be specific, actionable, and concise.
        """.trimMargin()


    /**
     * Build prompt for refactoring suggestion
     */
    private fun buildPrompt(
        moduleName: String,
        dependencies: List<String>,
        dependents: List<String>,
        complexityScore: Double,
    ): String =
        buildString {
            appendLine("# Refactoring Analysis Request")
            appendLine()
            appendPackageInformation(moduleName, dependencies, dependents, complexityScore)
            appendDependencyDetails(dependencies, dependents)
            appendAnalysisContext(complexityScore, dependencies, dependents)

            appendLine("## Task")
            appendLine("Create an actionable refactoring checklist in Markdown format.")
            appendLine()
            appendLine("### Required Format:")
            appendLine("
```markdown")
            appendLine("### 🎯 Refactoring Actions")
            appendLine("- [ ] Extract interface for better abstraction")
            appendLine("- [ ] Use sealed class for type safety")
            appendLine("- [ ] Apply dependency injection pattern")
            appendLine()
            appendLine("### 📝 Implementation Steps")
            appendLine("- [ ] Step 1: Identify classes to refactor")
            appendLine("- [ ] Step 2: Create new interfaces/classes")
            appendLine("- [ ] Step 3: Update dependencies")
            appendLine("- [ ] Step 4: Run tests and verify")
            appendLine()
            appendLine("### ⚠️ Risks & Mitigation")
            appendLine("- [ ] Risk: Breaking changes → Mitigation: Use deprecation warnings")
            appendLine("- [ ] Risk: Performance impact → Mitigation: Add benchmarks")
            appendLine("```")
            appendLine()
            appendLine("Include brief Kotlin code examples where helpful. Keep it concise and actionable.")
        }

    private fun StringBuilder.appendPackageInformation(
        moduleName: String,
        dependencies: List<String>,
        dependents: List<String>,
        complexityScore: Double,
    ) {
        appendLine("## Package Information")
        appendLine("- **Name**: `$moduleName`")
        appendLine("- **Complexity Score**: ${"%.2f".format(complexityScore)} (0.0=simple, 1.0=very complex)")
        appendLine("- **Incoming Dependencies**: ${dependents.size} packages depend on this")
        appendLine("- **Outgoing Dependencies**: ${dependencies.size} packages this depends on")
        appendLine()
    }

    private fun StringBuilder.appendDependencyDetails(
        dependencies: List<String>,
        dependents: List<String>,
    ) {
        if (dependencies.isNotEmpty()) {
            appendLine("### Dependencies (what this package uses):")
            dependencies.take(5).forEach { appendLine("  - `$it`") }
            if (dependencies.size > 5) appendLine("  - ... and ${dependencies.size - 5} more")
            appendLine()
        }

        if (dependents.isNotEmpty()) {
            appendLine("### Dependents (packages that use this):")
            dependents.take(5).forEach { appendLine("  - `$it`") }
            if (dependents.size > 5) appendLine("  - ... and ${dependents.size - 5} more")
            appendLine()
        }
    }

    private fun StringBuilder.appendAnalysisContext(
        complexityScore: Double,
        dependencies: List<String>,
        dependents: List<String>,
    ) {
        appendLine("## Analysis Context")
        val contextMessage = determineContextMessage(complexityScore, dependencies.size, dependents.size)
        appendLine(contextMessage)
        appendLine()
    }

    @Suppress("MagicNumber")
    private fun determineContextMessage(
        complexityScore: Double,
        dependenciesCount: Int,
        dependentsCount: Int,
    ): String =
        when {
            complexityScore > 0.7 && dependentsCount > 3 -> buildCriticalMessage(complexityScore, dependentsCount)
            complexityScore > 0.7 -> buildHighComplexityMessage(complexityScore)
            dependentsCount > 5 -> buildCoreInfrastructureMessage(dependentsCount)
            dependenciesCount > 5 -> buildTooManyDependenciesMessage(dependenciesCount)
            dependenciesCount == 0 && dependentsCount == 0 -> buildIsolatedMessage()
            else -> buildStandardMessage()
        }

    private fun buildCriticalMessage(
        complexityScore: Double,
        dependentsCount: Int,
    ) = """
        🚨 **CRITICAL REFACTORING NEEDED**
        - High complexity (${"%.2f".format(complexityScore)}) + $dependentsCount dependents
        - This is a bottleneck in the architecture
        - Refactoring will impact many packages
    """.trimIndent()

    private fun buildHighComplexityMessage(complexityScore: Double) =
        """
        ⚠️ **HIGH COMPLEXITY**
        - Complexity score: ${"%.2f".format(complexityScore)}
        - Needs simplification and decomposition
        """.trimIndent()

    private fun buildCoreInfrastructureMessage(dependentsCount: Int) =
        """
        ⚠️ **CORE INFRASTRUCTURE PACKAGE**
        - $dependentsCount packages depend on this
        - Changes must maintain backward compatibility
        - Consider extracting stable interfaces
        """.trimIndent()

    private fun buildTooManyDependenciesMessage(dependenciesCount: Int) =
        """
        ⚠️ **TOO MANY DEPENDENCIES**
        - Depends on $dependenciesCount packages
        - Likely violates Single Responsibility Principle
        - Consider splitting into focused modules
        """.trimIndent()

    private fun buildIsolatedMessage() =
        """
        ℹ️ **ISOLATED PACKAGE**
        - No dependencies or dependents
        - Safe to refactor or potentially remove
        """.trimIndent()

    private fun buildStandardMessage() =
        """
        ✅ **STANDARD PACKAGE**
        - Moderate complexity and coupling
        """.trimIndent()

    /**
     * Test API connection
     */
    suspend fun testConnection(): Boolean {
        return try {
            withTimeout(10_000) {
                val response =
                    client.post("chat/completions") {
                        setBody(
                            ChatCompletionRequest(
                                model = model,
                                messages =
                                    listOf(
                                        ChatMessage(role = "user", content = "Hello"),
                                    ),
                                maxTokens = 5,
                            ),
                        )
                    }
                response.status == HttpStatusCode.OK
            }
        } catch (e: Exception) {
            logger.error(e) { "API connection test failed" }
            false
        }
    }

    /**
     * Close the HTTP client
     */
    fun close() {
        client.close()
    }
}

// Data classes for OpenAI API

@Serializable
data class ChatCompletionRequest(
    val model: String,
    val messages: List<ChatMessage>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 500,
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)


@Serializable
data class ChatMessage(
    val role: String,
    val content: String,
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null,
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String,
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int,
)
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
        fun getInfo(): String =
            """
            |KRefactorAI v${getVersion()}
            |Untangle Dependencies with AI and Math
            |
            |GitHub: https://github.com/gangfunction/KRefactorAI
            |Documentation: ${ApiKeyManager.getSetupGuideUrl()}
            """.trimMargin()
    }
}
