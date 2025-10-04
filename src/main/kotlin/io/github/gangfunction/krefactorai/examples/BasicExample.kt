package io.github.gangfunction.krefactorai.examples

import io.github.gangfunction.krefactorai.KRefactorAI
import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Dependency
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.ModuleType

/**
 * Basic example demonstrating KRefactorAI usage
 */
fun mainBasicExample() {
    println("=".repeat(70))
    println("KRefactorAI - Basic Example")
    println("=".repeat(70))
    println()

    // Create KRefactorAI instance
    val refactorAI = KRefactorAI(enableAI = false) // Set to true to enable AI suggestions
    
    println("📊 Creating dependency graph...")
    println()

    // Create a dependency graph
    val graph = createSampleGraph()
    
    println("Graph created with:")
    println("  - Modules: ${graph.getModules().size}")
    println("  - Dependencies: ${graph.getDependencies().size}")
    println()

    // Analyze the graph
    println("🔍 Analyzing dependencies...")
    println()
    
    val plan = refactorAI.quickAnalyze(graph)
    
    // Print results
    println(plan)
    println()
    
    // Check for circular dependencies
    if (plan.circularDependencies.isNotEmpty()) {
        println("⚠️  Warning: Circular dependencies detected!")
        plan.circularDependencies.forEachIndexed { index, cycle ->
            println("  Cycle ${index + 1}: ${cycle.joinToString(" -> ") { it.name }}")
        }
        println()
    } else {
        println("✅ No circular dependencies found")
        println()
    }
    
    // Print refactoring order
    println("📋 Recommended Refactoring Order:")
    println("-".repeat(70))
    plan.modules.forEach { step ->
        println("${step.priority}. ${step.module.name}")
        println("   Complexity: ${"%.2f".format(step.complexityScore)}")
        println("   Dependencies: ${step.dependencies.size}")
        println("   Dependents: ${step.dependents.size}")
        println()
    }
    
    // Get refactoring layers
    println("🔄 Parallel Refactoring Layers:")
    println("-".repeat(70))
    val layers = refactorAI.getRefactoringLayers(graph)
    layers?.forEachIndexed { index, layer ->
        println("Layer ${index + 1}: ${layer.joinToString(", ") { it.name }}")
    }
    println()
    
    // Cleanup
    refactorAI.close()
    
    println("=".repeat(70))
    println("Analysis complete!")
    println("=".repeat(70))
}

/**
 * Create a sample dependency graph
 */
fun createSampleGraph(): DependencyGraph {
    val graph = DependencyGraph()
    
    // Create modules representing a typical application structure
    val ui = Module("UI", "/src/ui", ModuleType.PACKAGE)
    val controller = Module("Controller", "/src/controller", ModuleType.PACKAGE)
    val service = Module("Service", "/src/service", ModuleType.PACKAGE)
    val repository = Module("Repository", "/src/repository", ModuleType.PACKAGE)
    val model = Module("Model", "/src/model", ModuleType.PACKAGE)
    val utils = Module("Utils", "/src/utils", ModuleType.PACKAGE)
    
    // Add modules to graph
    listOf(ui, controller, service, repository, model, utils).forEach {
        graph.addModule(it)
    }
    
    // Define dependencies (A -> B means A depends on B)
    graph.addDependency(Dependency(ui, controller))
    graph.addDependency(Dependency(ui, model))
    
    graph.addDependency(Dependency(controller, service))
    graph.addDependency(Dependency(controller, model))
    
    graph.addDependency(Dependency(service, repository))
    graph.addDependency(Dependency(service, model))
    graph.addDependency(Dependency(service, utils))
    
    graph.addDependency(Dependency(repository, model))
    graph.addDependency(Dependency(repository, utils))
    
    return graph
}

