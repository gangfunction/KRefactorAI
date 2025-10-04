package io.github.gangfunction.krefactorai

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Edge case tests for KRefactorAI main API
 */
class KRefactorAIEdgeCaseTest {
    // ========== Initialization Edge Cases ==========

    @Test
    fun `test initialization with AI disabled`() {
        val refactorAI = KRefactorAI(enableAI = false)

        assertNotNull(refactorAI)
    }

    @Test
    fun `test initialization with AI enabled but no API key`() {
        // Should not throw exception, just log warning
        val refactorAI = KRefactorAI(enableAI = true)

        assertNotNull(refactorAI)
    }

    @Test
    fun `test initialization with custom AI model`() {
        val refactorAI = KRefactorAI(enableAI = false, aiModel = "gpt-3.5-turbo")

        assertNotNull(refactorAI)
    }

    // ========== Empty Graph Analysis ==========

    @Test
    fun `test analyze empty graph`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(0, plan.modules.size)
        assertEquals(0.0, plan.totalComplexity)
        assertEquals("0 minutes", plan.estimatedTime)
        assertTrue(plan.circularDependencies.isEmpty())
    }

    @Test
    fun `test quick analyze empty graph`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()

        val plan = refactorAI.quickAnalyze(graph)

        assertNotNull(plan)
        assertEquals(0, plan.modules.size)
    }

    // ========== Single Module Analysis ==========

    @Test
    fun `test analyze single module`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val module = Module("com.example.single", "/path/to/single")
        graph.addModule(module)

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(1, plan.modules.size)
        assertEquals(module, plan.modules[0].module)
    }

    @Test
    fun `test analyze single module with self-dependency`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val module = Module("com.example.self", "/path/to/self")
        graph.addModule(module)
        graph.addDependency(Dependency(module, module))

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(1, plan.modules.size)
        assertTrue(plan.circularDependencies.isNotEmpty())
    }

    // ========== AI Suggestions Edge Cases ==========

    @Test
    fun `test analyze with AI disabled explicitly`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val module = Module("com.example.test", "/path/to/test")
        graph.addModule(module)

        val plan = refactorAI.analyze(graph, includeAISuggestions = false)

        assertNotNull(plan)
        assertEquals(1, plan.modules.size)
        // AI suggestions should be null when disabled
        plan.modules.forEach { step ->
            assertTrue(step.aiSuggestion == null || step.aiSuggestion!!.isEmpty())
        }
    }

    @Test
    fun `test quick analyze never includes AI suggestions`() {
        val refactorAI = KRefactorAI(enableAI = true)
        val graph = DependencyGraph()
        val module = Module("com.example.test", "/path/to/test")
        graph.addModule(module)

        val plan = refactorAI.quickAnalyze(graph)

        assertNotNull(plan)
        // Quick analyze should not include AI suggestions
        plan.modules.forEach { step ->
            assertTrue(step.aiSuggestion == null || step.aiSuggestion!!.isEmpty())
        }
    }

    // ========== Circular Dependency Analysis ==========

    @Test
    fun `test analyze with simple circular dependency`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")

        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleA))

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(2, plan.modules.size)
        assertTrue(plan.circularDependencies.isNotEmpty())
    }

    @Test
    fun `test analyze with multiple circular dependencies`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()

        // First cycle: A -> B -> A
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")
        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleA))

        // Second cycle: C -> D -> C
        val moduleC = Module("com.example.c", "/path/to/c")
        val moduleD = Module("com.example.d", "/path/to/d")
        graph.addModule(moduleC)
        graph.addModule(moduleD)
        graph.addDependency(Dependency(moduleC, moduleD))
        graph.addDependency(Dependency(moduleD, moduleC))

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertTrue(plan.circularDependencies.size >= 2)
    }

    // ========== Large Graph Analysis ==========

    @Test
    fun `test analyze large graph with many modules`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val modules = (1..50).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create linear dependencies
        for (i in 0 until modules.size - 1) {
            graph.addDependency(Dependency(modules[i], modules[i + 1]))
        }

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(50, plan.modules.size)
        assertTrue(plan.totalComplexity > 0.0)
    }

    @Test
    fun `test analyze large graph with complex dependencies`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val modules = (1..30).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create complex dependency structure
        for (i in 0 until modules.size - 1) {
            for (j in i + 1 until minOf(i + 5, modules.size)) {
                graph.addDependency(Dependency(modules[i], modules[j]))
            }
        }

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(30, plan.modules.size)
    }

    // ========== Disconnected Components ==========

    @Test
    fun `test analyze graph with disconnected components`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()

        // Component 1: A -> B
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")
        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addDependency(Dependency(moduleA, moduleB))

        // Component 2: C -> D (disconnected)
        val moduleC = Module("com.example.c", "/path/to/c")
        val moduleD = Module("com.example.d", "/path/to/d")
        graph.addModule(moduleC)
        graph.addModule(moduleD)
        graph.addDependency(Dependency(moduleC, moduleD))

        // Isolated module E
        val moduleE = Module("com.example.e", "/path/to/e")
        graph.addModule(moduleE)

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(5, plan.modules.size)
    }

    // ========== Diamond Dependency Pattern ==========

    @Test
    fun `test analyze diamond dependency pattern`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()

        //     A
        //    / \
        //   B   C
        //    \ /
        //     D
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")
        val moduleC = Module("com.example.c", "/path/to/c")
        val moduleD = Module("com.example.d", "/path/to/d")

        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addModule(moduleC)
        graph.addModule(moduleD)

        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleA, moduleC))
        graph.addDependency(Dependency(moduleB, moduleD))
        graph.addDependency(Dependency(moduleC, moduleD))

        val plan = refactorAI.analyze(graph)

        assertNotNull(plan)
        assertEquals(4, plan.modules.size)
        assertTrue(plan.circularDependencies.isEmpty())
    }

    // ========== Plan Output Validation ==========

    @Test
    fun `test plan toString output is not empty`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val module = Module("com.example.test", "/path/to/test")
        graph.addModule(module)

        val plan = refactorAI.analyze(graph)
        val output = plan.toString()

        assertNotNull(output)
        assertTrue(output.isNotEmpty())
        assertTrue(output.contains("Refactoring Plan"))
    }

    @Test
    fun `test plan contains all expected information`() {
        val refactorAI = KRefactorAI(enableAI = false)
        val graph = DependencyGraph()
        val modules = (1..5).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        val plan = refactorAI.analyze(graph)
        val output = plan.toString()

        assertTrue(output.contains("Total Modules"))
        assertTrue(output.contains("Total Complexity"))
        assertTrue(output.contains("Estimated Time"))
        assertTrue(output.contains("Steps"))
    }

    // ========== Info Method ==========

    @Test
    fun `test getInfo returns library information`() {
        val info = KRefactorAI.getInfo()

        assertNotNull(info)
        assertTrue(info.isNotEmpty())
    }
}

