# Contributing to KRefactorAI

Thank you for your interest in contributing to KRefactorAI! This document provides guidelines and instructions for contributing.

## 🚀 Getting Started

### Prerequisites

- JDK 17 or higher
- Gradle 8.5 or higher (wrapper included)
- Git

### Setting Up Development Environment

1. **Fork and Clone**
   ```bash
   git clone https://github.com/YOUR_USERNAME/KRefactorAI.git
   cd KRefactorAI
   ```

2. **Build the Project**
   ```bash
   ./gradlew build
   ```

3. **Run Tests**
   ```bash
   ./gradlew test
   ```

4. **Run Code Quality Checks**
   ```bash
   ./gradlew ktlintCheck detekt
   ```

## 📝 Development Workflow

### 1. Create a Branch

Create a feature branch from `master`:

```bash
git checkout -b feature/your-feature-name
```

Branch naming conventions:
- `feature/` - New features
- `fix/` - Bug fixes
- `docs/` - Documentation updates
- `refactor/` - Code refactoring
- `test/` - Test additions or modifications

### 2. Make Changes

- Write clean, readable code
- Follow Kotlin coding conventions
- Add tests for new functionality
- Update documentation as needed

### 3. Code Quality

Before committing, ensure your code passes all checks:

```bash
# Format code
./gradlew ktlintFormat

# Run static analysis
./gradlew detekt

# Run tests
./gradlew test

# Check coverage
./gradlew jacocoTestReport
```

### 4. Commit Changes

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```bash
git commit -m "feat: add new feature"
git commit -m "fix: resolve bug in topological sort"
git commit -m "docs: update README with examples"
git commit -m "test: add edge case tests for DependencyGraph"
```

Commit types:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements

### 5. Push and Create Pull Request

```bash
git push origin feature/your-feature-name
```

Then create a Pull Request on GitHub with:
- Clear title and description
- Reference to related issues
- Screenshots/examples if applicable

## 🧪 Testing Guidelines

### Writing Tests

- Place tests in `src/test/kotlin/`
- Mirror the package structure of main code
- Use descriptive test names with backticks:
  ```kotlin
  @Test
  fun `test should handle circular dependencies correctly`() {
      // Test implementation
  }
  ```

### Test Coverage

- Aim for >70% code coverage
- Write tests for:
  - Happy path scenarios
  - Edge cases
  - Error conditions
  - Boundary conditions

### Running Specific Tests

```bash
# Run all tests
./gradlew test

# Run specific test class
./gradlew test --tests "DependencyGraphTest"

# Run specific test method
./gradlew test --tests "DependencyGraphTest.test should detect circular dependencies"
```

## 📚 Code Style

### Kotlin Conventions

- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful variable and function names
- Keep functions small and focused
- Prefer immutability (`val` over `var`)
- Use data classes for simple data holders
- Leverage Kotlin's null safety features

### Code Formatting

We use ktlint for code formatting:

```bash
# Check formatting
./gradlew ktlintCheck

# Auto-format code
./gradlew ktlintFormat
```

### Static Analysis

We use detekt for static analysis:

```bash
./gradlew detekt
```

Fix any issues reported before submitting PR.

## 📖 Documentation

### Code Documentation

- Add KDoc comments for public APIs
- Include usage examples in documentation
- Document complex algorithms and logic

Example:
```kotlin
/**
 * Calculates the complexity score for each module in the graph.
 * 
 * Uses eigenvalue decomposition of the adjacency matrix to determine
 * module importance and complexity.
 * 
 * @param graph The dependency graph to analyze
 * @return Map of modules to their complexity scores (0.0 to 1.0)
 * 
 * @throws IllegalArgumentException if graph is empty
 */
fun calculateComplexityScores(graph: DependencyGraph): Map<Module, Double>
```

### README Updates

Update README.md when:
- Adding new features
- Changing API
- Adding new dependencies
- Updating requirements

## 🐛 Reporting Bugs

### Before Reporting

1. Check existing issues
2. Verify it's reproducible
3. Test with latest version

### Bug Report Template

```markdown
**Describe the bug**
A clear description of the bug.

**To Reproduce**
Steps to reproduce:
1. ...
2. ...

**Expected behavior**
What you expected to happen.

**Actual behavior**
What actually happened.

**Environment**
- OS: [e.g., macOS 14.0]
- JDK: [e.g., 17.0.8]
- KRefactorAI version: [e.g., 0.1.0]

**Additional context**
Any other relevant information.
```

## 💡 Feature Requests

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
A clear description of the problem.

**Describe the solution you'd like**
What you want to happen.

**Describe alternatives you've considered**
Other solutions you've thought about.

**Additional context**
Any other relevant information.
```

## 🔍 Code Review Process

### What We Look For

- Code quality and readability
- Test coverage
- Documentation
- Performance implications
- Breaking changes
- Security considerations

### Review Timeline

- Initial review: Within 3-5 days
- Follow-up reviews: Within 1-2 days

## 📜 License

By contributing, you agree that your contributions will be licensed under the MIT License.

## 🙏 Thank You!

Your contributions make KRefactorAI better for everyone. We appreciate your time and effort!

## 📞 Questions?

- Open an issue for questions
- Join discussions in GitHub Discussions
- Contact maintainers: gangfunction@gmail.com

---

**Happy Contributing! 🎉**

