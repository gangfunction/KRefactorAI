# KRefactorAI

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Alpha-orange.svg)]()

**KRefactorAI: Untangle Dependencies with AI and Math**

A Kotlin library that analyzes module dependencies using minimal polynomial algorithms and provides AI-powered refactoring suggestions through OpenAI's GPT-4. Automatically detect project structure, calculate optimal refactoring order, and get specific, actionable recommendations with code examples.

## 🎯 Features

### Core Capabilities
- 🚀 **Automatic Project Analysis**: Automatically detect and analyze Gradle/Maven projects
- 📊 **Dependency Graph Analysis**: Extract dependencies from source code (Kotlin/Java)
- 🔄 **Circular Dependency Detection**: Identify and visualize circular dependencies
- 🧮 **Minimal Polynomial Algorithm**: Calculate optimal refactoring order using advanced linear algebra
- 📈 **Complexity Scoring**: Quantify module complexity using eigenvalue analysis and centrality metrics
- ⚡ **Topological Sorting**: Order modules based on dependencies using Kahn's algorithm
- 🔍 **Source Code Parsing**: Analyze package structures and import statements

### AI-Powered Features
- 🤖 **GPT-4 Integration**: Get intelligent refactoring recommendations from OpenAI
- 💡 **Specific Kotlin Patterns**: Receive concrete suggestions with sealed classes, data classes, extension functions
- � **Code Examples**: Get actual Kotlin code snippets for each recommendation
- 🎯 **Context-Aware Analysis**: Suggestions tailored to your module's complexity and dependencies
- �️ **Risk Mitigation**: Understand potential risks and how to avoid them
- 📋 **Step-by-Step Guidance**: Detailed implementation steps for each refactoring action

## 🚀 Quick Start

### Prerequisites

- Java 17 or higher
- Kotlin 1.9+
- OpenAI API key (for AI features)

### Installation

Add to your `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.gangfunction:krefactorai:0.1.0-SNAPSHOT")
}
```

### Basic Usage

#### Option 1: Automatic Project Analysis (Recommended)

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    // Enable AI for enhanced suggestions (requires OPENAI_API_KEY)
    val refactorAI = KRefactorAI(enableAI = true)

    // Automatically analyze your project
    val result = refactorAI.analyzeProject("/path/to/your/project")

    println("✅ Analysis Complete!")
    println("Project Type: ${result.projectType}")
    println("Modules Found: ${result.graph.getModules().size}")
    println("Dependencies Found: ${result.graph.getDependencies().size}")

    // Get AI-powered refactoring plan
    val plan = refactorAI.analyze(result.graph, includeAISuggestions = true)

    // Print detailed recommendations
    plan.steps.forEach { step ->
        println("\nPriority ${step.priority}: ${step.module.name}")
        println("Complexity: ${step.complexity}")

        if (step.aiSuggestion != null) {
            println("\n🤖 AI-Powered Suggestions:")
            println(step.aiSuggestion)
        }
    }
}
```

**Output Example:**
```
✅ Analysis Complete!
Project Type: GRADLE_KOTLIN
Modules Found: 8
Dependencies Found: 16

Priority 1: io.github.example.model
Complexity: 1.00

🤖 AI-Powered Suggestions:
### 1. Specific Refactoring Actions

1. **Extract Interfaces**: Identify core responsibilities and extract interfaces...
```kotlin
interface IUserManager {
    fun createUser(user: User)
    fun deleteUser(userId: String)
}
```
2. **Convert to Sealed Class Hierarchy**: For limited type sets...

### 2. Implementation Steps
...
```

#### Option 2: Manual Graph Construction

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI
import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.*

fun main() {
    // Create dependency graph manually
    val graph = DependencyGraph()

    // Add modules
    val moduleA = Module("ModuleA", "/path/to/A")
    val moduleB = Module("ModuleB", "/path/to/B")

    graph.addModule(moduleA)
    graph.addModule(moduleB)
    graph.addDependency(Dependency(moduleA, moduleB))

    // Analyze
    val refactorAI = KRefactorAI()
    val plan = refactorAI.analyze(graph)

    // Print results
    println(plan)
}
```

## 🔑 OpenAI API Setup

KRefactorAI uses OpenAI's API for generating refactoring suggestions. Set your API key as an environment variable:

