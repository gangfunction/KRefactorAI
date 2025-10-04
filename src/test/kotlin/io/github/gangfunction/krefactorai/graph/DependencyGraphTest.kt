package io.github.gangfunction.krefactorai.graph

import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType
import kotlin.test.*

class DependencyGraphTest {

    private lateinit var graph: DependencyGraph
    private lateinit var moduleA: Module
    private lateinit var moduleB: Module
    private lateinit var moduleC: Module

    @BeforeTest
    fun setup() {
        graph = DependencyGraph()
        moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        moduleB = Module("B", "/path/B", ModuleType.PACKAGE)
        moduleC = Module("C", "/path/C", ModuleType.PACKAGE)
    }

    @Test
    fun `test add module`() {
        graph.addModule(moduleA)
        
        assertTrue(graph.getModules().contains(moduleA))
        assertEquals(1, graph.size())
    }

    @Test
    fun `test add dependency`() {
        val dependency = Dependency(moduleA, moduleB)
        graph.addDependency(dependency)
        
        assertEquals(2, graph.getModules().size)
        assertEquals(1, graph.getDependencies().size)
        assertTrue(graph.getDependenciesOf(moduleA).contains(moduleB))
    }

    @Test
    fun `test get dependencies of module`() {
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleA, moduleC))
        
        val dependencies = graph.getDependenciesOf(moduleA)
        
        assertEquals(2, dependencies.size)
        assertTrue(dependencies.contains(moduleB))
        assertTrue(dependencies.contains(moduleC))
    }

    @Test
    fun `test get dependents of module`() {
        graph.addDependency(Dependency(moduleA, moduleC))
        graph.addDependency(Dependency(moduleB, moduleC))
        
        val dependents = graph.getDependentsOf(moduleC)
        
        assertEquals(2, dependents.size)
        assertTrue(dependents.contains(moduleA))
        assertTrue(dependents.contains(moduleB))
    }

    @Test
    fun `test detect circular dependencies`() {
        // Create a cycle: A -> B -> C -> A
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleC))
        graph.addDependency(Dependency(moduleC, moduleA))
        
        val cycles = graph.detectCircularDependencies()
        
        assertTrue(cycles.isNotEmpty())
    }

    @Test
    fun `test no circular dependencies`() {
        // Create a DAG: A -> B -> C
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleC))
        
        val cycles = graph.detectCircularDependencies()
        
        assertTrue(cycles.isEmpty())
    }

    @Test
    fun `test adjacency matrix`() {
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleC))
        
        val matrix = graph.toAdjacencyMatrix()
        
        assertEquals(3, matrix.size)
        assertEquals(3, matrix[0].size)
    }

    @Test
    fun `test in-degree and out-degree`() {
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleC, moduleB))
        
        assertEquals(0, graph.getInDegree(moduleA))
        assertEquals(1, graph.getOutDegree(moduleA))
        
        assertEquals(2, graph.getInDegree(moduleB))
        assertEquals(0, graph.getOutDegree(moduleB))
    }

    @Test
    fun `test empty graph`() {
        assertTrue(graph.isEmpty())
        assertEquals(0, graph.size())
    }
}

