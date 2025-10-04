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

        // Initialize out-degree for all modules (number of dependencies)
        modules.forEach { module ->
            inDegree[module] = graph.getOutDegree(module)
        }

        // Find all modules with out-degree 0 (no dependencies)
        val queue = ArrayDeque<Module>()
        inDegree.filter { it.value == 0 }.forEach { (module, _) ->
            queue.add(module)
        }

        // Process modules in topological order
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            result.add(current)

            // Get all modules that have current as a dependency
            // These are the modules that depend ON current
            val allModules = graph.getModules()
            allModules.forEach { module ->
                val deps = graph.getDependenciesOf(module)
                if (current in deps) {
                    val newDegree = (inDegree[module] ?: 0) - 1
                    inDegree[module] = newDegree

                    if (newDegree == 0 && module !in result) {
                        queue.add(module)
                    }
                }
            }
        }

        // If not all modules are processed, there's a cycle (shouldn't happen as we checked earlier)
        val totalModules = graph.getModules().size
        if (result.size != totalModules) {
            logger.error { "Topological sort failed: processed ${result.size} out of $totalModules modules" }
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

        // Initialize out-degree for all modules (number of dependencies)
        modules.forEach { module ->
            inDegree[module] = graph.getOutDegree(module)
        }

        // Use a priority queue based on complexity scores
        val queue =
            java.util.PriorityQueue<Module>(
                compareByDescending { complexityScores[it] ?: 0.0 },
            )

        // Find all modules with out-degree 0 (no dependencies)
        inDegree.filter { it.value == 0 }.forEach { (module, _) ->
            queue.add(module)
        }

        // Process modules in topological order with priority
        while (queue.isNotEmpty()) {
            val current = queue.poll()
            result.add(current)

            // Get all modules that have current as a dependency
            val allModules = graph.getModules()
            allModules.forEach { module ->
                val deps = graph.getDependenciesOf(module)
                if (current in deps) {
                    val newDegree = (inDegree[module] ?: 0) - 1
                    inDegree[module] = newDegree

                    if (newDegree == 0 && module !in result) {
                        queue.add(module)
                    }
                }
            }
        }

        // Verify all modules are processed
        val totalModules = graph.getModules().size
        if (result.size != totalModules) {
            logger.error { "Priority topological sort failed: processed ${result.size} out of $totalModules modules" }
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

        // Initialize out-degree (number of dependencies)
        graph.getModules().forEach { module ->
            inDegree[module] = graph.getOutDegree(module)
        }

        while (processed.size < graph.size()) {
            // Find all modules with in-degree 0 (considering already processed modules)
            val currentLayer =
                inDegree
                    .filter { (module, degree) -> degree == 0 && module !in processed }
                    .keys
                    .toSet()

            if (currentLayer.isEmpty()) {
                logger.error { "Failed to create layers: no modules with in-degree 0 found" }
                return null
            }

            layers.add(currentLayer)
            processed.addAll(currentLayer)

            // Update in-degrees for modules that depend on current layer
            currentLayer.forEach { current ->
                graph.getModules().forEach { module ->
                    if (module !in processed && current in graph.getDependenciesOf(module)) {
                        inDegree[module] = (inDegree[module] ?: 0) - 1
                    }
                }
            }
        }

        logger.info { "Created ${layers.size} layers for parallel refactoring" }
        return layers
    }
}