### macOS/Linux
```bash
export OPENAI_API_KEY="your-api-key-here"
```

### Windows (PowerShell)
```powershell
$env:OPENAI_API_KEY="your-api-key-here"
```

For detailed setup instructions, see [API Key Setup Guide](docs/API_KEY_SETUP.md).

## 📖 Documentation

- [Requirements Specification](REQUIREMENTS.md)
- [API Key Setup Guide](docs/API_KEY_SETUP.md)
- [Usage Guide](docs/USAGE.md)
- [Examples](src/main/kotlin/io/github/gangfunction/krefactorai/examples/)

## 🧪 Running Examples

### Automatic Analysis (Current Project)
```bash
# Analyze current project with AI suggestions
export OPENAI_API_KEY="your-api-key-here"
./gradlew run
```

### Manual Examples
```bash
# Run basic example (without AI)
./gradlew run --args="basic"

# Run with AI suggestions (requires OPENAI_API_KEY)
./gradlew run --args="ai"
```

### Run Tests
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "io.github.gangfunction.krefactorai.graph.DependencyGraphTest"
```

## 🏗️ Project Structure

```
KRefactorAI/
├── src/main/kotlin/io/github/gangfunction/krefactorai/
│   ├── analyzer/          # Project analyzers (Gradle, Maven, Source Code)
│   ├── ai/                # OpenAI GPT-4 integration
│   ├── config/            # Configuration (API key management)
│   ├── core/              # Core algorithms (minimal polynomial, refactoring calculator)
│   ├── graph/             # Graph structures and algorithms (DependencyGraph, TopologicalSorter)
│   ├── model/             # Data models (Module, Dependency, RefactoringPlan)
│   ├── examples/          # Usage examples
│   ├── KRefactorAI.kt     # Main library interface
│   ├── MainAuto.kt        # Automatic project analysis entry point
│   └── DebugGraph.kt      # Debug utility for graph visualization
├── src/test/kotlin/       # Unit tests (17 tests, all passing)
├── docs/                  # Documentation
│   ├── API_KEY_SETUP.md   # OpenAI API key setup guide
│   └── USAGE.md           # Detailed usage guide
├── build.gradle.kts       # Build configuration
└── REQUIREMENTS.md        # Requirements specification
```

## 🧮 How It Works

### 1. **Automatic Project Detection**
- Scans for `build.gradle.kts`, `build.gradle`, or `pom.xml`
- Detects project type (Gradle Kotlin, Gradle Groovy, or Maven)
- Parses build files to extract module structure

### 2. **Dependency Graph Construction**
- Analyzes source code (`.kt` and `.java` files)
- Extracts package names and import statements using regex
- Builds a directed graph where edges represent dependencies

### 3. **Complexity Analysis**
- **Minimal Polynomial Algorithm**: Converts graph to adjacency matrix and calculates eigenvalues
- **Complexity Scoring Formula**: `0.4 × eigenvalue_score + 0.3 × degree_score + 0.3 × centrality_score`
- **PageRank-style Centrality**: Power iteration algorithm for module importance

### 4. **Topological Sorting**
- **Kahn's Algorithm**: Orders modules based on dependencies
- Starts with modules having zero dependencies (out-degree = 0)
- Processes modules in dependency order for safe refactoring

### 5. **AI-Powered Recommendations**
- **Context Building**: Includes complexity scores, dependencies, dependents
- **Prompt Engineering**: Requests specific Kotlin patterns and code examples
- **GPT-4 Analysis**: Generates tailored suggestions with implementation steps
- **Risk Assessment**: Identifies potential issues and mitigation strategies

### Example Workflow
```
Your Project
    ↓
[Auto Detection] → Gradle/Maven/Source Code
    ↓
[Graph Building] → 8 modules, 16 dependencies
    ↓
[Complexity Analysis] → Scores: 0.03 to 1.00
    ↓
[Topological Sort] → Ordered: config → model → ai → graph → ...
    ↓
[AI Enhancement] → Specific Kotlin refactoring patterns
    ↓
