# Brahma AI Mobile: Sovereign On-Device Autonomous Intelligence

Brahma AI is an enterprise-grade, autonomous on-device cognitive engine and digital assistant built for Android. It combines multi-model generative intelligence (Google Gemini & OpenRouter) with real-time screen automation, biometric hardware sentinel defense, and low-latency streaming neural voice interactions.

---

## 🏛️ System Architecture

```
BrahmaAI-Mobile/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/brahma/assistant/
│   │   │   │   ├── ai/              # Gemini SSE streaming & OpenRouter fallback clients
│   │   │   │   ├── memory/          # Memory Cortex (semantic knowledge & episodic store)
│   │   │   │   ├── planner/         # OmniPlanner multi-agent DAG task decomposition
│   │   │   │   ├── heal/            # ExecutionReflectionEngine & AutoHealManager
│   │   │   │   ├── actions/         # BrahmaActionRegistry & OS hardware dispatchers
│   │   │   │   ├── automation/      # AccessibilityService screen node traversal & gestures
│   │   │   │   ├── sentinel/        # ML Kit Face verification & DeviceAdmin lockscreen
│   │   │   │   ├── audio/           # SentenceChunker & low-latency neural TTS VoiceManager
│   │   │   │   ├── service/         # Floating Overlay Orb, NotificationListener & Proactive daemon
│   │   │   │   ├── wallpaper/       # 3D Quantum Core live wallpaper engine
│   │   │   │   └── ui/              # Jetpack Compose HUD, MVI ViewModel & Navigation
│   │   │   └── AndroidManifest.xml
│   │   └── test/                    # Comprehensive unit tests
│   └── build.gradle.kts
├── .github/
│   └── workflows/
│       └── android-apk-build.yml    # Universal APK Automation & GitHub Release CI/CD
└── settings.gradle.kts
```

---

## 🚀 Key Subsystems

1. **Gemini Streaming Client (`GeminiMobileClient`)**:
   - HTTP/2 Server-Sent Events (SSE) streaming with non-blocking Kotlin Coroutines (`Flow<String>`).
   - `NetworkResilience` auto-retry with exponential backoff and jitter.
   - Structured JSON schema generation via Gemini `responseSchema`.

2. **OmniPlanner Multi-Agent Engine (`OmniPlanner`)**:
   - Translates complex directives (e.g., `"plan: diagnose battery and open wifi settings"`) into structured execution DAGs (`PlanStep`).
   - Roles: `ARCHITECT`, `NAVIGATOR`, `EXECUTOR`, `AUDITOR`, `HEALER`.

3. **Memory Cortex (`BrahmaMemoryCortex`)**:
   - Dual-tier memory: short-term rolling conversational buffer + persistent semantic facts (`MemoryItem`).
   - Automatically consolidates user preferences and context across app lifecycles.

4. **Screen Automation Bridge (`BrahmaAccessibilityService`)**:
   - Traverses `AccessibilityNodeInfo` hierarchy trees.
   - Dispatches programmatic clicks, fuzzy text matching, and global gestures (`BACK`, `HOME`, `RECENTS`).

5. **Sentinel Biometric Security (`SentinelBiometricManager`)**:
   - On-device facial feature extraction with ML Kit.
   - Triggers `SentinelLockActivity` and hardware lock upon unauthorized physical device tampering.

6. **Interactive 3D Live Wallpaper (`BrahmaLiveWallpaperService`)**:
   - Renders a pulsing Quantum Core on the device launcher with touch-reactive ripples.

---

## 🛠️ Build and Compilation

### Prerequisites
- JDK 17
- Android SDK 34 (API 34)

### Building Debug APK Locally
```bash
./gradlew assembleDebug
```
The APK is generated at:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Automated Test Suite
```bash
./gradlew test
```

---

## 🤖 Universal CI/CD Automation

Every push to `main` triggers `.github/workflows/android-apk-build.yml` which:
1. Configures JDK 17 and Gradle.
2. Runs the full test suite.
3. Compiles the Android Debug APK.
4. Uploads downloadable artifacts (`brahma-ai-debug-apk`).
5. Publishes a GitHub Release automatically when tagged (e.g. `v1.0.1`).
