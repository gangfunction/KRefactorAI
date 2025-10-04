# KRefactorAI 설치 가이드

KRefactorAI를 다른 프로젝트에서 사용하는 방법을 안내합니다.

## 📦 설치 방법

### 1. JitPack 사용 (가장 간단함) ⭐ 권장

JitPack은 GitHub 저장소에서 직접 라이브러리를 빌드하고 배포하는 서비스입니다.

#### Gradle (Kotlin DSL)

**Step 1**: `settings.gradle.kts`에 JitPack 저장소 추가

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
    }
}
```

또는 `build.gradle.kts`에 직접 추가:

```kotlin
repositories {
    mavenCentral()
    maven { url = uri("https://jitpack.io") }
}
```

**Step 2**: 의존성 추가

```kotlin
dependencies {
    // 최신 master 브랜치 사용
    implementation("com.github.gangfunction:KRefactorAI:master-SNAPSHOT")
    
    // 또는 특정 커밋 사용 (안정적)
    implementation("com.github.gangfunction:KRefactorAI:2b9634e")
    
    // 또는 특정 태그/릴리스 사용 (릴리스 후)
    // implementation("com.github.gangfunction:KRefactorAI:v0.1.0")
}
```

#### Gradle (Groovy)

```groovy
repositories {
    mavenCentral()
    maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.gangfunction:KRefactorAI:master-SNAPSHOT'
}
```

#### Maven

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>com.github.gangfunction</groupId>
        <artifactId>KRefactorAI</artifactId>
        <version>master-SNAPSHOT</version>
    </dependency>
</dependencies>
```

---

### 2. GitHub Packages 사용

GitHub Packages는 GitHub에서 제공하는 패키지 저장소입니다.

#### Step 1: GitHub Personal Access Token 생성

1. GitHub 로그인 → Settings
2. Developer settings → Personal access tokens → Tokens (classic)
3. "Generate new token (classic)" 클릭
4. 권한 선택:
   - ✅ `read:packages` (패키지 읽기)
5. "Generate token" 클릭 후 토큰 복사

#### Step 2: 인증 정보 설정

**방법 A**: `~/.gradle/gradle.properties` 파일에 추가 (권장)

```properties
gpr.user=YOUR_GITHUB_USERNAME
gpr.token=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

**방법 B**: 환경 변수 사용

```bash
export GITHUB_ACTOR=YOUR_GITHUB_USERNAME
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### Step 3: build.gradle.kts 설정

```kotlin
repositories {
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/gangfunction/KRefactorAI")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
            password = project.findProperty("gpr.token") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    implementation("io.github.gangfunction:krefactorai:0.1.0-SNAPSHOT")
}
```

---

### 3. 로컬 빌드 및 설치

개발 중이거나 소스 코드를 수정하고 싶을 때 사용합니다.

#### Step 1: 저장소 클론

```bash
git clone https://github.com/gangfunction/KRefactorAI.git
cd KRefactorAI
```

#### Step 2: 로컬 Maven 저장소에 설치

```bash
./gradlew publishToMavenLocal
```

성공 메시지:
```
BUILD SUCCESSFUL in 10s
Published to ~/.m2/repository/io/github/gangfunction/krefactorai/0.1.0-SNAPSHOT/
```

#### Step 3: 다른 프로젝트에서 사용

```kotlin
repositories {
    mavenLocal()  // 로컬 Maven 저장소 추가
    mavenCentral()
}

dependencies {
    implementation("io.github.gangfunction:krefactorai:0.1.0-SNAPSHOT")
}
```

---

## 🔧 설정

### OpenAI API 키 설정 (AI 기능 사용 시)

AI 기반 리팩토링 제안을 받으려면 OpenAI API 키가 필요합니다.

#### 방법 1: 환경 변수 (권장)

```bash
export OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### 방법 2: 프로젝트 루트에 `.env` 파일 생성

```
OPENAI_API_KEY=sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
```

#### 방법 3: 코드에서 직접 설정

```kotlin
import io.github.gangfunction.krefactorai.config.ApiKeyManager

ApiKeyManager.setApiKey("sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx")
```

---

## ✅ 설치 확인

설치가 제대로 되었는지 확인하는 간단한 코드:

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    println(KRefactorAI.getInfo())
}
```

출력 예시:
```
╔══════════════════════════════════════════════════════════════╗
║                        KRefactorAI                           ║
╠══════════════════════════════════════════════════════════════╣
║ Library: KRefactorAI                                         ║
║ Version: 0.1.0-SNAPSHOT                                      ║
║ Description: Untangle Dependencies with AI and Math          ║
╚══════════════════════════════════════════════════════════════╝
```

---

## 🚀 빠른 시작

### 기본 사용 예제

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    // AI 기능 활성화 (OPENAI_API_KEY 필요)
    val refactorAI = KRefactorAI(enableAI = true)
    
    // 프로젝트 자동 분석
    val result = refactorAI.analyzeProject("/path/to/your/project")
    
    println("✅ 분석 완료!")
    println("프로젝트 타입: ${result.projectType}")
    println("모듈 수: ${result.graph.getModules().size}")
    println("의존성 수: ${result.graph.getDependencies().size}")
    
    // AI 기반 리팩토링 계획 생성
    val plan = refactorAI.analyze(result.graph, includeAISuggestions = true)
    
    // 결과 출력
    println(plan)
}
```

### AI 없이 사용 (무료)

```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    // AI 기능 비활성화 (API 키 불필요)
    val refactorAI = KRefactorAI(enableAI = false)
    
    val result = refactorAI.analyzeProject("/path/to/your/project")
    val plan = refactorAI.quickAnalyze(result.graph)
    
    println(plan)
}
```

---

## 📚 더 많은 예제

- [기본 사용법](examples/BasicExample.kt)
- [자동 분석](examples/AutoAnalysisExample.kt)
- [API 문서](https://github.com/gangfunction/KRefactorAI/wiki)

---

## 🐛 문제 해결

### "Could not resolve dependency" 오류

**JitPack 사용 시**:
1. JitPack 저장소가 올바르게 추가되었는지 확인
2. 인터넷 연결 확인
3. Gradle 캐시 삭제: `./gradlew clean --refresh-dependencies`

**GitHub Packages 사용 시**:
1. GitHub Token이 올바른지 확인
2. `read:packages` 권한이 있는지 확인
3. 인증 정보가 올바르게 설정되었는지 확인

### "OPENAI_API_KEY not found" 경고

AI 기능을 사용하지 않으면 무시해도 됩니다. AI 기능을 사용하려면:
1. OpenAI API 키 발급: https://platform.openai.com/api-keys
2. 환경 변수 설정: `export OPENAI_API_KEY=sk-...`

### 빌드 실패

1. Java 17 이상 설치 확인: `java -version`
2. Gradle 버전 확인: `./gradlew --version`
3. 의존성 업데이트: `./gradlew build --refresh-dependencies`

---

## 💬 지원

- 이슈 리포트: [GitHub Issues](https://github.com/gangfunction/KRefactorAI/issues)
- 기능 요청: [GitHub Discussions](https://github.com/gangfunction/KRefactorAI/discussions)
- 이메일: gangfunction@gmail.com

---

## 📄 라이선스

MIT License - 자유롭게 사용, 수정, 배포 가능합니다.