Actionable Refactoring Plan
```

## 🎓 Key Algorithms

### Minimal Polynomial Algorithm
Uses eigenvalue decomposition of the adjacency matrix to calculate module complexity:
- Converts dependency graph to adjacency matrix
- Computes eigenvalues using Apache Commons Math
- Scores modules based on eigenvalue magnitude and centrality

### Kahn's Topological Sort
Orders modules for safe refactoring:
1. Calculate out-degree (number of dependencies) for each module
2. Start with modules having out-degree = 0 (no dependencies)
3. Process modules and reduce out-degree of dependent modules
4. Continue until all modules are processed

### PageRank-style Centrality
Identifies critical modules using power iteration:
- Iteratively calculates module importance
- Considers both direct and indirect dependencies
- Converges to stable centrality scores

## 🔧 Technical Stack

- **Kotlin 1.9.22**: Modern JVM language with null-safety and coroutines
- **JGraphT 1.5.2**: Graph data structures and algorithms
- **Apache Commons Math 3.6.1**: Linear algebra and eigenvalue decomposition
- **Ktor Client 2.3.7**: Async HTTP client for OpenAI API
- **kotlinx.serialization 1.6.2**: JSON serialization/deserialization
- **Logback 1.4.14**: Logging framework
- **JUnit 5**: Testing framework

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup
```bash
# Clone the repository
git clone https://github.com/gangfunction/KRefactorAI.git
cd KRefactorAI

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
export OPENAI_API_KEY="your-api-key-here"
./gradlew run
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Lee Gangju** ([@gangfunction](https://github.com/gangfunction))

## � Performance

- **Analysis Speed**: ~1-2 seconds for projects with 10-20 modules
- **AI Response Time**: ~10-20 seconds per module (depends on OpenAI API)
- **Memory Usage**: Minimal (graph-based analysis)
- **Test Coverage**: 17 unit tests covering core functionality

## 🚧 Roadmap

- [ ] Support for multi-module Gradle projects
- [ ] Visualization of dependency graphs (GraphViz/Mermaid)
- [ ] Integration with CI/CD pipelines
- [ ] Custom refactoring rules and patterns
- [ ] Support for other languages (TypeScript, Python, etc.)
- [ ] Web UI for interactive analysis
- [ ] GitHub Action for automated analysis

## �🙏 Acknowledgments

- [JGraphT](https://jgrapht.org/) for graph algorithms
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/) for linear algebra
- [Ktor](https://ktor.io/) for HTTP client
- [OpenAI](https://openai.com/) for GPT-4 AI capabilities
- [Kotlin](https://kotlinlang.org/) for the amazing language
 Guide](docs/USAGE.md)
- [Examples](src/main/kotlin/io/github/gangfunction/krefactorai/examples/)

## 🧪 Running Examples

### Automatic Analysis (Current Project)
```bash
# Analyze current project with AI suggestions
export OPENAI_API_KEY="your-api-key-here"
./gradlew run
```

### Manual Examples
```bash
# Run basic example (without AI)
./gradlew run --args="basic"

# Run with AI suggestions (requires OPENAI_API_KEY)
./gradlew run --args="ai"
```

### Run Tests
```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests "io.github.gangfunction.krefactorai.graph.DependencyGraphTest"
```

## 🏗️ Project Structure

```
KRefactorAI/
├── src/main/kotlin/io/github/gangfunction/krefactorai/
│   ├── analyzer/          # Project analyzers (Gradle, Maven, Source Code)
│   ├── ai/                # OpenAI GPT-4 integration
│   ├── config/            # Configuration (API key management)
│   ├── core/              # Core algorithms (minimal polynomial, refactoring calculator)
│   ├── graph/             # Graph structures and algorithms (DependencyGraph, TopologicalSorter)
│   ├── model/             # Data models (Module, Dependency, RefactoringPlan)
│   ├── examples/          # Usage examples
│   ├── KRefactorAI.kt     # Main library interface
│   ├── MainAuto.kt        # Automatic project analysis entry point
│   └── DebugGraph.kt      # Debug utility for graph visualization
├── src/test/kotlin/       # Unit tests (17 tests, all passing)
├── docs/                  # Documentation
│   ├── API_KEY_SETUP.md   # OpenAI API key setup guide
│   └── USAGE.md           # Detailed usage guide
├── build.gradle.kts       # Build configuration
└── REQUIREMENTS.md        # Requirements specification
```

## 🧮 How It Works

### 1. **Automatic Project Detection**
- Scans for `build.gradle.kts`, `build.gradle`, or `pom.xml`
- Detects project type (Gradle Kotlin, Gradle Groovy, or Maven)
- Parses build files to extract module structure

### 2. **Dependency Graph Construction**
- Analyzes source code (`.kt` and `.java` files)
- Extracts package names and import statements using regex
- Builds a directed graph where edges represent dependencies

### 3. **Complexity Analysis**
- **Minimal Polynomial Algorithm**: Converts graph to adjacency matrix and calculates eigenvalues
- **Complexity Scoring Formula**: `0.4 × eigenvalue_score + 0.3 × degree_score + 0.3 × centrality_score`
- **PageRank-style Centrality**: Power iteration algorithm for module importance

### 4. **Topological Sorting**
- **Kahn's Algorithm**: Orders modules based on dependencies
- Starts with modules having zero dependencies (out-degree = 0)
- Processes modules in dependency order for safe refactoring

### 5. **AI-Powered Recommendations**
- **Context Building**: Includes complexity scores, dependencies, dependents
- **Prompt Engineering**: Requests specific Kotlin patterns and code examples
- **GPT-4 Analysis**: Generates tailored suggestions with implementation steps
- **Risk Assessment**: Identifies potential issues and mitigation strategies

### Example Workflow
```
Your Project
    ↓
[Auto Detection] → Gradle/Maven/Source Code
    ↓
[Graph Building] → 8 modules, 16 dependencies
    ↓
[Complexity Analysis] → Scores: 0.03 to 1.00
    ↓
[Topological Sort] → Ordered: config → model → ai → graph → ...
    ↓
[AI Enhancement] → Specific Kotlin refactoring patterns
    ↓
Actionable Refactoring Plan
```

## 🎓 Key Algorithms

### Minimal Polynomial Algorithm
Uses eigenvalue decomposition of the adjacency matrix to calculate module complexity:
- Converts dependency graph to adjacency matrix
- Computes eigenvalues using Apache Commons Math
- Scores modules based on eigenvalue magnitude and centrality

### Kahn's Topological Sort
Orders modules for safe refactoring:
1. Calculate out-degree (number of dependencies) for each module
2. Start with modules having out-degree = 0 (no dependencies)
3. Process modules and reduce out-degree of dependent modules
4. Continue until all modules are processed

### PageRank-style Centrality
Identifies critical modules using power iteration:
- Iteratively calculates module importance
- Considers both direct and indirect dependencies
- Converges to stable centrality scores

## 🔧 Technical Stack

- **Kotlin 1.9.22**: Modern JVM language with null-safety and coroutines
- **JGraphT 1.5.2**: Graph data structures and algorithms
- **Apache Commons Math 3.6.1**: Linear algebra and eigenvalue decomposition
- **Ktor Client 2.3.7**: Async HTTP client for OpenAI API
- **kotlinx.serialization 1.6.2**: JSON serialization/deserialization
- **Logback 1.4.14**: Logging framework
- **JUnit 5**: Testing framework

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

### Development Setup
```bash
# Clone the repository
git clone https://github.com/gangfunction/KRefactorAI.git
cd KRefactorAI

# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the application
export OPENAI_API_KEY="your-api-key-here"
./gradlew run
```

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Lee Gangju** ([@gangfunction](https://github.com/gangfunction))

## � Performance

- **Analysis Speed**: ~1-2 seconds for projects with 10-20 modules
- **AI Response Time**: ~10-20 seconds per module (depends on OpenAI API)
- **Memory Usage**: Minimal (graph-based analysis)
- **Test Coverage**: 17 unit tests covering core functionality

## 🚧 Roadmap

- [ ] Support for multi-module Gradle projects
- [ ] Visualization of dependency graphs (GraphViz/Mermaid)
- [ ] Integration with CI/CD pipelines
- [ ] Custom refactoring rules and patterns
- [ ] Support for other languages (TypeScript, Python, etc.)
- [ ] Web UI for interactive analysis
- [ ] GitHub Action for automated analysis

## �🙏 Acknowledgments

- [JGraphT](https://jgrapht.org/) for graph algorithms
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/) for linear algebra
- [Ktor](https://ktor.io/) for HTTP client
- [OpenAI](https://openai.com/) for GPT-4 AI capabilities
- [Kotlin](https://kotlinlang.org/) for the amazing language
