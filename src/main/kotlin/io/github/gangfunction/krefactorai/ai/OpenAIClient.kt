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
                                content = """You are a senior Kotlin architect with 10+ years of experience in large-scale refactoring.
                                    |
                                    |Your responses MUST:
                                    |1. Be SPECIFIC - mention exact Kotlin patterns, not generic advice
                                    |2. Include CODE examples when relevant
                                    |3. Reference specific refactoring techniques (Extract Interface, Dependency Injection, etc.)
                                    |4. Consider Kotlin idioms: data classes, sealed classes, extension functions, coroutines
                                    |5. Provide step-by-step action items
                                    |
                                    |AVOID:
                                    |- Generic statements like "improve code quality"
                                    |- Vague advice without concrete steps
                                    |- Repeating the problem without solutions
                                    """.trimMargin()
                            ),
                            ChatMessage(
                                role = "user",
                                content = prompt
                            )
                        ),
                        temperature = 0.2,
                        maxTokens = 500
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

        appendLine("## Required Output")
        appendLine("Provide a refactoring plan with:")
        appendLine()
        appendLine("### 1. Specific Refactoring Actions")
        appendLine("List 2-3 CONCRETE actions with Kotlin code patterns:")
        appendLine("- Example: \"Extract interface `IUserRepository` from `UserRepository` class\"")
        appendLine("- Example: \"Split into `UserValidation` and `UserPersistence` packages\"")
        appendLine("- Example: \"Convert to sealed class hierarchy for type safety\"")
        appendLine()
        appendLine("### 2. Implementation Steps")
        appendLine("Provide step-by-step instructions:")
        appendLine("- Step 1: [specific action]")
        appendLine("- Step 2: [specific action]")
        appendLine("- Step 3: [specific action]")
        appendLine()
        appendLine("### 3. Risk Mitigation")
        appendLine("What specific issues to watch for during refactoring?")
        appendLine()
        appendLine("**Format**: Use bullet points. Be specific. Include Kotlin code examples if relevant.")
        appendLine("**Length**: 200-300 words maximum.")
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

