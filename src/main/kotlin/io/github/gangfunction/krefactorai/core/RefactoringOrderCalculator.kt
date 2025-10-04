package io.github.gangfunction.krefactorai.core

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.graph.TopologicalSorter
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.RefactoringPlan
import io.github.gangfunction.krefactorai.model.RefactoringStep
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Calculates the optimal refactoring order using minimal polynomial and topological sorting
 */
class RefactoringOrderCalculator(
    private val graph: DependencyGraph,
    private val minimalPolynomial: MinimalPolynomial = MinimalPolynomial()
) {

    /**
     * Calculate the complete refactoring plan
     */
    fun calculateRefactoringOrder(): RefactoringPlan {
        logger.info { "Starting refactoring order calculation" }
        
        if (graph.isEmpty()) {
            logger.warn { "Empty dependency graph" }
            return RefactoringPlan(
                modules = emptyList(),
                circularDependencies = emptyList(),
                totalComplexity = 0.0,
                estimatedTime = "0 minutes"
            )
        }

        // Step 1: Detect circular dependencies
        val circularDeps = graph.detectCircularDependencies()
        if (circularDeps.isNotEmpty()) {
            logger.warn { "Found ${circularDeps.size} circular dependencies" }
            circularDeps.forEachIndexed { index, cycle ->
                logger.warn { "Cycle ${index + 1}: ${cycle.joinToString(" -> ") { it.name }}" }
            }
        }

        // Step 2: Calculate complexity scores using minimal polynomial approach
        val adjacencyMatrix = graph.toAdjacencyMatrix()
        val complexityScores = minimalPolynomial.calculateComplexityScores(adjacencyMatrix)
        val moduleIndexMap = graph.getModuleIndexMap()
        
        // Create complexity map
        val complexityMap = moduleIndexMap.entries.associate { (module, index) ->
            module to complexityScores.getOrElse(index) { 0.5 }
        }

        // Step 3: Perform topological sort with priority
        val sorter = TopologicalSorter(graph)
        val sortedModules = sorter.sortWithPriority(complexityMap)

        // Step 4: Create refactoring steps
        val steps = if (sortedModules != null) {
            createRefactoringSteps(sortedModules, complexityMap)
        } else {
            // If topological sort fails (due to cycles), create steps without ordering
            logger.warn { "Topological sort failed, creating unordered steps" }
            createUnorderedSteps(complexityMap)
        }

        // Step 5: Calculate total complexity and estimated time
        val totalComplexity = steps.sumOf { it.complexityScore }
        val estimatedTime = estimateTime(steps.size, totalComplexity)

        // Step 6: Analyze structure
        val structureAnalysis = minimalPolynomial.analyzeStructure(adjacencyMatrix)
        logger.info { structureAnalysis.toString() }

        logger.info { "Refactoring order calculation completed: ${steps.size} steps" }

        return RefactoringPlan(
            modules = steps,
            circularDependencies = circularDeps,
            totalComplexity = totalComplexity,
            estimatedTime = estimatedTime
        )
    }

    /**
     * Create refactoring steps from sorted modules
     */
    private fun createRefactoringSteps(
        sortedModules: List<Module>,
        complexityMap: Map<Module, Double>
    ): List<RefactoringStep> {
        return sortedModules.mapIndexed { index, module ->
            RefactoringStep(
                module = module,
                priority = index + 1,
                complexityScore = complexityMap[module] ?: 0.5,
                dependencies = graph.getDependenciesOf(module).toList(),
                dependents = graph.getDependentsOf(module).toList(),
                aiSuggestion = null // Will be filled by AI module
            )
        }
    }

    /**
     * Create unordered steps (when topological sort fails)
     */
    private fun createUnorderedSteps(
        complexityMap: Map<Module, Double>
    ): List<RefactoringStep> {
        return graph.getModules()
            .sortedByDescending { complexityMap[it] ?: 0.0 }
            .mapIndexed { index, module ->
                RefactoringStep(
                    module = module,
                    priority = index + 1,
                    complexityScore = complexityMap[module] ?: 0.5,
                    dependencies = graph.getDependenciesOf(module).toList(),
                    dependents = graph.getDependentsOf(module).toList(),
                    aiSuggestion = null
                )
            }
    }

    /**
     * Estimate time required for refactoring
     */
    private fun estimateTime(moduleCount: Int, totalComplexity: Double): String {
        // Base time: 20 minutes per module
        // Complexity multiplier: 0.5 to 2.0
        val baseMinutes = moduleCount * 20
        val complexityMultiplier = 0.5 + (totalComplexity / moduleCount).coerceIn(0.0, 1.5)
        val totalMinutes = (baseMinutes * complexityMultiplier).toInt()

        return when {
            totalMinutes < 60 -> "$totalMinutes minutes"
            totalMinutes < 480 -> {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                "${hours}h ${minutes}m"
            }
            else -> {
                val days = totalMinutes / 480
                val hours = (totalMinutes % 480) / 60
                "${days}d ${hours}h"
            }
        }
    }

    /**
     * Get refactoring layers (modules that can be refactored in parallel)
     */
    fun getRefactoringLayers(): List<Set<Module>>? {
        val sorter = TopologicalSorter(graph)
        return sorter.getLayers()
    }

    /**
     * Calculate priority score for a module
     * Higher score = higher priority for refactoring
     */
    fun calculatePriorityScore(module: Module): Double {
        val inDegree = graph.getInDegree(module).toDouble()
        val outDegree = graph.getOutDegree(module).toDouble()
        
        // Modules with many dependents and few dependencies have higher priority
        return inDegree * 2.0 - outDegree * 0.5
    }

    /**
     * Get recommended refactoring order as a simple list
     */
    fun getRecommendedOrder(): List<Module> {
        val plan = calculateRefactoringOrder()
        return plan.modules.map { it.module }
    }

    /**
     * Get modules grouped by complexity level
     */
    fun getModulesByComplexity(): Map<ComplexityLevel, List<Module>> {
        val adjacencyMatrix = graph.toAdjacencyMatrix()
        val complexityScores = minimalPolynomial.calculateComplexityScores(adjacencyMatrix)
        val moduleIndexMap = graph.getModuleIndexMap()

        val result = mutableMapOf<ComplexityLevel, MutableList<Module>>()
        ComplexityLevel.values().forEach { result[it] = mutableListOf() }

        moduleIndexMap.forEach { (module, index) ->
            val score = complexityScores.getOrElse(index) { 0.5 }
            val level = when {
                score < 0.3 -> ComplexityLevel.LOW
                score < 0.7 -> ComplexityLevel.MEDIUM
                else -> ComplexityLevel.HIGH
            }
            result[level]?.add(module)
        }

        return result
    }
}

/**
 * Complexity levels for modules
 */
enum class ComplexityLevel {
    LOW,
    MEDIUM,
    HIGH
}

