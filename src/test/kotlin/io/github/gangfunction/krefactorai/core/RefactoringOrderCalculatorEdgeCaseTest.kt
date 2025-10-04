package io.github.gangfunction.krefactorai.core

import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Edge case tests for RefactoringOrderCalculator
 */
class RefactoringOrderCalculatorEdgeCaseTest {
    // ========== Empty Graph Edge Cases ==========

    @Test
    fun `test calculate order with empty graph`() {
        val graph = DependencyGraph()
        val calculator = RefactoringOrderCalculator(graph)

        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(0, plan.modules.size)
        assertEquals(0.0, plan.totalComplexity)
        assertEquals("0 minutes", plan.estimatedTime)
        assertTrue(plan.circularDependencies.isEmpty())
    }

    // ========== Single Module Edge Cases ==========

    @Test
    fun `test calculate order with single module no dependencies`() {
        val graph = DependencyGraph()
        val module = Module("com.example.single", "/path/to/single")
        graph.addModule(module)

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(1, plan.modules.size)
        assertEquals(module, plan.modules[0].module)
        assertTrue(plan.circularDependencies.isEmpty())
    }

    @Test
    fun `test calculate order with single module self-dependency`() {
        val graph = DependencyGraph()
        val module = Module("com.example.self", "/path/to/self")
        graph.addModule(module)
        graph.addDependency(Dependency(module, module))

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(1, plan.modules.size)
        // Self-dependency should be detected as circular
        assertTrue(plan.circularDependencies.isNotEmpty())
    }

    // ========== Circular Dependency Edge Cases ==========

    @Test
    fun `test calculate order with simple circular dependency`() {
        val graph = DependencyGraph()
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")

        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addDependency(Dependency(moduleA, moduleB))
        graph.addDependency(Dependency(moduleB, moduleA))

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(2, plan.modules.size)
        assertTrue(plan.circularDependencies.isNotEmpty())
    }

    @Test
    fun `test calculate order with complex circular dependency chain`() {
        val graph = DependencyGraph()
        val modules = (1..5).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create circular chain: 1 -> 2 -> 3 -> 4 -> 5 -> 1
        for (i in 0 until modules.size) {
            val from = modules[i]
            val to = modules[(i + 1) % modules.size]
            graph.addDependency(Dependency(from, to))
        }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(5, plan.modules.size)
        assertTrue(plan.circularDependencies.isNotEmpty())
    }

    @Test
    fun `test calculate order with multiple separate circular dependencies`() {
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

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(4, plan.modules.size)
        assertTrue(plan.circularDependencies.size >= 2)
    }

    // ========== Large Graph Edge Cases ==========

    @Test
    fun `test calculate order with large linear dependency chain`() {
        val graph = DependencyGraph()
        val modules = (1..50).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create linear chain: 1 -> 2 -> 3 -> ... -> 50
        for (i in 0 until modules.size - 1) {
            graph.addDependency(Dependency(modules[i], modules[i + 1]))
        }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(50, plan.modules.size)
        assertTrue(plan.circularDependencies.isEmpty())
    }

    @Test
    fun `test calculate order with large graph many dependencies`() {
        val graph = DependencyGraph()
        val modules = (1..30).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create complex dependency structure
        for (i in 0 until modules.size - 1) {
            for (j in i + 1 until minOf(i + 5, modules.size)) {
                graph.addDependency(Dependency(modules[i], modules[j]))
            }
        }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(30, plan.modules.size)
        assertTrue(plan.totalComplexity > 0.0)
    }

    // ========== Disconnected Components Edge Cases ==========

    @Test
    fun `test calculate order with disconnected components`() {
        val graph = DependencyGraph()

        // Component 1: A -> B
        val moduleA = Module("com.example.a", "/path/to/a")
        val moduleB = Module("com.example.b", "/path/to/b")
        graph.addModule(moduleA)
        graph.addModule(moduleB)
        graph.addDependency(Dependency(moduleA, moduleB))

        // Component 2: C -> D (disconnected from A-B)
        val moduleC = Module("com.example.c", "/path/to/c")
        val moduleD = Module("com.example.d", "/path/to/d")
        graph.addModule(moduleC)
        graph.addModule(moduleD)
        graph.addDependency(Dependency(moduleC, moduleD))

        // Isolated module E
        val moduleE = Module("com.example.e", "/path/to/e")
        graph.addModule(moduleE)

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(5, plan.modules.size)
    }

    // ========== Diamond Dependency Pattern ==========

    @Test
    fun `test calculate order with diamond dependency pattern`() {
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

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan)
        assertEquals(4, plan.modules.size)
        assertTrue(plan.circularDependencies.isEmpty())

        // D should come before B and C, which should come before A
        val moduleOrder = plan.modules.map { it.module }
        val indexD = moduleOrder.indexOf(moduleD)
        val indexB = moduleOrder.indexOf(moduleB)
        val indexC = moduleOrder.indexOf(moduleC)
        val indexA = moduleOrder.indexOf(moduleA)

        assertTrue(indexD < indexB)
        assertTrue(indexD < indexC)
        assertTrue(indexB < indexA)
        assertTrue(indexC < indexA)
    }

    // ========== Complexity Score Edge Cases ==========

    @Test
    fun `test complexity scores are normalized between 0 and 1`() {
        val graph = DependencyGraph()
        val modules = (1..10).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        // Create various dependency patterns
        for (i in 0 until modules.size - 1) {
            graph.addDependency(Dependency(modules[i], modules[i + 1]))
        }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        // All complexity scores should be between 0 and 1
        plan.modules.forEach { step ->
            assertTrue(step.complexityScore >= 0.0, "Complexity score should be >= 0")
            assertTrue(step.complexityScore <= 1.0, "Complexity score should be <= 1")
        }
    }

    // ========== Time Estimation Edge Cases ==========

    @Test
    fun `test time estimation for small project`() {
        val graph = DependencyGraph()
        val modules = (1..3).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan.estimatedTime)
        assertTrue(plan.estimatedTime.isNotEmpty())
    }

    @Test
    fun `test time estimation for large project`() {
        val graph = DependencyGraph()
        val modules = (1..100).map { Module("com.example.module$it", "/path/to/$it") }

        modules.forEach { graph.addModule(it) }

        val calculator = RefactoringOrderCalculator(graph)
        val plan = calculator.calculateRefactoringOrder()

        assertNotNull(plan.estimatedTime)
        assertTrue(plan.estimatedTime.isNotEmpty())
    }
}

