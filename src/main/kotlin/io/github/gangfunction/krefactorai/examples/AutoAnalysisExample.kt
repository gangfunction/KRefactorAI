package io.github.gangfunction.krefactorai.examples

import io.github.gangfunction.krefactorai.KRefactorAI
import io.github.gangfunction.krefactorai.analyzer.AutoProjectAnalyzer
import kotlin.io.path.Path

/**
 * Example demonstrating automatic project analysis
 */
fun mainAutoAnalysis() {
    println("=".repeat(70))
    println("KRefactorAI - Automatic Project Analysis Example")
    println("=".repeat(70))
    println()

    // Get current project path (KRefactorAI itself)
    val currentProjectPath = System.getProperty("user.dir")
    println("📂 Analyzing current project: $currentProjectPath")
    println()

    // Create auto analyzer
    val autoAnalyzer = AutoProjectAnalyzer()

    // Check if project can be analyzed
    val projectInfo = autoAnalyzer.getProjectInfo(Path(currentProjectPath))
    println(projectInfo)
    println()

    if (!projectInfo.canAnalyze) {
        println("❌ This project cannot be automatically analyzed")
        println("Supported project types: Gradle (Kotlin/Groovy), Maven")
        return
    }

    // Analyze the project
    println("🔍 Starting automatic analysis...")
    println()

    try {
        val result = autoAnalyzer.analyze(Path(currentProjectPath))

        println("✅ Analysis Complete!")
        println()
        println(result)
        println()

        // Show some statistics
        println("📊 Statistics:")
        println("-".repeat(70))
        println("Total Packages: ${result.graph.getModules().size}")
        println("Total Dependencies: ${result.graph.getDependencies().size}")

        if (result.graph.getModules().isNotEmpty()) {
            val avgDependencies =
                result.graph.getDependencies().size.toDouble() /
                    result.graph.getModules().size
            println("Average Dependencies per Package: ${"%.2f".format(avgDependencies)}")
        }
        println()

        // Show top packages by dependencies
        println("📦 Top Packages by Dependents:")
        println("-".repeat(70))
        result.graph.getModules()
            .sortedByDescending { result.graph.getInDegree(it) }
            .take(10)
            .forEachIndexed { index, module ->
                val inDegree = result.graph.getInDegree(module)
                val outDegree = result.graph.getOutDegree(module)
                println("${index + 1}. ${module.name}")
                println("   Dependents: $inDegree, Dependencies: $outDegree")
            }
        println()

        // Check for circular dependencies
        val cycles = result.graph.detectCircularDependencies()
        if (cycles.isNotEmpty()) {
            println("⚠️  Circular Dependencies Detected:")
            println("-".repeat(70))
            cycles.take(5).forEachIndexed { index, cycle ->
                println("Cycle ${index + 1}: ${cycle.joinToString(" -> ") { it.name }}")
            }
            if (cycles.size > 5) {
                println("... and ${cycles.size - 5} more cycles")
            }
        } else {
            println("✅ No circular dependencies found!")
        }
        println()

        // Generate refactoring plan
        println("🔧 Generating Refactoring Plan...")
        println()

        val refactorAI = KRefactorAI(enableAI = false)
        val plan = refactorAI.quickAnalyze(result.graph)

        println("Estimated Refactoring Time: ${plan.estimatedTime}")
        println("Total Complexity: ${"%.2f".format(plan.totalComplexity)}")
        println()

        println("Top 10 Priority Packages:")
        println("-".repeat(70))
        plan.modules.take(10).forEach { step ->
            println("${step.priority}. ${step.module.name}")
            println("   Complexity: ${"%.2f".format(step.complexityScore)}")
            println("   Dependencies: ${step.dependencies.size}, Dependents: ${step.dependents.size}")
            println()
        }

        refactorAI.close()
    } catch (e: Exception) {
        println("❌ Error during analysis: ${e.message}")
        e.printStackTrace()
    }

    println("=".repeat(70))
    println("Analysis Complete!")
    println("=".repeat(70))
}
