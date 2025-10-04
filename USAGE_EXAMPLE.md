# KRefactorAI 사용 예제

## 📝 목차

1. [기본 사용법](#기본-사용법)
2. [프로젝트 자동 분석](#프로젝트-자동-분석)
3. [수동 그래프 구성](#수동-그래프-구성)
4. [AI 기능 사용](#ai-기능-사용)
5. [결과 저장](#결과-저장)

---

## 기본 사용법

### 1. 간단한 프로젝트 분석

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    // KRefactorAI 인스턴스 생성 (AI 비활성화)
    val refactorAI = KRefactorAI(enableAI = false)
    
    // 프로젝트 분석
    val result = refactorAI.analyzeProject(".")
    
    // 결과 출력
    println("프로젝트 타입: ${result.projectType}")
    println("모듈 수: ${result.modulesFound}")
    println("의존성 수: ${result.dependenciesFound}")
    
    // 리팩토링 계획 생성
    val plan = refactorAI.quickAnalyze(result.graph)
    println(plan)
}
```

**출력 예시:**
```
프로젝트 타입: GRADLE_KOTLIN
모듈 수: 8
의존성 수: 16

=== Refactoring Plan ===
Total Modules: 8
Total Complexity: 12.45
Estimated Time: 2 hours 30 minutes

Steps:
1. io.github.example.model (priority=1, complexity=1.00)
2. io.github.example.util (priority=2, complexity=0.85)
...
```

---

## 프로젝트 자동 분석

### 2. Gradle 프로젝트 분석

```kotlin
import io.github.gangfunction.krefactorai.analyzer.AutoProjectAnalyzer
import java.nio.file.Paths

fun main() {
    val analyzer = AutoProjectAnalyzer()
    
    // 프로젝트 경로 지정
    val projectPath = Paths.get("/path/to/your/gradle/project")
    
    // 자동 분석
    val result = analyzer.analyze(projectPath)
    
    println("✅ 분석 완료!")
    println("프로젝트 타입: ${result.projectType}")
    println("발견된 모듈: ${result.modulesFound}개")
    println("발견된 의존성: ${result.dependenciesFound}개")
    
    // 경고 확인
    if (result.warnings.isNotEmpty()) {
        println("\n⚠️ 경고:")
        result.warnings.forEach { println("  - $it") }
    }
    
    // 순환 의존성 확인
    val cycles = result.graph.detectCircularDependencies()
    if (cycles.isNotEmpty()) {
        println("\n🔄 순환 의존성 발견:")
        cycles.forEach { cycle ->
            println("  ${cycle.joinToString(" -> ") { it.name }}")
        }
    }
}
```

### 3. Maven 프로젝트 분석

```kotlin
import io.github.gangfunction.krefactorai.analyzer.MavenProjectAnalyzer
import java.nio.file.Paths

fun main() {
    val analyzer = MavenProjectAnalyzer()
    val projectPath = Paths.get("/path/to/your/maven/project")
    
    val graph = analyzer.analyze(projectPath)
    
    println("모듈 수: ${graph.getModules().size}")
    println("의존성 수: ${graph.getDependencies().size}")
}
```

---

## 수동 그래프 구성

### 4. 직접 의존성 그래프 만들기

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI
import io.github.gangfunction.krefactorai.graph.DependencyGraph
import io.github.gangfunction.krefactorai.model.Module
import io.github.gangfunction.krefactorai.model.Dependency

fun main() {
    // 빈 그래프 생성
    val graph = DependencyGraph()
    
    // 모듈 생성
    val moduleA = Module("com.example.core", "/src/core")
    val moduleB = Module("com.example.api", "/src/api")
    val moduleC = Module("com.example.service", "/src/service")
    val moduleD = Module("com.example.web", "/src/web")
    
    // 그래프에 모듈 추가
    graph.addModule(moduleA)
    graph.addModule(moduleB)
    graph.addModule(moduleC)
    graph.addModule(moduleD)
    
    // 의존성 추가
    // web -> service -> api -> core
    graph.addDependency(Dependency(moduleD, moduleC))
    graph.addDependency(Dependency(moduleC, moduleB))
    graph.addDependency(Dependency(moduleB, moduleA))
    
    // 분석
    val refactorAI = KRefactorAI(enableAI = false)
    val plan = refactorAI.analyze(graph)
    
    println(plan)
}
```

---

## AI 기능 사용

### 5. AI 기반 리팩토링 제안 받기

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    // AI 활성화 (OPENAI_API_KEY 환경 변수 필요)
    val refactorAI = KRefactorAI(
        enableAI = true,
        aiModel = "gpt-4"  // 또는 "gpt-3.5-turbo"
    )
    
    // 프로젝트 분석
    val result = refactorAI.analyzeProject(".")
    
    // AI 제안 포함하여 계획 생성
    val plan = refactorAI.analyze(result.graph, includeAISuggestions = true)
    
    // 각 모듈별 AI 제안 확인
    plan.modules.forEach { step ->
        println("\n" + "=".repeat(70))
        println("모듈: ${step.module.name}")
        println("우선순위: ${step.priority}")
        println("복잡도: ${"%.2f".format(step.complexityScore)}")
        println("의존성: ${step.dependencies.size}개")
        println("의존하는 모듈: ${step.dependents.size}개")
        
        if (step.aiSuggestion != null) {
            println("\n🤖 AI 제안:")
            println(step.aiSuggestion)
        }
    }
}
```

**AI 제안 출력 예시:**
```
======================================================================
모듈: com.example.service
우선순위: 3
복잡도: 0.85
의존성: 2개
의존하는 모듈: 3개

🤖 AI 제안:
### 🎯 Refactoring Actions
- [ ] Extract interface for better abstraction
- [ ] Use sealed class for type safety
- [ ] Apply dependency injection pattern

### 📝 Implementation Steps
- [ ] Step 1: Identify classes to refactor
- [ ] Step 2: Create new interfaces/classes
- [ ] Step 3: Update dependencies
- [ ] Step 4: Run tests and verify

### ⚠️ Risks & Mitigation
- [ ] Risk: Breaking changes → Mitigation: Use deprecation warnings
- [ ] Risk: Performance impact → Mitigation: Add benchmarks
```

### 6. 커스텀 AI 프롬프트

```kotlin
import io.github.gangfunction.krefactorai.ai.OpenAIClient
import kotlinx.coroutines.runBlocking

fun main() = runBlocking {
    val client = OpenAIClient(
        apiKey = "your-api-key",
        model = "gpt-4"
    )
    
    val suggestion = client.generateRefactoringSuggestion(
        moduleName = "com.example.service",
        dependencies = listOf("com.example.api", "com.example.core"),
        dependents = listOf("com.example.web"),
        complexityScore = 0.85
    )
    
    println(suggestion)
    
    client.close()
}
```

---

## 결과 저장

### 7. 마크다운 파일로 저장

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

fun main() {
    val refactorAI = KRefactorAI(enableAI = true)
    val result = refactorAI.analyzeProject(".")
    val plan = refactorAI.analyze(result.graph, includeAISuggestions = true)
    
    // 마크다운 생성
    val markdown = buildString {
        appendLine("# 리팩토링 계획")
        appendLine()
        appendLine("**생성 시간**: ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)}")
        appendLine("**프로젝트**: ${Paths.get(".").toAbsolutePath().normalize()}")
        appendLine()
        
        appendLine("## 요약")
        appendLine("- 총 모듈: ${plan.modules.size}")
        appendLine("- 순환 의존성: ${plan.circularDependencies.size}")
        appendLine("- 총 복잡도: ${"%.2f".format(plan.totalComplexity)}")
        appendLine("- 예상 시간: ${plan.estimatedTime}")
        appendLine()
        
        plan.modules.forEach { step ->
            appendLine("## ${step.priority}. ${step.module.name}")
            appendLine()
            appendLine("**복잡도**: ${"%.2f".format(step.complexityScore)}")
            appendLine("**의존성**: ${step.dependencies.size} | **의존하는 모듈**: ${step.dependents.size}")
            appendLine()
            
            if (step.aiSuggestion != null) {
                appendLine(step.aiSuggestion)
            }
            appendLine()
        }
    }
    
    // 파일 저장
    val outputPath = Paths.get("REFACTORING_PLAN.md")
    Files.writeString(outputPath, markdown)
    
    println("✅ 리팩토링 계획이 저장되었습니다: $outputPath")
}
```

### 8. JSON으로 저장

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.nio.file.Files
import java.nio.file.Paths

fun main() {
    val refactorAI = KRefactorAI(enableAI = false)
    val result = refactorAI.analyzeProject(".")
    val plan = refactorAI.analyze(result.graph)
    
    // JSON 직렬화
    val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    
    val jsonString = json.encodeToString(plan)
    
    // 파일 저장
    val outputPath = Paths.get("refactoring_plan.json")
    Files.writeString(outputPath, jsonString)
    
    println("✅ JSON 파일이 저장되었습니다: $outputPath")
}
```

---

## 고급 사용법

### 9. 복잡도 분석

```kotlin
import io.github.gangfunction.krefactorai.core.RefactoringOrderCalculator

fun main() {
    val refactorAI = KRefactorAI(enableAI = false)
    val result = refactorAI.analyzeProject(".")
    
    val calculator = RefactoringOrderCalculator(result.graph)
    val plan = calculator.calculateRefactoringOrder()
    
    // 복잡도 순으로 정렬
    val sortedByComplexity = plan.modules.sortedByDescending { it.complexityScore }
    
    println("복잡도가 높은 모듈 Top 5:")
    sortedByComplexity.take(5).forEach { step ->
        println("${step.module.name}: ${"%.2f".format(step.complexityScore)}")
    }
}
```

### 10. 순환 의존성 상세 분석

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    val refactorAI = KRefactorAI(enableAI = false)
    val result = refactorAI.analyzeProject(".")
    
    val cycles = result.graph.detectCircularDependencies()
    
    if (cycles.isEmpty()) {
        println("✅ 순환 의존성이 없습니다!")
    } else {
        println("⚠️ ${cycles.size}개의 순환 의존성 발견:")
        cycles.forEachIndexed { index, cycle ->
            println("\n순환 ${index + 1}:")
            cycle.forEach { module ->
                println("  → ${module.name}")
            }
            println("  → ${cycle.first().name} (순환 완성)")
        }
    }
}
```

---

## 💡 팁

1. **AI 기능 비용 절감**: `quickAnalyze()`를 사용하면 AI 호출 없이 빠르게 분석할 수 있습니다.

2. **대규모 프로젝트**: 프로젝트가 크면 분석에 시간이 걸릴 수 있습니다. 진행 상황을 로깅하세요.

3. **캐싱**: 같은 프로젝트를 여러 번 분석할 때는 결과를 캐싱하여 재사용하세요.

4. **테스트**: 리팩토링 전후로 테스트를 실행하여 기능이 깨지지 않았는지 확인하세요.

---

## 📚 더 보기

- [API 문서](https://github.com/gangfunction/KRefactorAI/wiki)
- [설치 가이드](INSTALLATION.md)
- [기여 가이드](CONTRIBUTING.md)

