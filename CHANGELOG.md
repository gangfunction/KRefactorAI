# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Comprehensive edge case tests (67 tests total)
  - SourceCodeAnalyzerEdgeCaseTest (27 tests)
  - DependencyGraphEdgeCaseTest (19 tests)
  - TopologicalSorterEdgeCaseTest (4 tests)
- CI/CD workflows
  - Continuous Integration (multi-OS, multi-JDK)
  - Release automation
  - CodeQL security analysis
- Code quality tools
  - Jacoco code coverage
  - Ktlint code style checking
  - Detekt static analysis
- GitHub Packages publishing support

### Changed
- Enhanced OpenAI prompts for actionable markdown output
- Fixed topological sort algorithm (out-degree vs in-degree bug)
- Improved test coverage and robustness

### Fixed
- Topological sort now correctly processes all modules
- Wildcard import handling in SourceCodeAnalyzer
- Duplicate dependency handling in DependencyGraph

## [0.1.0-SNAPSHOT] - 2025-10-04

### Added
- Initial release
- Core dependency analysis features
  - Minimal Polynomial algorithm for complexity calculation
  - Topological sorting with Kahn's algorithm
  - Circular dependency detection
  - PageRank-style centrality calculation
- AI-powered refactoring suggestions
  - OpenAI GPT-4 integration
  - Context-aware recommendations
  - Actionable markdown output with task lists
- Automatic project analysis
  - Gradle project support
  - Maven project support
  - Source code parsing (Kotlin/Java)
- Comprehensive documentation
  - README with usage examples
  - API key setup guide
  - Usage documentation
- 17 unit tests (all passing)
- Maven publishing configuration

### Core Features
- **DependencyGraph**: JGraphT-based directed graph for module dependencies
- **MinimalPolynomial**: Eigenvalue-based complexity scoring
- **TopologicalSorter**: Priority-based topological sorting
- **OpenAIClient**: AI-powered refactoring recommendations
- **AutoProjectAnalyzer**: Automatic Gradle/Maven project detection
- **SourceCodeAnalyzer**: Package and import extraction from source files

### Technical Stack
- Kotlin 1.9.22
- JVM 17+
- JGraphT 1.5.2
- Apache Commons Math 3.6.1
- Ktor Client 2.3.7
- kotlinx.serialization 1.6.2

[Unreleased]: https://github.com/gangfunction/KRefactorAI/compare/v0.1.0-SNAPSHOT...HEAD
[0.1.0-SNAPSHOT]: https://github.com/gangfunction/KRefactorAI/releases/tag/v0.1.0-SNAPSHOT

