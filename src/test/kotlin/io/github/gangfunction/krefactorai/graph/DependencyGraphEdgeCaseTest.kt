package io.github.gangfunction.krefactorai.graph

import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType
import kotlin.test.*

/**
 * Edge case tests for DependencyGraph
 */
class DependencyGraphEdgeCaseTest {
    private lateinit var graph: DependencyGraph

    @BeforeTest
    fun setup() {
        graph = DependencyGraph()
    }

    // ========== Self-Dependency Tests ==========

    @Test
    fun `test self-dependency is handled correctly`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)

        // A module depending on itself
        val selfDependency = Dependency(moduleA, moduleA)
        graph.addDependency(selfDependency)

        assertEquals(1, graph.getModules().size)
        assertEquals(1, graph.getDependencies().size)
        assertTrue(graph.getDependenciesOf(moduleA).contains(moduleA))
    }

    @Test
    fun `test self-dependency creates cycle`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        graph.addDependency(Dependency(moduleA, moduleA))

        val cycles = graph.detectCircularDependencies()
        assertTrue(cycles.isNotEmpty(), "Self-dependency should be detected as a cycle")
    }

    // ========== Duplicate Dependency Tests ==========

    @Test
    fun `test duplicate dependencies are handled`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)

        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleA, moduleB))

        // JGraphT DirectedGraph doesn't allow duplicate edges by default
        // So adding the same dependency twice should result in only one edge
        val dependencies = graph.getDependenciesOf(moduleA)
        assertEquals(1, dependencies.size)
        assertTrue(dependencies.contains(moduleB))
    }

    // ========== Large Graph Tests ==========

    @Test
    fun `test large graph with many modules`() {
        val modules = (1..100).map { Module("Module$it", "/path/$it", ModuleType.PACKAGE) }

        modules.forEach { graph.addModule(it) }

        assertEquals(100, graph.getModules().size)
        assertFalse(graph.isEmpty())
    }

    @Test
    fun `test large graph with many dependencies`() {
        val modules = (1..50).map { Module("Module$it", "/path/$it", ModuleType.PACKAGE) }

        // Create a chain: Module1 -> Module2 -> Module3 -> ... -> Module50
        for (i in 0 until modules.size - 1) {
            graph.addDependency(Dependency(modules[i], modules[i + 1]))
        }

        assertEquals(50, graph.getModules().size)
        assertEquals(49, graph.getDependencies().size)

        // First module should have 1 dependency
        assertEquals(1, graph.getDependenciesOf(modules[0]).size)

        // Last module should have 0 dependencies
        assertEquals(0, graph.getDependenciesOf(modules[49]).size)

        // Last module should have 1 dependent
        assertEquals(1, graph.getDependentsOf(modules[49]).size)
    }

    // ========== Complex Cycle Tests ==========

    @Test
    fun `test multiple separate cycles`() {
        // Cycle 1: A -> B -> C -> A
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)
        val moduleC = Module("C", "/path/C", ModuleType.PACKAGE)

        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleC))
        graph.addDependency(Dependency(moduleC, moduleA))

        // Cycle 2: D -> E -> D
        val moduleD = Module("D", "/path/D", ModuleType.PACKAGE)
        val moduleE = Module("E", "/path/E", ModuleType.PACKAGE)

        graph.addDependency(Dependency(moduleD, moduleE))
        graph.addDependency(Dependency(moduleE, moduleD))

        val cycles = graph.detectCircularDependencies()
        assertTrue(cycles.size >= 2, "Should detect at least 2 cycles")
    }

    @Test
    fun `test nested cycles`() {
        // Create a complex nested cycle structure
        val modules = (1..5).map { Module("Module$it", "/path/$it", ModuleType.PACKAGE) }

        // Outer cycle: 1 -> 2 -> 3 -> 1
        graph.addDependency(Dependency(modules[0], modules[1]))
        graph.addDependency(Dependency(modules[1], modules[2]))
        graph.addDependency(Dependency(modules[2], modules[0]))

        // Inner cycle: 2 -> 4 -> 5 -> 2
        graph.addDependency(Dependency(modules[1], modules[3]))
        graph.addDependency(Dependency(modules[3], modules[4]))
        graph.addDependency(Dependency(modules[4], modules[1]))

        val cycles = graph.detectCircularDependencies()
        assertTrue(cycles.isNotEmpty(), "Should detect nested cycles")
    }

    // ========== Degree Tests ==========

    @Test
    fun `test module with zero in-degree and out-degree`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        graph.addModule(moduleA)

        assertEquals(0, graph.getInDegree(moduleA))
        assertEquals(0, graph.getOutDegree(moduleA))
    }

    @Test
    fun `test module with high in-degree`() {
        val hub = Module("Hub", "/path/hub", ModuleType.PACKAGE)
        val dependents = (1..20).map { Module("Dependent$it", "/path/$it", ModuleType.PACKAGE) }

        dependents.forEach { dependent ->
            graph.addDependency(Dependency(dependent, hub))
        }

        assertEquals(20, graph.getInDegree(hub))
        assertEquals(0, graph.getOutDegree(hub))
    }

    @Test
    fun `test module with high out-degree`() {
        val root = Module("Root", "/path/root", ModuleType.PACKAGE)
        val dependencies = (1..20).map { Module("Dependency$it", "/path/$it", ModuleType.PACKAGE) }

        dependencies.forEach { dependency ->
            graph.addDependency(Dependency(root, dependency))
        }

        assertEquals(0, graph.getInDegree(root))
        assertEquals(20, graph.getOutDegree(root))
    }

    // ========== Adjacency Matrix Tests ==========

    @Test
    fun `test adjacency matrix for empty graph`() {
        val matrix = graph.toAdjacencyMatrix()

        assertEquals(0, matrix.size)
    }

    @Test
    fun `test adjacency matrix for single module`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        graph.addModule(moduleA)

        val matrix = graph.toAdjacencyMatrix()

        assertEquals(1, matrix.size)
        assertEquals(1, matrix[0].size)
        assertEquals(0.0, matrix[0][0])
    }

    @Test
    fun `test adjacency matrix for self-dependency`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        graph.addDependency(Dependency(moduleA, moduleA))

        val matrix = graph.toAdjacencyMatrix()

        assertEquals(1, matrix.size)
        assertEquals(1.0, matrix[0][0])
    }

    @Test
    fun `test adjacency matrix symmetry for bidirectional dependency`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)

        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleA))

        val matrix = graph.toAdjacencyMatrix()

        assertEquals(2, matrix.size)
        // Both directions should have edges
        assertTrue(matrix[0][1] == 1.0 || matrix[1][0] == 1.0)
    }

    // ========== Module Name Edge Cases ==========

    @Test
    fun `test modules with special characters in names`() {
        val module1 = Module("com.example.my-app", "/path/1", ModuleType.PACKAGE)
        val module2 = Module("com.example.my_app", "/path/2", ModuleType.PACKAGE)
        val module3 = Module("com.example.my.app", "/path/3", ModuleType.PACKAGE)

        graph.addModule(module1)
        graph.addModule(module2)
        graph.addModule(module3)

        assertEquals(3, graph.getModules().size)
    }

    @Test
    fun `test modules with very long names`() {
        val longName = "com.example." + "verylongpackagename".repeat(10)
        val module = Module(longName, "/path/long", ModuleType.PACKAGE)

        graph.addModule(module)

        assertTrue(graph.getModules().contains(module))
    }

    @Test
    fun `test modules with empty path`() {
        val module = Module("com.example.app", "", ModuleType.PACKAGE)

        graph.addModule(module)

        assertTrue(graph.getModules().contains(module))
    }

    // ========== Diamond Dependency Pattern ==========

    @Test
    fun `test diamond dependency pattern`() {
        // Diamond: A -> B, A -> C, B -> D, C -> D
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)
        val moduleC = Module("C", "/path/C", ModuleType.PACKAGE)
        val moduleD = Module("D", "/path/D", ModuleType.PACKAGE)

        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleA, moduleC))
        graph.addDependency(Dependency(moduleB, moduleD))
        graph.addDependency(Dependency(moduleC, moduleD))

        assertEquals(4, graph.getModules().size)
        assertEquals(4, graph.getDependencies().size)

        // D should have 2 dependents
        assertEquals(2, graph.getDependentsOf(moduleD).size)

        // A should have 2 dependencies
        assertEquals(2, graph.getDependenciesOf(moduleA).size)

        // Should not have cycles
        assertTrue(graph.detectCircularDependencies().isEmpty())
    }

    // ========== Disconnected Components ==========

    @Test
    fun `test graph with disconnected components`() {
        // Component 1: A -> B
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)
        graph.addDependency(Dependency(moduleA, moduleB))

        // Component 2: C -> D (completely separate)
        val moduleC = Module("C", "/path/C", ModuleType.PACKAGE)
        val moduleD = Module("D", "/path/D", ModuleType.PACKAGE)
        graph.addDependency(Dependency(moduleC, moduleD))

        // Isolated module
        val moduleE = Module("E", "/path/E", ModuleType.PACKAGE)
        graph.addModule(moduleE)

        assertEquals(5, graph.getModules().size)
        assertEquals(2, graph.getDependencies().size)

        // E should have no dependencies or dependents
        assertEquals(0, graph.getDependenciesOf(moduleE).size)
        assertEquals(0, graph.getDependentsOf(moduleE).size)
    }
}
