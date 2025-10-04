# KRefactorAI Usage Guide

This guide provides detailed instructions on how to use KRefactorAI to analyze and refactor your Kotlin/Java projects.

## Table of Contents

1. [Installation](#installation)
2. [Basic Usage](#basic-usage)
3. [Advanced Features](#advanced-features)
4. [API Reference](#api-reference)
5. [Examples](#examples)

---

## Installation

### Gradle (Kotlin DSL)

```kotlin
dependencies {
    implementation("io.github.gangfunction:krefactorai:0.1.0-SNAPSHOT")
}
```

### Gradle (Groovy)

```groovy
dependencies {
    implementation 'io.github.gangfunction:krefactorai:0.1.0-SNAPSHOT'
}
```

### Maven

```xml
<dependency>
    <groupId>io.github.gangfunction</groupId>
    <artifactId>krefactorai</artifactId>
    <version>0.1.0-SNAPSHOT</version>
</dependency>
```

---

## Basic Usage

### 1. Create a Dependency Graph

```kotlin
import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.*

val graph = DependencyGraph()

// Create modules
val moduleA = Module("ModuleA", "/path/to/A", ModuleType.PACKAGE)
val moduleB = Module("ModuleB", "/path/to/B", ModuleType.PACKAGE)

// Add modules to graph
graph.addModule(moduleA)
graph.addModule(moduleB)

// Add dependency (A depends on B)
graph.addDependency(Dependency(moduleA, moduleB))
```

### 2. Analyze Dependencies

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

val refactorAI = KRefactorAI(enableAI = false)
val plan = refactorAI.analyze(graph)

// Print the refactoring plan
println(plan)
```

### 3. Get Refactoring Order

```kotlin
plan.modules.forEach { step ->
    println("${step.priority}. ${step.module.name}")
    println("   Complexity: ${step.complexityScore}")
    println("   Dependencies: ${step.dependencies.size}")
    println("   Dependents: ${step.dependents.size}")
}
```

---

## Advanced Features

### AI-Powered Suggestions

To enable AI-powered refactoring suggestions, you need to set up your OpenAI API key:

```bash
export OPENAI_API_KEY="your-api-key-here"
```

Then enable AI in your code:

```kotlin
val refactorAI = KRefactorAI(enableAI = true)
val plan = refactorAI.analyze(graph, includeAISuggestions = true)

plan.modules.forEach { step ->
    println("Module: ${step.module.name}")
    println("AI Suggestion: ${step.aiSuggestion}")
}
```

### Detect Circular Dependencies

```kotlin
val cycles = refactorAI.findCircularDependencies(graph)

if (cycles.isNotEmpty()) {
    println("⚠️ Circular dependencies found:")
    cycles.forEach { cycle ->
        println(cycle.joinToString(" -> ") { it.name })
    }
}
```

### Get Parallel Refactoring Layers

```kotlin
val layers = refactorAI.getRefactoringLayers(graph)

layers?.forEachIndexed { index, layer ->
    println("Layer ${index + 1}:")
    layer.forEach { module ->
        println("  - ${module.name}")
    }
}
```

### Quick Analysis (Without AI)

```kotlin
val plan = refactorAI.quickAnalyze(graph)
```

---

## API Reference

### KRefactorAI

Main class for analyzing dependencies and generating refactoring plans.

#### Constructor

```kotlin
KRefactorAI(
    enableAI: Boolean = true,
    aiModel: String = "gpt-4"
)
```

#### Methods

- `analyze(graph: DependencyGraph, includeAISuggestions: Boolean = enableAI): RefactoringPlan`
  - Analyze a dependency graph and generate a complete refactoring plan
  
- `quickAnalyze(graph: DependencyGraph): RefactoringPlan`
  - Quick analysis without AI suggestions
  
- `findCircularDependencies(graph: DependencyGraph): List<List<Module>>`
  - Find all circular dependencies in the graph
  
- `getRefactoringLayers(graph: DependencyGraph): List<Set<Module>>?`
  - Get modules grouped by layers for parallel refactoring
  
- `isAIEnabled(): Boolean`
  - Check if AI features are available
  
- `getApiKeyStatus(): String`
  - Get the current API key status
  
- `close()`
  - Close resources and cleanup

### DependencyGraph

Represents a directed graph of module dependencies.

#### Methods

- `addModule(module: Module)`
  - Add a module to the graph
  
- `addDependency(dependency: Dependency)`
  - Add a dependency between two modules
  
- `getModules(): Set<Module>`
  - Get all modules in the graph
  
- `getDependencies(): List<Dependency>`
  - Get all dependencies
  
- `getDependenciesOf(module: Module): Set<Module>`
  - Get modules that this module depends on
  
- `getDependentsOf(module: Module): Set<Module>`
  - Get modules that depend on this module
  
- `detectCircularDependencies(): List<List<Module>>`
  - Detect circular dependencies

### Module

Represents a module or package.

```kotlin
data class Module(
    val name: String,
    val path: String,
    val type: ModuleType = ModuleType.PACKAGE
)
```

### Dependency

Represents a dependency relationship.

```kotlin
data class Dependency(
    val from: Module,
    val to: Module,
    val weight: Double = 1.0,
    val type: DependencyType = DependencyType.DIRECT
)
```

### RefactoringPlan

Contains the complete refactoring plan.

```kotlin
data class RefactoringPlan(
    val modules: List<RefactoringStep>,
    val circularDependencies: List<List<Module>>,
    val totalComplexity: Double,
    val estimatedTime: String
)
```

### RefactoringStep

Represents a single step in the refactoring plan.

```kotlin
data class RefactoringStep(
    val module: Module,
    val priority: Int,
    val complexityScore: Double,
    val dependencies: List<Module>,
    val dependents: List<Module>,
    val aiSuggestion: String? = null
)
```

---

## Examples

### Example 1: Simple Dependency Analysis

```kotlin
fun main() {
    val graph = DependencyGraph()
    
    // Create a simple dependency chain: A -> B -> C
    val moduleA = Module("A", "/src/A")
    val moduleB = Module("B", "/src/B")
    val moduleC = Module("C", "/src/C")
    
    graph.addDependency(Dependency(moduleA, moduleB))
    graph.addDependency(Dependency(moduleB, moduleC))
    
    val refactorAI = KRefactorAI(enableAI = false)
    val plan = refactorAI.quickAnalyze(graph)
    
    println(plan)
}
```

### Example 2: Detect Circular Dependencies

```kotlin
fun main() {
    val graph = DependencyGraph()
    
    // Create a circular dependency: A -> B -> C -> A
    val moduleA = Module("A", "/src/A")
    val moduleB = Module("B", "/src/B")
    val moduleC = Module("C", "/src/C")
    
    graph.addDependency(Dependency(moduleA, moduleB))
    graph.addDependency(Dependency(moduleB, moduleC))
    graph.addDependency(Dependency(moduleC, moduleA))
    
    val refactorAI = KRefactorAI(enableAI = false)
    val cycles = refactorAI.findCircularDependencies(graph)
    
    if (cycles.isNotEmpty()) {
        println("⚠️ Circular dependencies detected!")
        cycles.forEach { cycle ->
            println(cycle.joinToString(" -> ") { it.name })
        }
    }
}
```

### Example 3: AI-Powered Analysis

```kotlin
suspend fun main() {
    // Make sure OPENAI_API_KEY is set
    val refactorAI = KRefactorAI(enableAI = true, aiModel = "gpt-4")
    
    // Test API connection
    val isConnected = refactorAI.testAIConnection()
    if (!isConnected) {
        println("❌ Failed to connect to OpenAI API")
        return
    }
    
    val graph = createYourGraph()
    val plan = refactorAI.analyze(graph, includeAISuggestions = true)
    
    plan.modules.forEach { step ->
        println("Module: ${step.module.name}")
        println("Priority: ${step.priority}")
        println("Complexity: ${step.complexityScore}")
        println("AI Suggestion:")
        println(step.aiSuggestion)
        println()
    }
    
    refactorAI.close()
}
```

### Example 4: Using the Built-in Example

```kotlin
fun main() {
    // Use the built-in example graph
    val graph = KRefactorAI.createExampleGraph()
    
    val refactorAI = KRefactorAI(enableAI = false)
    val plan = refactorAI.analyze(graph)
    
    println(plan)
}
```

---

## Best Practices

1. **Always close resources**: Call `refactorAI.close()` when done to release HTTP client resources
2. **Handle API errors**: Wrap AI-enabled calls in try-catch blocks
3. **Use quick analysis for large graphs**: If you don't need AI suggestions, use `quickAnalyze()` for better performance
4. **Check for circular dependencies first**: Before refactoring, always check for circular dependencies
5. **Use layers for parallel work**: If possible, refactor modules in the same layer simultaneously

---

## Troubleshooting

### API Key Issues

If you encounter API key errors:

1. Verify the environment variable is set: `echo $OPENAI_API_KEY`
2. Check the API key format (should start with `sk-`)
3. Verify the key is active on OpenAI Platform
4. See [API Key Setup Guide](API_KEY_SETUP.md) for detailed instructions

### Performance Issues

For large graphs (>1000 modules):

1. Use `quickAnalyze()` instead of full analysis
2. Disable AI suggestions
3. Consider analyzing subgraphs separately

### Memory Issues

If you encounter OutOfMemoryError:

1. Increase JVM heap size: `-Xmx4g`
2. Process modules in batches
3. Use streaming analysis (future feature)

---

## Support

- GitHub Issues: [KRefactorAI Issues](https://github.com/gangfunction/KRefactorAI/issues)
- Documentation: [GitHub Wiki](https://github.com/gangfunction/KRefactorAI/wiki)
- Email: gangfunction@gmail.com

