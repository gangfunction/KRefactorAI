package io.github.gangfunction.krefactorai

import io.github.gangfunction.krefactorai.analyzer.AutoProjectAnalyzer
import kotlin.io.path.Path

fun main() {
    val currentProjectPath = System.getProperty("user.dir")
    val autoAnalyzer = AutoProjectAnalyzer()
    val result = autoAnalyzer.analyze(Path(currentProjectPath))

    println("=== Graph Debug Info ===")
    println("Total modules: ${result.graph.getModules().size}")
    println()

    result.graph.getModules().forEach { module ->
        val dependencies = result.graph.getDependenciesOf(module)
        val dependents = result.graph.getDependentsOf(module)
        val inDegree = result.graph.getInDegree(module)
        val outDegree = result.graph.getOutDegree(module)

        println("Module: ${module.name}")
        println("  In-degree: $inDegree (number of dependencies)")
        println("  Out-degree: $outDegree (number of dependents)")
        println("  Dependencies (${dependencies.size}):")
        dependencies.forEach { println("    -> ${it.name}") }
        println("  Dependents (${dependents.size}):")
        dependents.forEach { println("    <- ${it.name}") }
        println()
    }

    println("=== Topological Sort Test ===")
    val sorter = io.github.gangfunction.krefactorai.graph.TopologicalSorter(result.graph)
    val sorted = sorter.sort()

    if (sorted != null) {
        println("✅ Sort successful!")
        sorted.forEachIndexed { index, module ->
            println("${index + 1}. ${module.name}")
        }
    } else {
        println("❌ Sort failed!")
    }
}
