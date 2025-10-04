package io.github.gangfunction.krefactorai.model

import kotlinx.serialization.Serializable

/**
 * Represents a complete refactoring plan with ordered steps
 */
@Serializable
data class RefactoringPlan(
    val modules: List<RefactoringStep>,
    val circularDependencies: List<List<Module>>,
    val totalComplexity: Double,
    val estimatedTime: String,
) {
    override fun toString(): String =
        buildString {
            appendLine("=== Refactoring Plan ===")
            appendLine("Total Modules: ${modules.size}")
            appendLine("Circular Dependencies: ${circularDependencies.size}")
            appendLine("Total Complexity: ${"%.2f".format(totalComplexity)}")
            appendLine("Estimated Time: $estimatedTime")
            appendLine()
            appendLine("Steps:")
            modules.forEachIndexed { index, step ->
                val complexityFormatted = "%.2f".format(step.complexityScore)
                val stepInfo = "${index + 1}. ${step.module.name} " +
                    "(priority=${step.priority}, complexity=$complexityFormatted)"
                appendLine(stepInfo)
            }
        }
}

/**
 * Represents a single step in the refactoring plan
 */
@Serializable
data class RefactoringStep(
    val module: Module,
    val priority: Int,
    val complexityScore: Double,
    val dependencies: List<Module>,
    val dependents: List<Module>,
    val aiSuggestion: String? = null,
) {
    override fun toString(): String =
        buildString {
            appendLine("Module: ${module.name}")
            appendLine("Priority: $priority")
            appendLine("Complexity: ${"%.2f".format(complexityScore)}")
            appendLine("Dependencies: ${dependencies.joinToString(", ") { it.name }}")
            appendLine("Dependents: ${dependents.joinToString(", ") { it.name }}")
            if (aiSuggestion != null) {
                appendLine("AI Suggestion: $aiSuggestion")
            }
        }
}
