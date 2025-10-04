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
        println("🔧 Generating Refactoring Plan with AI...")
        println()

        val refactorAI = KRefactorAI(enableAI = true)
        val plan = refactorAI.analyze(result.graph, includeAISuggestions = true)

        println(plan)
        println()

        println("📋 Detailed Refactoring Recommendations:")
        println("=".repeat(70))
        plan.modules.take(10).forEach { step ->
            println()
            println("Priority ${step.priority}: ${step.module.name}")
            println("-".repeat(70))
            println("Complexity Score: ${"%.2f".format(step.complexityScore)}")
            println("Dependencies: ${step.dependencies.size}")
            println("Dependents: ${step.dependents.size}")

            // Show AI suggestions if available
            if (step.aiSuggestion != null) {
                println()
                println("🤖 AI-Powered Suggestions:")
                println(step.aiSuggestion)
            } else {
                // Provide specific recommendations
                println()
                println("💡 Recommendations:")
                when {
                    step.complexityScore > 0.7 -> {
                        println("  • HIGH PRIORITY - This package has high complexity")
                        println("  • Consider breaking it into smaller modules")
                        println("  • Review and simplify dependencies")
                    }
                    step.dependents.size > 3 -> {
                        println("  • Many packages depend on this - refactor carefully")
                        println("  • Ensure backward compatibility")
                        println("  • Consider creating stable interfaces")
                    }
                    step.dependencies.size > 5 -> {
                        println("  • This package has many dependencies")
                        println("  • Review if all dependencies are necessary")
                        println("  • Consider dependency injection")
                    }
                    step.dependencies.isEmpty() && step.dependents.isEmpty() -> {
                        println("  • Isolated package - safe to refactor")
                        println("  • Consider if this package is still needed")
                    }
                    else -> {
                        println("  • Standard refactoring approach")
                        println("  • Review code quality and test coverage")
                    }
                }
            }

            if (step.dependencies.isNotEmpty()) {
                println()
                println("📦 Depends on:")
                step.dependencies.take(5).forEach { dep ->
                    println("  - ${dep.name}")
                }
                if (step.dependencies.size > 5) {
                    println("  ... and ${step.dependencies.size - 5} more")
                }
            }

            if (step.dependents.isNotEmpty()) {
                println()
                println("👥 Used by:")
                step.dependents.take(5).forEach { dep ->
                    println("  - ${dep.name}")
                }
                if (step.dependents.size > 5) {
                    println("  ... and ${step.dependents.size - 5} more")
                }
            }
        }

        refactorAI.close()

    } catch (e: Exception) {
        println("❌ Error: ${e.message}")
        e.printStackTrace()
    }

    println()
    println("=".repeat(70))
}

