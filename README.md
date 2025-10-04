# KRefactorAI

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-blue.svg)](https://kotlinlang.org)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
[![Status](https://img.shields.io/badge/Status-Alpha-orange.svg)]()

**KRefactorAI: Untangle Dependencies with AI and Math**

A Kotlin library that analyzes module dependencies using minimal polynomial algorithms and provides AI-powered refactoring suggestions through OpenAI's GPT models.

## 🎯 Features

- 🚀 **Automatic Project Analysis**: Automatically detect and analyze Gradle/Maven projects
- 📊 **Dependency Graph Analysis**: Extract dependencies from source code (Kotlin/Java)
- 🔄 **Circular Dependency Detection**: Identify and visualize circular dependencies
- 🧮 **Minimal Polynomial Algorithm**: Calculate optimal refactoring order using advanced linear algebra
- 🤖 **AI-Powered Suggestions**: Get intelligent refactoring recommendations from OpenAI GPT-4
- ⚡ **Parallel Refactoring Layers**: Identify modules that can be refactored simultaneously
- 📈 **Complexity Scoring**: Quantify module complexity using eigenvalue analysis
- 🔍 **Source Code Parsing**: Analyze package structures and import statements

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
    // Automatically analyze your project
    val refactorAI = KRefactorAI()
    val result = refactorAI.analyzeProject("/path/to/your/project")

    // Print results
    println(result)

    // Get refactoring plan
    val plan = refactorAI.analyze(result.graph)
    println(plan)
}
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
- [Examples](src/main/kotlin/io/github/gangfunction/krefactorai/examples/)

## 🧪 Running Examples

```bash
# Run basic example (without AI)
./gradlew run --args="basic"

# Run with AI suggestions (requires OPENAI_API_KEY)
./gradlew run --args="ai"
```

## 🏗️ Project Structure

```
KRefactorAI/
├── src/main/kotlin/io/github/gangfunction/krefactorai/
│   ├── core/              # Core algorithms (minimal polynomial, refactoring calculator)
│   ├── graph/             # Graph structures and algorithms
│   ├── ai/                # OpenAI integration
│   ├── model/             # Data models
│   ├── config/            # Configuration (API key management)
│   └── examples/          # Usage examples
├── docs/                  # Documentation
└── build.gradle.kts       # Build configuration
```

## 🧮 How It Works

1. **Dependency Analysis**: Builds a directed graph of module dependencies
2. **Minimal Polynomial Calculation**: Converts the graph to an adjacency matrix and calculates eigenvalues
3. **Complexity Scoring**: Uses eigenvalue decomposition and centrality metrics to score module complexity
4. **Topological Sorting**: Orders modules based on dependencies and complexity
5. **AI Enhancement**: Generates specific refactoring suggestions using OpenAI GPT-4

## 🤝 Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 👤 Author

**Lee Gangju** ([@gangfunction](https://github.com/gangfunction))

## 🙏 Acknowledgments

- [JGraphT](https://jgrapht.org/) for graph algorithms
- [Apache Commons Math](https://commons.apache.org/proper/commons-math/) for linear algebra
- [Ktor](https://ktor.io/) for HTTP client
- [OpenAI](https://openai.com/) for AI capabilities
