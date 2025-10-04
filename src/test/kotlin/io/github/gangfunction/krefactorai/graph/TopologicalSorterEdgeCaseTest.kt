package io.github.gangfunction.krefactorai.graph

import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType
import kotlin.test.*

class TopologicalSorterEdgeCaseTest {
    private lateinit var graph: DependencyGraph
    private lateinit var sorter: TopologicalSorter

    @BeforeTest
    fun setup() {
        graph = DependencyGraph()
        sorter = TopologicalSorter(graph)
    }

    @Test
    fun `test sort empty graph`() {
        val result = sorter.sort()
        assertNotNull(result)
        assertTrue(result!!.isEmpty())
    }

    @Test
    fun `test sort single module`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        graph.addModule(moduleA)
        val result = sorter.sort()
        assertNotNull(result)
        assertEquals(1, result!!.size)
    }

    @Test
    fun `test sort with cycle returns null`() {
        val moduleA = Module("A", "/path/A", ModuleType.PACKAGE)
        val moduleB = Module("B", "/path/B", ModuleType.PACKAGE)
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleA))
        val result = sorter.sort()
        assertNull(result)
    }

    @Test
    fun `test sort linear chain`() {
        val modules = (1..10).map { Module("M$it", "/path/$it", ModuleType.PACKAGE) }
        for (i in 0 until modules.size - 1) {
            graph.addDependency(Dependency(modules[i], modules[i + 1]))
        }
        val result = sorter.sort()
        assertNotNull(result)
        assertEquals(10, result!!.size)
    }
}
