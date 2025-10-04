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
    private val timeoutMillis: Long = 30_000
) {
    
    private val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                prettyPrint = true
            })
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
        complexityScore: Double
    ): String {
        logger.info { "Generating refactoring suggestion for module: $moduleName" }
        
        val prompt = buildPrompt(moduleName, dependencies, dependents, complexityScore)
        
        return try {
            withTimeout(timeoutMillis) {
                val response = client.post("chat/completions") {
                    setBody(ChatCompletionRequest(
                        model = model,
                        messages = listOf(
                            ChatMessage(
                                role = "system",
                                content = """You are a senior Kotlin architect creating actionable refactoring checklists.
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
                                    |Include brief Kotlin code examples in ```kotlin blocks when helpful.
                                    |Be specific, actionable, and concise.
                                    """.trimMargin()
                            ),
                            ChatMessage(
                                role = "user",
                                content = prompt
                            )
                        ),
                        temperature = 0.3,
                        maxTokens = 600
                    ))
                }
                
                val completion: ChatCompletionResponse = response.body()
                val suggestion = completion.choices.firstOrNull()?.message?.content
                    ?: "No suggestion generated"
                
                logger.info { "Successfully generated suggestion for $moduleName" }
                suggestion
            }
        } catch (e: Exception) {
            logger.error(e) { "Failed to generate suggestion for $moduleName" }
            "Error generating suggestion: ${e.message}"
        }
    }

    /**
     * Build prompt for refactoring suggestion
     */
    private fun buildPrompt(
        moduleName: String,
        dependencies: List<String>,
        dependents: List<String>,
        complexityScore: Double
    ): String = buildString {
        appendLine("# Refactoring Analysis Request")
        appendLine()
        appendLine("## Package Information")
        appendLine("- **Name**: `$moduleName`")
        appendLine("- **Complexity Score**: ${"%.2f".format(complexityScore)} (0.0=simple, 1.0=very complex)")
        appendLine("- **Incoming Dependencies**: ${dependents.size} packages depend on this")
        appendLine("- **Outgoing Dependencies**: ${dependencies.size} packages this depends on")
        appendLine()

        // Detailed dependency information
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

        // Context-specific analysis request
        appendLine("## Analysis Context")
        when {
            complexityScore > 0.7 && dependents.size > 3 -> {
                appendLine("🚨 **CRITICAL REFACTORING NEEDED**")
                appendLine("- High complexity (${String.format("%.2f", complexityScore)}) + ${dependents.size} dependents")
                appendLine("- This is a bottleneck in the architecture")
                appendLine("- Refactoring will impact many packages")
            }
            complexityScore > 0.7 -> {
                appendLine("⚠️ **HIGH COMPLEXITY**")
                appendLine("- Complexity score: ${String.format("%.2f", complexityScore)}")
                appendLine("- Needs simplification and decomposition")
            }
            dependents.size > 5 -> {
                appendLine("⚠️ **CORE INFRASTRUCTURE PACKAGE**")
                appendLine("- ${dependents.size} packages depend on this")
                appendLine("- Changes must maintain backward compatibility")
                appendLine("- Consider extracting stable interfaces")
            }
            dependencies.size > 5 -> {
                appendLine("⚠️ **TOO MANY DEPENDENCIES**")
                appendLine("- Depends on ${dependencies.size} packages")
                appendLine("- Likely violates Single Responsibility Principle")
                appendLine("- Consider splitting into focused modules")
            }
            dependencies.isEmpty() && dependents.isEmpty() -> {
                appendLine("ℹ️ **ISOLATED PACKAGE**")
                appendLine("- No dependencies or dependents")
                appendLine("- Safe to refactor or potentially remove")
            }
            else -> {
                appendLine("✅ **STANDARD PACKAGE**")
                appendLine("- Moderate complexity and coupling")
            }
        }
        appendLine()

        appendLine("## Task")
        appendLine("Create an actionable refactoring checklist in Markdown format.")
        appendLine()
        appendLine("### Required Format:")
        appendLine("```markdown")
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

    /**
     * Test API connection
     */
    suspend fun testConnection(): Boolean {
        return try {
            withTimeout(10_000) {
                val response = client.post("chat/completions") {
                    setBody(ChatCompletionRequest(
                        model = model,
                        messages = listOf(
                            ChatMessage(role = "user", content = "Hello")
                        ),
                        maxTokens = 5
                    ))
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
    val maxTokens: Int = 500
)

@Serializable
data class ChatMessage(
    val role: String,
    val content: String
)

@Serializable
data class ChatCompletionResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage? = null
)

@Serializable
data class Choice(
    val index: Int,
    val message: ChatMessage,
    @SerialName("finish_reason")
    val finishReason: String
)

@Serializable
data class Usage(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

