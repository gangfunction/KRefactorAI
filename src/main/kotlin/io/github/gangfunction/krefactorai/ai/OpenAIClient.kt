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
                                content = "You are an expert software architect specializing in code refactoring and dependency management."
                            ),
                            ChatMessage(
                                role = "user",
                                content = prompt
                            )
                        ),
                        temperature = 0.7,
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
        appendLine("Analyze the following module and provide refactoring suggestions:")
        appendLine()
        appendLine("Module: $moduleName")
        appendLine("Complexity Score: ${"%.2f".format(complexityScore)} (0.0 = simple, 1.0 = complex)")
        appendLine()
        appendLine("Dependencies (${dependencies.size}):")
        if (dependencies.isEmpty()) {
            appendLine("  - None")
        } else {
            dependencies.take(10).forEach { appendLine("  - $it") }
            if (dependencies.size > 10) {
                appendLine("  ... and ${dependencies.size - 10} more")
            }
        }
        appendLine()
        appendLine("Dependents (${dependents.size}):")
        if (dependents.isEmpty()) {
            appendLine("  - None")
        } else {
            dependents.take(10).forEach { appendLine("  - $it") }
            if (dependents.size > 10) {
                appendLine("  ... and ${dependents.size - 10} more")
            }
        }
        appendLine()
        appendLine("Please provide:")
        appendLine("1. Key refactoring priorities for this module")
        appendLine("2. Potential risks or challenges")
        appendLine("3. Recommended approach (2-3 sentences)")
        appendLine()
        appendLine("Keep the response concise and actionable.")
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

