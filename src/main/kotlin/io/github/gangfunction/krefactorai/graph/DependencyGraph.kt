package io.github.gangfunction.krefactorai.graph

import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import org.jgrapht.Graph
import org.jgrapht.alg.connectivity.ConnectivityInspector
import org.jgrapht.alg.cycle.CycleDetector
import org.jgrapht.graph.DefaultDirectedGraph
import org.jgrapht.graph.DefaultEdge

/**
 * Represents a dependency graph using JGraphT
 */
class DependencyGraph {
    private val graph: Graph<Module, DefaultEdge> = DefaultDirectedGraph(DefaultEdge::class.java)
    private val dependencies = mutableListOf<Dependency>()

    /**
     * Add a module to the graph
     */
    fun addModule(module: Module) {
        graph.addVertex(module)
    }

    /**
     * Add a dependency between two modules
     */
    fun addDependency(dependency: Dependency) {
        // Ensure both modules exist in the graph
        graph.addVertex(dependency.from)
        graph.addVertex(dependency.to)

        // Add edge
        graph.addEdge(dependency.from, dependency.to)
        dependencies.add(dependency)
    }

    /**
     * Get all modules in the graph
     */
    fun getModules(): Set<Module> = graph.vertexSet()

    /**
     * Get all dependencies
     */
    fun getDependencies(): List<Dependency> = dependencies.toList()

    /**
     * Get dependencies of a specific module (modules that this module depends on)
     */
    fun getDependenciesOf(module: Module): Set<Module> {
        return graph.outgoingEdgesOf(module).map { edge ->
            graph.getEdgeTarget(edge)
        }.toSet()
    }

    /**
     * Get dependents of a specific module (modules that depend on this module)
     */
    fun getDependentsOf(module: Module): Set<Module> {
        return graph.incomingEdgesOf(module).map { edge ->
            graph.getEdgeSource(edge)
        }.toSet()
    }

    /**
     * Detect circular dependencies
     */
    fun detectCircularDependencies(): List<List<Module>> {
        val cycleDetector = CycleDetector(graph)

        if (!cycleDetector.detectCycles()) {
            return emptyList()
        }

        val cycles = mutableListOf<List<Module>>()
        val cycleVertices = cycleDetector.findCycles()

        // Group vertices into cycles
        cycleVertices.forEach { vertex ->
            val cycle = findCycleContaining(vertex)
            if (cycle.isNotEmpty() && !cycles.any { it.toSet() == cycle.toSet() }) {
                cycles.add(cycle)
            }
        }

        return cycles
    }

    /**
     * Find a cycle containing the given module
     */
    private fun findCycleContaining(start: Module): List<Module> {
        val visited = mutableSetOf<Module>()
        val path = mutableListOf<Module>()

        fun dfs(current: Module): Boolean {
            if (current in path) {
                // Found a cycle
                val cycleStart = path.indexOf(current)
                return true
            }

            if (current in visited) {
                return false
            }

            visited.add(current)
            path.add(current)

            for (neighbor in getDependenciesOf(current)) {
                if (dfs(neighbor)) {
                    return true
                }
            }

            path.removeAt(path.lastIndex)
            return false
        }

        dfs(start)
        return if (start in path) {
            val cycleStart = path.indexOf(start)
            path.subList(cycleStart, path.size)
        } else {
            emptyList()
        }
    }

    /**
     * Convert graph to adjacency matrix
     */
    fun toAdjacencyMatrix(): Array<DoubleArray> {
        val modules = getModules().toList()
        val size = modules.size
        val matrix = Array(size) { DoubleArray(size) { 0.0 } }

        modules.forEachIndexed { i, from ->
            modules.forEachIndexed { j, to ->
                if (graph.containsEdge(from, to)) {
                    // Find the dependency weight
                    val dependency = dependencies.find { it.from == from && it.to == to }
                    matrix[i][j] = dependency?.weight ?: 1.0
                }
            }
        }

        return matrix
    }

    /**
     * Get module index mapping for matrix operations
     */
    fun getModuleIndexMap(): Map<Module, Int> {
        return getModules().toList().withIndex().associate { it.value to it.index }
    }

    /**
     * Calculate in-degree (number of modules depending on this module)
     */
    fun getInDegree(module: Module): Int {
        return graph.inDegreeOf(module)
    }

    /**
     * Calculate out-degree (number of modules this module depends on)
     */
    fun getOutDegree(module: Module): Int {
        return graph.outDegreeOf(module)
    }

    /**
     * Check if the graph is connected
     */
    fun isConnected(): Boolean {
        val inspector = ConnectivityInspector(graph)
        return inspector.isConnected
    }

    /**
     * Get strongly connected components
     */
    fun getStronglyConnectedComponents(): List<Set<Module>> {
        val inspector = ConnectivityInspector(graph)
        return inspector.connectedSets().toList()
    }

    /**
     * Get the size of the graph
     */
    fun size(): Int = graph.vertexSet().size

    /**
     * Check if graph is empty
     */
    fun isEmpty(): Boolean = graph.vertexSet().isEmpty()

    override fun toString(): String =
        buildString {
            appendLine("DependencyGraph:")
            appendLine("  Modules: ${graph.vertexSet().size}")
            appendLine("  Dependencies: ${graph.edgeSet().size}")
            appendLine("  Circular Dependencies: ${detectCircularDependencies().size}")
        }
}
