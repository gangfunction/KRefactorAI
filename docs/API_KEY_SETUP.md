# OpenAI API 키 설정 가이드

KRefactorAI는 OpenAI API를 사용하여 지능적인 리팩토링 제안을 생성합니다. 이 가이드는 API 키를 안전하게 설정하는 방법을 안내합니다.

---

## 📋 목차
1. [API 키 발급받기](#1-api-키-발급받기)
2. [환경 변수 설정하기](#2-환경-변수-설정하기)
3. [설정 확인하기](#3-설정-확인하기)
4. [문제 해결](#4-문제-해결)
5. [보안 주의사항](#5-보안-주의사항)

---

## 1. API 키 발급받기

### 1.1 OpenAI 계정 생성
1. [OpenAI Platform](https://platform.openai.com/) 접속
2. **Sign up** 버튼 클릭하여 계정 생성
3. 이메일 인증 완료

### 1.2 API 키 생성
1. 로그인 후 우측 상단 프로필 클릭
2. **View API keys** 선택
3. **Create new secret key** 버튼 클릭
4. 키 이름 입력 (예: "KRefactorAI-Dev")
5. **Create secret key** 클릭
6. 🔑 **생성된 키를 즉시 복사** (다시 확인 불가!)

```
예시: sk-proj-abc123def456ghi789jkl012mno345pqr678stu901vwx234yz
```

### 1.3 결제 정보 등록
- API 사용을 위해 결제 정보 등록 필요
- **Settings** → **Billing** → **Add payment method**
- 사용량 제한 설정 권장 (예: 월 $10)

---

## 2. 환경 변수 설정하기

### 2.1 macOS / Linux

#### 방법 1: 터미널에서 임시 설정
현재 터미널 세션에만 적용됩니다.

```bash
export OPENAI_API_KEY="sk-proj-your-actual-api-key-here"
```

#### 방법 2: 영구 설정 (Bash)
```bash
# .bashrc 파일에 추가
echo 'export OPENAI_API_KEY="sk-proj-your-actual-api-key-here"' >> ~/.bashrc

# 변경사항 적용
source ~/.bashrc
```

#### 방법 3: 영구 설정 (Zsh - macOS 기본)
```bash
# .zshrc 파일에 추가
echo 'export OPENAI_API_KEY="sk-proj-your-actual-api-key-here"' >> ~/.zshrc

# 변경사항 적용
source ~/.zshrc
```

#### 방법 4: 영구 설정 (Fish Shell)
```fish
# config.fish 파일에 추가
echo 'set -gx OPENAI_API_KEY "sk-proj-your-actual-api-key-here"' >> ~/.config/fish/config.fish

# 변경사항 적용
source ~/.config/fish/config.fish
```

---

### 2.2 Windows

#### 방법 1: PowerShell (임시)
```powershell
$env:OPENAI_API_KEY="sk-proj-your-actual-api-key-here"
```

#### 방법 2: PowerShell (영구)
```powershell
[System.Environment]::SetEnvironmentVariable(
    'OPENAI_API_KEY', 
    'sk-proj-your-actual-api-key-here', 
    'User'
)
```

#### 방법 3: Command Prompt (영구)
```cmd
setx OPENAI_API_KEY "sk-proj-your-actual-api-key-here"
```
⚠️ 설정 후 터미널을 재시작해야 적용됩니다.

#### 방법 4: GUI로 설정
1. **시작** → **시스템 환경 변수 편집** 검색
2. **환경 변수** 버튼 클릭
3. **사용자 변수** 섹션에서 **새로 만들기** 클릭
4. 변수 이름: `OPENAI_API_KEY`
5. 변수 값: `sk-proj-your-actual-api-key-here`
6. **확인** 클릭

---

### 2.3 IDE 설정 (IntelliJ IDEA / Android Studio)

#### 실행 구성에 추가
1. **Run** → **Edit Configurations** 메뉴 선택
2. 실행할 구성 선택 (또는 새로 생성)
3. **Environment variables** 필드 찾기
4. 우측 폴더 아이콘 클릭
5. **+** 버튼 클릭
6. Name: `OPENAI_API_KEY`
7. Value: `sk-proj-your-actual-api-key-here`
8. **OK** 클릭

#### .env 파일 사용 (EnvFile 플러그인)
1. **Plugins** → **EnvFile** 설치
2. 프로젝트 루트에 `.env` 파일 생성:
   ```properties
   OPENAI_API_KEY=sk-proj-your-actual-api-key-here
   ```
3. **Run** → **Edit Configurations**
4. **EnvFile** 탭에서 `.env` 파일 추가
5. ⚠️ `.gitignore`에 `.env` 추가 필수!

---

### 2.4 Docker 환경

#### docker-compose.yml
```yaml
version: '3.8'
services:
  krefactorai:
    image: your-image
    environment:
      - OPENAI_API_KEY=${OPENAI_API_KEY}
    # 또는 직접 지정 (비권장)
    # environment:
    #   - OPENAI_API_KEY=sk-proj-your-actual-api-key-here
```

#### .env 파일 (Docker Compose)
```properties
# .env
OPENAI_API_KEY=sk-proj-your-actual-api-key-here
```

#### Docker run 명령어
```bash
docker run -e OPENAI_API_KEY="sk-proj-your-actual-api-key-here" your-image
```

---

## 3. 설정 확인하기

### 3.1 터미널에서 확인

#### macOS / Linux
```bash
echo $OPENAI_API_KEY
```

#### Windows (PowerShell)
```powershell
echo $env:OPENAI_API_KEY
```

#### Windows (Command Prompt)
```cmd
echo %OPENAI_API_KEY%
```

**예상 출력**:
```
sk-proj-abc123def456ghi789jkl012mno345pqr678stu901vwx234yz
```

### 3.2 Kotlin 코드로 확인
```kotlin
fun main() {
    val apiKey = System.getenv("OPENAI_API_KEY")
    
    if (apiKey.isNullOrBlank()) {
        println("❌ API 키가 설정되지 않았습니다.")
        println("설정 가이드: https://github.com/gangfunction/KRefactorAI/blob/main/docs/API_KEY_SETUP.md")
    } else {
        println("✅ API 키가 정상적으로 설정되었습니다.")
        println("키 앞 10자: ${apiKey.take(10)}...")
    }
}
```

### 3.3 KRefactorAI로 확인
```kotlin
import io.github.gangfunction.krefactorai.KRefactorAI

fun main() {
    try {
        val refactorAI = KRefactorAI()
        println("✅ KRefactorAI 초기화 성공!")
    } catch (e: IllegalStateException) {
        println("❌ 오류: ${e.message}")
    }
}
```

---

## 4. 문제 해결

### 4.1 "OPENAI_API_KEY environment variable is not set" 오류

#### 원인
환경 변수가 설정되지 않았거나 현재 세션에 적용되지 않음

#### 해결 방법
1. 환경 변수 설정 명령어 재실행
2. 터미널/IDE 재시작
3. 설정 확인 명령어로 검증

---

### 4.2 "Invalid API Key" 오류 (401 Unauthorized)

#### 원인
- API 키가 잘못 복사됨
- API 키가 비활성화됨
- 결제 정보 미등록

#### 해결 방법
1. [OpenAI Platform](https://platform.openai.com/api-keys)에서 키 상태 확인
2. 키를 다시 복사하여 설정 (공백 제거 확인)
3. 결제 정보 등록 확인
4. 필요시 새 키 생성

---

### 4.3 "Rate Limit Exceeded" 오류 (429)

#### 원인
- API 호출 한도 초과
- 무료 티어 제한

#### 해결 방법
1. [Usage Dashboard](https://platform.openai.com/usage)에서 사용량 확인
2. 요금제 업그레이드 고려
3. KRefactorAI 호출 빈도 조절

---

### 4.4 IDE에서 환경 변수 인식 안 됨

#### 해결 방법
1. IDE 완전 재시작 (캐시 무효화)
2. Run Configuration에 직접 추가
3. `.env` 파일 + EnvFile 플러그인 사용

---

## 5. 보안 주의사항

### 5.1 절대 하지 말아야 할 것 ⛔

#### ❌ 코드에 하드코딩
```kotlin
// 절대 이렇게 하지 마세요!
val apiKey = "sk-proj-abc123def456..." 
```

#### ❌ Git에 커밋
```bash
# .env 파일을 실수로 커밋하지 마세요!
git add .env  # ❌ 위험!
```

#### ❌ 공개 저장소에 노출
- GitHub, GitLab 등 공개 저장소에 API 키 업로드 금지
- 실수로 업로드 시 즉시 키 삭제 및 재발급

---

### 5.2 권장 보안 사항 ✅

#### ✅ .gitignore에 추가
```gitignore
# .gitignore
.env
.env.local
*.env
```

#### ✅ 환경 변수만 사용
```kotlin
// 올바른 방법
val apiKey = System.getenv("OPENAI_API_KEY") 
    ?: throw IllegalStateException("API key not found")
```

#### ✅ 키 권한 제한
- OpenAI Platform에서 키별 권한 설정
- 개발/프로덕션 키 분리

#### ✅ 정기적인 키 교체
- 3-6개월마다 키 재발급 권장
- 이전 키는 즉시 삭제

#### ✅ 사용량 모니터링
- [Usage Dashboard](https://platform.openai.com/usage)에서 이상 활동 감지
- 예산 알림 설정

---

### 5.3 키 노출 시 대응

#### 1단계: 즉시 키 삭제
1. [API Keys 페이지](https://platform.openai.com/api-keys) 접속
2. 노출된 키 옆 **Delete** 클릭

#### 2단계: 새 키 발급
1. **Create new secret key** 클릭
2. 새 키를 안전하게 저장

#### 3단계: Git 히스토리 정리 (필요시)
```bash
# BFG Repo-Cleaner 사용
bfg --replace-text passwords.txt

# 또는 git filter-branch
git filter-branch --force --index-filter \
  "git rm --cached --ignore-unmatch .env" \
  --prune-empty --tag-name-filter cat -- --all
```

---

## 6. 추가 리소스

### 공식 문서
- [OpenAI API Documentation](https://platform.openai.com/docs)
- [API Key Safety Best Practices](https://help.openai.com/en/articles/5112595-best-practices-for-api-key-safety)

### KRefactorAI 문서
- [README](../README.md)
- [요구사항 명세서](../REQUIREMENTS.md)
- [사용 가이드](./USAGE.md)

### 문의
- GitHub Issues: [KRefactorAI Issues](https://github.com/gangfunction/KRefactorAI/issues)
- Email: gangfunction@gmail.com

---

## 7. 체크리스트

설정 완료 전 확인하세요:

- [ ] OpenAI API 키 발급 완료
- [ ] 결제 정보 등록 완료
- [ ] 환경 변수 `OPENAI_API_KEY` 설정 완료
- [ ] 터미널/IDE에서 환경 변수 확인 완료
- [ ] `.env` 파일을 `.gitignore`에 추가 (사용 시)
- [ ] KRefactorAI 초기화 테스트 성공
- [ ] 사용량 제한 설정 완료 (선택)

모든 항목을 체크했다면 KRefactorAI를 사용할 준비가 완료되었습니다! 🎉

