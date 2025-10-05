# Contributing to KRefactorAI

Thank you for your interest in contributing to KRefactorAI! 🎉

## 📋 Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Setup](#development-setup)
- [How to Contribute](#how-to-contribute)
- [Coding Standards](#coding-standards)
- [Testing Guidelines](#testing-guidelines)
- [Pull Request Process](#pull-request-process)
- [Issue Guidelines](#issue-guidelines)

---

## Code of Conduct

This project follows a simple code of conduct:
- Be respectful and inclusive
- Provide constructive feedback
- Focus on what is best for the community
- Show empathy towards other community members

---

## Getting Started

### Prerequisites

- **Java 17 or higher**
- **Kotlin 1.9+**
- **Git**
- **Gradle 8.5+** (wrapper included)

### Fork and Clone

1. Fork the repository on GitHub
2. Clone your fork:
   ```bash
git clone https://github.com/YOUR_USERNAME/KRefactorAI.git
   cd KRefactorAI
```

3. Add upstream remote:
   ```bash
git remote add upstream https://github.com/gangfunction/KRefactorAI.git
```

---

## Development Setup

### 1. Build the Project

```bash
./gradlew build
```

### 2. Run Tests

```bash
./gradlew test
```

### 3. Run Code Quality Checks

```bash
# Ktlint (code formatting)
./gradlew ktlintCheck

# Detekt (static analysis)
./gradlew detekt

# All checks
./gradlew check
```

### 4. Format Code

```bash
./gradlew ktlintFormat
```

---

## How to Contribute

### Types of Contributions

We welcome various types of contributions:

1. **🐛 Bug Reports**: Found a bug? Open an issue!
2. **✨ Feature Requests**: Have an idea? We'd love to hear it!
3. **📝 Documentation**: Improve docs, add examples
4. **🔧 Code**: Fix bugs, add features, improve performance
5. **🧪 Tests**: Add test cases, improve coverage
6. **🎨 UI/UX**: Improve output formatting, error messages

### Workflow

1. **Create an Issue** (for significant changes)
   - Describe the problem or feature
   - Discuss the approach
   - Get feedback before coding

2. **Create a Branch**
   ```bash
git checkout -b feature/your-feature-name
   # or
   git checkout -b fix/your-bug-fix
```

3. **Make Changes**
   - Write clean, readable code
   - Follow coding standards
   - Add tests for new features
   - Update documentation

4. **Commit Changes**
   ```bash
git add .
   git commit -m "feat: Add amazing feature"
```

5. **Push and Create PR**
   ```bash
git push origin feature/your-feature-name
```

---

## Coding Standards

### Kotlin Style Guide

We follow the [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html).

**Key Points:**
- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable names
- Add KDoc comments for public APIs
- Prefer immutability (`val` over `var`)
- Use data classes for DTOs
- Leverage Kotlin idioms (extension functions, sealed classes, etc.)

### Code Quality Tools

- **Ktlint**: Enforces Kotlin style guide
- **Detekt**: Static code analysis
- **Jacoco**: Code coverage (minimum 70%)

### Example

```kotlin
/**
 * Analyzes a dependency graph and generates a refactoring plan.
 *
 * @param graph The dependency graph to analyze
 * @param includeAISuggestions Whether to include AI-powered suggestions
 * @return A refactoring plan with ordered steps
 */
fun analyze(
    graph: DependencyGraph,
    includeAISuggestions: Boolean = enableAI,
): RefactoringPlan {
    if (graph.isEmpty()) {
        return createEmptyPlan()
    }

    val calculator = RefactoringOrderCalculator(graph)
    val plan = calculator.calculateRefactoringOrder()

    return if (includeAISuggestions && openAIClient != null) {
        enhancePlanWithAI(plan)
    } else {
        plan
    }
```
---

## Testing Guidelines

### Writing Tests

- **Unit Tests**: Test individual components in isolation
- **Integration Tests**: Test component interactions
- **Edge Cases**: Test boundary conditions and error cases
- **Coverage**: Aim for >70% code coverage

### Test Structure
```kotlin
class MyComponentTest {
    @Test
    fun `test should do something when condition is met`() {
        // Given
        val input = createTestInput()

        // When
        val result = component.process(input)

        // Then
        assertEquals(expected, result)
    }
}
```
### Running Tests
```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "DependencyGraphTest"

# Run with coverage
./gradlew test jacocoTestReport
```
---

## Pull Request Process

### Before Submitting

1. ✅ **Tests pass**: `./gradlew test`
2. ✅ **Code quality**: `./gradlew ktlintCheck detekt`
3. ✅ **Build succeeds**: `./gradlew build`
4. ✅ **Documentation updated**: README, KDoc, etc.
5. ✅ **Commits are clean**: Use conventional commits

### Commit Message Format

We use [Conventional Commits](https://www.conventionalcommits.org/):
```
<type>(<scope>): <subject>

<body>

<footer>
```
**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process, dependencies, etc.
- `ci`: CI/CD changes

**Examples:**
```
feat(analyzer): Add support for Maven multi-module projects

- Implement MavenProjectAnalyzer
- Add tests for multi-module detection
- Update documentation

Closes #123
```

```
fix(graph): Fix circular dependency detection for complex cycles

The previous implementation failed to detect cycles with more than
3 nodes. This fix uses Tarjan's algorithm for robust cycle detection.

Fixes #456
```
### PR Template

When creating a PR, include:

1. **Description**: What does this PR do?
2. **Motivation**: Why is this change needed?
3. **Changes**: List of changes made
4. **Testing**: How was this tested?
5. **Screenshots**: If applicable
6. **Checklist**:
   - [ ] Tests added/updated
   - [ ] Documentation updated
   - [ ] Code quality checks pass
   - [ ] No breaking changes (or documented)

---

## Issue Guidelines

### Bug Reports

Include:
- **Description**: Clear description of the bug
- **Steps to Reproduce**: Minimal steps to reproduce
- **Expected Behavior**: What should happen
- **Actual Behavior**: What actually happens
- **Environment**: OS, Java version, Kotlin version
- **Logs/Screenshots**: If applicable

### Feature Requests

Include:
- **Problem**: What problem does this solve?
- **Proposed Solution**: How should it work?
- **Alternatives**: Other solutions considered
- **Additional Context**: Examples, mockups, etc.

---

## Development Tips

### Useful Gradle Tasks
```bash
# Clean build
./gradlew clean build

# Run with debug logging
./gradlew test --debug

# Refresh dependencies
./gradlew build --refresh-dependencies

# Generate coverage report
./gradlew jacocoTestReport
# Report: build/reports/jacoco/test/html/index.html

# Check code style
./gradlew ktlintCheck
# Auto-fix: ./gradlew ktlintFormat

# Static analysis
./gradlew detekt
# Report: build/reports/detekt/detekt.html
```

### IDE Setup

**IntelliJ IDEA** (Recommended):
1. Import as Gradle project
2. Enable Kotlin plugin
3. Install Ktlint plugin (optional)
4. Set code style to Kotlin conventions

**VS Code**:
1. Install Kotlin extension
2. Install Gradle extension
3. Configure Java 17+

---

## Questions?

- **GitHub Issues**: For bugs and features
- **GitHub Discussions**: For questions and ideas
- **Email**: gangfunction@gmail.com

---

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to KRefactorAI! 🚀