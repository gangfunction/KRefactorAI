package io.github.gangfunction.krefactorai.graph

import io.github.gangfunction.krefactorai.model.Module
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Performs topological sorting on a dependency graph
 */
class TopologicalSorter(private val graph: DependencyGraph) {

    /**
     * Perform topological sort using Kahn's algorithm
     * Returns null if the graph contains cycles
     */
    fun sort(): List<Module>? {
        if (graph.isEmpty()) {
            return emptyList()
        }

        // Check for cycles first
        val cycles = graph.detectCircularDependencies()
        if (cycles.isNotEmpty()) {
            logger.warn { "Graph contains ${cycles.size} circular dependencies. Cannot perform topological sort." }
            return null
        }

        val modules = graph.getModules().toMutableSet()
        val result = mutableListOf<Module>()
        val inDegree = mutableMapOf<Module, Int>()

        // Initialize in-degree for all modules
        modules.forEach { module ->
            inDegree[module] = graph.getInDegree(module)
        }

        // Find all modules with in-degree 0 (no dependencies)
        val queue = ArrayDeque<Module>()
        inDegree.filter { it.value == 0 }.forEach { (module, _) ->
            queue.add(module)
        }

        // Process modules in topological order
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            result.add(current)

            // Reduce in-degree of dependent modules
            graph.getDependentsOf(current).forEach { dependent ->
                val newDegree = (inDegree[dependent] ?: 0) - 1
                inDegree[dependent] = newDegree

                if (newDegree == 0) {
                    queue.add(dependent)
                }
            }
        }

        // If not all modules are processed, there's a cycle (shouldn't happen as we checked earlier)
        if (result.size != modules.size) {
            logger.error { "Topological sort failed: processed ${result.size} out of ${modules.size} modules" }
            return null
        }

        logger.info { "Topological sort completed: ${result.size} modules ordered" }
        return result
    }

    /**
     * Perform topological sort with priority based on complexity
     * Modules with higher complexity are prioritized when there are multiple choices
     */
    fun sortWithPriority(complexityScores: Map<Module, Double>): List<Module>? {
        if (graph.isEmpty()) {
            return emptyList()
        }

        // Check for cycles first
        val cycles = graph.detectCircularDependencies()
        if (cycles.isNotEmpty()) {
            logger.warn { "Graph contains ${cycles.size} circular dependencies. Cannot perform topological sort." }
            return null
        }

        val modules = graph.getModules().toMutableSet()
        val result = mutableListOf<Module>()
        val inDegree = mutableMapOf<Module, Int>()

        // Initialize in-degree for all modules
        modules.forEach { module ->
            inDegree[module] = graph.getInDegree(module)
        }

        // Use a priority queue based on complexity scores
        val queue = java.util.PriorityQueue<Module>(
            compareByDescending { complexityScores[it] ?: 0.0 }
        )

        // Find all modules with in-degree 0
        inDegree.filter { it.value == 0 }.forEach { (module, _) ->
            queue.add(module)
        }

        // Process modules in topological order with priority
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            result.add(current)

            // Reduce in-degree of dependent modules
            graph.getDependentsOf(current).forEach { dependent ->
                val newDegree = (inDegree[dependent] ?: 0) - 1
                inDegree[dependent] = newDegree

                if (newDegree == 0) {
                    queue.add(dependent)
                }
            }
        }

        // Verify all modules are processed
        if (result.size != modules.size) {
            logger.error { "Priority topological sort failed: processed ${result.size} out of ${modules.size} modules" }
            return null
        }

        logger.info { "Priority topological sort completed: ${result.size} modules ordered" }
        return result
    }

    /**
     * Get layers of modules that can be refactored in parallel
     * Each layer contains modules with no dependencies on each other
     */
    fun getLayers(): List<Set<Module>>? {
        if (graph.isEmpty()) {
            return emptyList()
        }

        // Check for cycles
        val cycles = graph.detectCircularDependencies()
        if (cycles.isNotEmpty()) {
            logger.warn { "Graph contains cycles. Cannot create layers." }
            return null
        }

        val layers = mutableListOf<Set<Module>>()
        val processed = mutableSetOf<Module>()
        val inDegree = mutableMapOf<Module, Int>()

        // Initialize in-degree
        graph.getModules().forEach { module ->
            inDegree[module] = graph.getInDegree(module)
        }

        while (processed.size < graph.size()) {
            // Find all modules with in-degree 0 (considering already processed modules)
            val currentLayer = inDegree
                .filter { (module, degree) -> degree == 0 && module !in processed }
                .keys
                .toSet()

            if (currentLayer.isEmpty()) {
                logger.error { "Failed to create layers: no modules with in-degree 0 found" }
                return null
            }

            layers.add(currentLayer)
            processed.addAll(currentLayer)

            // Update in-degrees
            currentLayer.forEach { module ->
                graph.getDependentsOf(module).forEach { dependent ->
                    if (dependent !in processed) {
                        inDegree[dependent] = (inDegree[dependent] ?: 0) - 1
                    }
                }
            }
        }

        logger.info { "Created ${layers.size} layers for parallel refactoring" }
        return layers
    }
}

