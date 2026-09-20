# VoiceRep - 안드로이드 헬스 음성 카운팅 앱 (Voice-Powered Fitness Counter)

**VoiceRep**은 웨이트 트레이닝 중 손으로 기기를 조작할 필요 없이 기합 및 호흡/음성 발성을 실시간으로 감지하여 반복 횟수(Reps)를 자동 카운팅하고, 세트와 운동 기록을 관리해주는 안드로이드 피트니스 어시스턴트 애플리케이션입니다.

---

## 🛠 기술 스택 (Tech Stack)

* **Language**: Kotlin 2.0+
* **UI**: Jetpack Compose (Material 3)
* **Architecture**: Modern Android Architecture (MVVM + Clean Architecture)
* **Local DB**: Room Database (SQLite with Foreign Keys & Cascade)
* **Audio Capture & Analysis**: Android Native `AudioRecord` (16kHz Mono PCM, Dynamic RMS/dBFS)
* **Background Support**: Foreground Service (`FOREGROUND_SERVICE_MICROPHONE`) & WakeLock
* **Feedback Engine**: TextToSpeech (TTS) & ToneGenerator / Android Vibrator
* **Preferences**: Jetpack DataStore (Preferences)

---

## 🏗 프로젝트 디렉토리 구조

```
VoiceRep/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── java/com/voicerep/app/
│   │   │       ├── VoiceRepApplication.kt
│   │   │       ├── MainActivity.kt
│   │   │       ├── audio/
│   │   │       │   ├── AudioConfig.kt
│   │   │       │   ├── AudioRmsCalculator.kt
│   │   │       │   ├── AudioRecordHelper.kt
│   │   │       │   ├── AudioEngineState.kt
│   │   │       │   ├── AudioEngineConfig.kt
│   │   │       │   └── AudioEngine.kt
│   │   │       ├── data/
│   │   │       │   ├── local/
│   │   │       │   │   ├── entity/ (WorkoutEntity, SetEntity, WorkoutWithSets)
│   │   │       │   │   ├── dao/ (WorkoutDao, SetDao)
│   │   │       │   │   └── VoiceRepDatabase.kt
│   │   │       │   ├── repository/WorkoutRepository.kt
│   │   │       │   └── preference/UserPreferencesRepository.kt
│   │   │       ├── feedback/
│   │   │       │   └── FeedbackManager.kt
│   │   │       ├── service/
│   │   │       │   └── AudioRecordingService.kt
│   │   │       └── ui/
│   │   │           ├── theme/ (Color, Theme, Type)
│   │   │           ├── screens/ (AudioMeterScreen)
│   │   │           ├── session/ (WorkoutSessionScreen, ViewModel, RestTimer)
│   │   │           ├── history/ (HistoryScreen)
│   │   │           ├── analytics/ (AnalyticsScreen)
│   │   │           ├── workouts/ (WorkoutManagementScreen)
│   │   │           ├── settings/ (SettingsScreen)
│   │   │           └── navigation/ (AppNavigation)
│   │   ├── test/
│   │   │   └── java/com/voicerep/app/
│   │   │       ├── audio/AudioEngineTest.kt
│   │   │       └── data/WorkoutDataTest.kt
│   │   └── androidTest/
│   │       └── java/com/voicerep/app/data/RoomDaoTest.kt
│   ├── build.gradle.kts
│   └── proguard-rules.pro
├── docs/
│   ├── spec.md       # 전체 기능 및 기술 명세서
│   └── ROADMAP.md    # 단계별 구현 로드맵 및 검증 현황
├── gradle/libs.versions.toml
├── build.gradle.kts
├── settings.gradle.kts
└── gradle.properties
```

---

## ⚡ 주요 핵심 구현 내용

1. **오디오 엔진 & 오인식 방지 필터링 (`AudioEngine.kt`)**
   - **소음 캘리브레이션 (Calibration)**: 세트 시작 시 주변 헬스장 소음(음악, 기구 소리 등)의 Noise Floor를 자동 측정.
   - **충돌음 배제 (Spike Filter)**: 100ms 미만의 덤벨 내려놓는 소리, 박수 소리 등 단발성 충돌음 무시.
   - **장기 소음 배제 (Long Duration Filter)**: 700ms 이상의 긴 대화나 음악 구절 무시.
   - **디바운스 쿨다운 (Debounce)**: 1회 카운트 후 1,200ms 동안 재트리거 방지 (호흡음이나 중복 발성 배제).

2. **휴식 타이머 & 오디오/햅틱 피드백**
   - 세트 완료 시 목표 달성 축하 음성(TTS) 및 자동 휴식 타이머 카운트다운 시작.
   - 휴식 종료 5초 전 비프 경고음.

3. **백그라운드 지원 (`AudioRecordingService.kt`)**
   - 세트 도중 화면을 끄거나 주머니에 넣어도 마이크 캡처 및 음성 카운팅이 중단되지 않도록 Foreground Service 및 WakeLock 구성.

---

## 🧪 빌드 및 테스트 방법

### 1. 단위 테스트 실행 (JVM Unit Tests)
```bash
./gradlew testDebugUnitTest
```
* `AudioEngineTest`: 가상 PCM 신호(충돌음, 유효 발성, 디바운스, 지속시간 초과) 판정 로직 검증.
* `WorkoutDataTest`: 볼륨 계산 및 엔티티 유효성 검증.

### 2. 안드로이드 빌드
```bash
./gradlew assembleDebug
```
