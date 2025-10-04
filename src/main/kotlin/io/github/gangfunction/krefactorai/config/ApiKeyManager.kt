package io.github.gangfunction.krefactorai.config

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Manages OpenAI API key from environment variables
 */
object ApiKeyManager {
    
    private const val API_KEY_ENV_VAR = "OPENAI_API_KEY"
    private const val SETUP_GUIDE_URL = "https://github.com/gangfunction/KRefactorAI/blob/main/docs/API_KEY_SETUP.md"
    
    /**
     * Get the OpenAI API key from environment variable
     * @throws IllegalStateException if the API key is not set
     */
    fun getApiKey(): String {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
        
        if (apiKey.isNullOrBlank()) {
            val errorMessage = buildString {
                appendLine("❌ Error: OPENAI_API_KEY environment variable is not set.")
                appendLine()
                appendLine("Please set your OpenAI API key as an environment variable:")
                appendLine()
                appendLine("macOS/Linux:")
                appendLine("  export OPENAI_API_KEY=\"your-api-key-here\"")
                appendLine()
                appendLine("Windows (PowerShell):")
                appendLine("  \$env:OPENAI_API_KEY=\"your-api-key-here\"")
                appendLine()
                appendLine("For detailed setup instructions, please visit:")
                appendLine("  $SETUP_GUIDE_URL")
            }
            
            logger.error { "API key not found in environment variables" }
            throw IllegalStateException(errorMessage)
        }
        
        // Validate API key format (basic check)
        if (!isValidApiKeyFormat(apiKey)) {
            logger.warn { "API key format appears invalid" }
            throw IllegalStateException(
                "Invalid API key format. OpenAI API keys should start with 'sk-' or 'sk-proj-'.\n" +
                "Please check your API key and refer to: $SETUP_GUIDE_URL"
            )
        }
        
        logger.info { "✅ API key loaded successfully (${apiKey.take(10)}...)" }
        return apiKey
    }
    
    /**
     * Check if API key is set (without throwing exception)
     */
    fun isApiKeySet(): Boolean {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
        return !apiKey.isNullOrBlank() && isValidApiKeyFormat(apiKey)
    }
    
    /**
     * Get API key status information
     */
    fun getApiKeyStatus(): ApiKeyStatus {
        val apiKey = System.getenv(API_KEY_ENV_VAR)
        
        return when {
            apiKey.isNullOrBlank() -> ApiKeyStatus.NOT_SET
            !isValidApiKeyFormat(apiKey) -> ApiKeyStatus.INVALID_FORMAT
            else -> ApiKeyStatus.VALID
        }
    }
    
    /**
     * Validate API key format (basic check)
     */
    private fun isValidApiKeyFormat(apiKey: String): Boolean {
        // OpenAI API keys typically start with "sk-" or "sk-proj-"
        return apiKey.startsWith("sk-") && apiKey.length > 20
    }
    
    /**
     * Get setup guide URL
     */
    fun getSetupGuideUrl(): String = SETUP_GUIDE_URL
    
    /**
     * Print API key setup instructions
     */
    fun printSetupInstructions() {
        println("""
            |
            |╔════════════════════════════════════════════════════════════════╗
            |║           OpenAI API Key Setup Instructions                    ║
            |╚════════════════════════════════════════════════════════════════╝
            |
            |To use KRefactorAI, you need to set your OpenAI API key as an
            |environment variable.
            |
            |📋 Quick Setup:
            |
            |  macOS/Linux (Bash/Zsh):
            |    export OPENAI_API_KEY="your-api-key-here"
            |
            |  Windows (PowerShell):
            |    ${'$'}env:OPENAI_API_KEY="your-api-key-here"
            |
            |  Windows (Command Prompt):
            |    setx OPENAI_API_KEY "your-api-key-here"
            |
            |🔑 Get Your API Key:
            |  1. Visit: https://platform.openai.com/
            |  2. Sign up or log in
            |  3. Go to API Keys section
            |  4. Create a new secret key
            |  5. Copy the key (you won't be able to see it again!)
            |
            |📖 Detailed Guide:
            |  $SETUP_GUIDE_URL
            |
            |⚠️  Security Note:
            |  - Never commit your API key to version control
            |  - Add .env files to .gitignore
            |  - Use environment variables only
            |
        """.trimMargin())
    }
    
    /**
     * Mask API key for logging (show only first 10 characters)
     */
    fun maskApiKey(apiKey: String): String {
        return if (apiKey.length > 10) {
            "${apiKey.take(10)}...${apiKey.takeLast(4)}"
        } else {
            "***"
        }
    }
}

/**
 * API key status
 */
enum class ApiKeyStatus {
    NOT_SET,
    INVALID_FORMAT,
    VALID;
    
    val isValid: Boolean
        get() = this == VALID
    
    val message: String
        get() = when (this) {
            NOT_SET -> "API key is not set in environment variables"
            INVALID_FORMAT -> "API key format is invalid"
            VALID -> "API key is valid"
        }
}

