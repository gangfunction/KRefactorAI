package io.github.gangfunction.krefactorai

import io.github.gangfunction.krefactorai.analyzer.AutoProjectAnalyzer
import kotlin.io.path.Path

/**
 * Main entry point for running auto analysis example
 */
fun main(args: Array<String>) {
    println(KRefactorAI.getInfo())
    println()

    println("=".repeat(70))
    println("KRefactorAI - Automatic Project Analysis")
    println("=".repeat(70))
    println()

    // Get current project path
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

        // Show statistics
        println("📊 Package Statistics:")
        println("-".repeat(70))
        println("Total Packages: ${result.graph.getModules().size}")
        println("Total Dependencies: ${result.graph.getDependencies().size}")
        println()

        // Show top packages
        println("📦 Top 10 Packages by Dependents:")
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

    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    }

    println()
    println("=".repeat(70))
}

