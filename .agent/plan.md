# Project Plan

AccessEd: An Android app for inclusive education. Key features: Live Lecture Transcription (STT), ML-Powered Text Simplification, Smart OCR & TTS Reader (CameraX, ML Kit), and an Emergency Safety Hub. Built with Kotlin, Jetpack Compose, and Material 3.

## Project Brief

# AccessEd - Project Brief

AccessEd is an inclusive educational tool designed to empower students with disabilities by removing barriers to learning. The app provides real-time assistive technologies to ensure every student can engage with educational content effectively, regardless of their visual or auditory needs.

### **Features**
*   **Live Lecture Transcription**: Provides real-time speech-to-text conversion of classroom lectures, allowing students with hearing impairments to follow along with high accuracy.
*   **ML-Powered Text Simplification**: Analyzes complex sentences from digital documents or scanned text and provides simplified explanations to improve comprehension for students with cognitive or learning disabilities.
*   **Smart OCR & TTS Reader**: Utilizes the device camera to scan physical handouts and textbooks, converting them into accessible digital text that is read aloud via a high-quality text-to-speech engine.
*   **Emergency Safety Hub**: A dedicated quick-access portal for contacting emergency services (ambulance) and guardians, alongside interactive safety guides for immediate assistance.

### **High-Level Technical Stack**
*   **Kotlin**: The core language used for a modern, concise, and safe codebase.
*   **Jetpack Compose**: For building a vibrant, Material 3-compliant UI with an emphasis on accessibility (high contrast, dynamic scaling).
*   **KSP (Kotlin Symbol Processing)**: Leveraged for efficient code generation (e.g., for JSON parsing and dependency management).
*   **Kotlin Coroutines**: Manages intensive background operations like OCR processing and real-time speech transcription.
*   **CameraX & ML Kit**: Provides the infrastructure for document scanning and on-device machine learning for text simplification.
*   **Speech Services by Google**: Powers the core assistive Speech-to-Text and Text-to-Speech functionalities.

## Implementation Steps

### Task_1_Foundation_SafetyHub: Set up the core application architecture including Material 3 theme (vibrant colors, light/dark modes), navigation, and the Emergency Safety Hub feature.
- **Status:** COMPLETED
- **Updates:** Set up the core application architecture for AccessEd.
- **Acceptance Criteria:**
  - Material 3 theme with vibrant color scheme implemented
  - Navigation between Home, OCR, Transcription, and Safety Hub works
  - Emergency Safety Hub UI allows initiating emergency calls/messages
  - App builds and runs successfully

### Task_2_OCR_TTS_Reader: Implement the Smart OCR & TTS Reader using CameraX and ML Kit for text recognition, paired with Android's Speech services for text-to-speech.
- **Status:** COMPLETED
- **Updates:** Implemented the Smart OCR & TTS Reader for AccessEd.
1. CameraX Integration: Set up a real-time camera preview using CameraX's PreviewView.
2. ML Kit Text Recognition: Integrated Google ML Kit's Text Recognition API to extract text from camera frames in real-time.
3. Text-to-Speech (TTS): Integrated Android's TextToSpeech engine to read recognized text aloud with playback controls.
4. UI Implementation: Created a clean, Material 3-compliant interface for OCR scanning and text display.
5. Permission Handling: Used Accompanist Permissions to handle camera permissions.
6. Build: Verified the app builds and runs successfully.
- **Acceptance Criteria:**
  - CameraX preview displays correctly
  - OCR accurately extracts text from physical documents
  - TTS engine reads extracted text aloud
  - UI handles text display and playback controls smoothly
- **Duration:** N/A

### Task_3_Transcription_Simplification: Implement Live Lecture Transcription (STT) using Google Speech Services and ML-powered Text Simplification logic.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Real-time speech-to-text transcription works with high accuracy
  - Text simplification logic reduces complexity of provided text
  - UI provides a clear interface for live transcription and simplified text display
- **StartTime:** 2026-03-04 22:40:46 IST

### Task_4_Polish_Verification: Apply final UI/UX refinements including an adaptive app icon, Edge-to-Edge display, and perform a full app verification.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Adaptive app icon matches AccessEd branding
  - Edge-to-Edge display implemented across all screens
  - No crashes during final walkthrough
  - Critic agent verifies application stability and requirement alignment

