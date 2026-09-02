# InterviewMate

> **Interview Review Assistant** — A smart Android app that helps job seekers systematically record, analyze, and improve their interview performance.

## Overview

InterviewMate is an Android application built with **Kotlin** and **Jetpack Compose** following the **MVVM + Repository** architecture. It provides a structured way to log interview experiences, generate visualized reports, identify weak areas, and recommend targeted practice questions — turning every interview into a measurable growth step.

## Features

- **Interview CRUD** — Add, view, edit, delete interview records with search support
- **Question Entry** — Add multiple questions per interview with category tags and self-rating (1-5)
- **Room Persistence** — All data stored locally via Room database (SQLite)
- **Auto Report Generation** — Generates a visualized review report upon saving
- **Smart Recommendations** — Recommends 3 practice questions based on weakest category
- **Question Bank** — Browse questions by category (Algorithms, System Design, Behavioral, Project Experience)
- **Statistics & Visualization** — Overall stats + 4-dimension radar chart

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin 2.2.10 |
| Architecture | MVVM (ViewModel + StateFlow + Repository) |
| UI | Jetpack Compose + Material Design 3 |
| Database | Room + Room KTX (SQLite) |
| Async | Kotlin Coroutines + Flow / StateFlow |
| Navigation | Jetpack Navigation Compose |
| Build | Gradle KTS + KSP |
| Min SDK / Target | API 24 / API 36 |

## Architecture

```
    UI Layer (Jetpack Compose + Material 3)
    MainActivity / Composable Screens (x6) / AppDestination (NavHost)
         |  StateFlow / events
    ViewModel Layer
    InterviewViewModel / QuestionViewModel
         |  suspend fun / Flow
    Repository Layer
    InterviewRepository / QuestionRepository
         |  DAO (suspend / Flow)
    Data Layer (Room)
    InterviewEntity / InterviewItemEntity / QuestionEntity / AppDatabase
         |  seed / generate
    Engine Layer
    ReportEngine / ReportData / QuestionSeedLoader
    ====================================================
    Infra
    InterviewMateApplication / AppContainer / ReportEngineTest
```

## Database Design

Three tables with `interviews` 1:N `interview_items` (ON DELETE CASCADE):

- **interviews** — id, company, position, date, round, result, notes, createdAt
- **interview_items** — id, interviewId (FK), category, question, myAnswer, selfRating
- **questions** — id, category, question, answerHint, difficulty, company

## Getting Started

### Prerequisites
- Android Studio Ladybug (2024.2+) or newer
- JDK 17+
- Android SDK 36

### Build & Run

```bash
git clone https://github.com/ponpoppppp/InterviewMate.git
cd InterviewMate
./gradlew assembleDebug
```

Open in Android Studio, sync Gradle, and run on emulator or device (Android 7.0+).

## Project Structure

```
app/src/main/java/com/example/interviewmate/
  InterviewMateApplication.kt       # Application + AppContainer (DI)
  MainActivity.kt                    # Single Activity entry
  data/
    local/                           # Room DB, DAOs, SeedLoader
    model/                           # Entities + Relation
    repository/                      # InterviewRepository, QuestionRepository
  engine/                            # ReportEngine, ReportData
  ui/
    interview/                       # List, Detail, Edit screens + ViewModel
    question/                        # QuestionBank screen + ViewModel
    navigation/                      # NavHost, AppDestination
    profile/                         # Profile screen + RadarChart
    report/                          # Report screen
    theme/                           # Color, Theme, Typography
  util/                              # InterviewConstants, DateUtils
```


## License

HKU COMP7506 Smart Phone Apps Development — Course Project
