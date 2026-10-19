# VoyageX — Java Semester Project

VoyageX is a small desktop travel planner built with Core Java and Java Swing. It is designed as a working, beginner-friendly semester project that our team can understand and explain during the viva.

## Planned Features
- Create and view trips
- Enter destination, travel dates, and budget
- Choose Flight, Train, or Bus and calculate an estimated cost
- Add activities and expenses
- View total cost and remaining budget
- Save and retrieve trips using JDBC and SQLite
- Export an itinerary to a `.txt` file

## Technology
- Java 8-compatible Core Java
- Java Swing and AWT
- SQLite with JDBC
- `ArrayList` for collections
- Text-file export

## Scope
This is a local desktop application. It will not include HTML, a web backend, online payments, live travel APIs, authentication, or cloud deployment.

## Development Approach
Build and test the model classes first, then add the Swing interface, database persistence, validation, and file export. Add syllabus features only when they can be demonstrated clearly.

## Team Rule
Every team member should understand the overall application and be able to explain their own module. AI-generated code must be reviewed, run, tested, and understood before integration.

## Project Status
- [x] **Phase 1: Core OOP Models** (`Trip`, `Activity`, `Expense`, `Transport`, `Flight`, `Train`, `Bus`)
  - Run verification test:
    ```bash
    javac model/*.java TestPhase1.java
    java TestPhase1
    ```
- [x] **Phase 2: Basic Java Swing GUI** (`Main`, `ui/MainFrame`)
  - Compile all files:
    ```bash
    javac model/*.java ui/*.java Main.java TestPhase1.java TestPhase2.java
    ```
  - Run the application:
    ```bash
    java Main
    ```
  - Run automated GUI & validation test suite:
    ```bash
    java TestPhase2
    ```
- [x] **Phase 3: Transport, Activities & Budget** (`ui/MainFrame`, `TestPhase3`)
  - Select and manage trips from session list
  - Configure polymorphic transportation (Flight, Train, Bus with AC surcharge)
  - Manage scheduled activities with calendar date picking and expense items
  - Real-time budget breakdown, calculation, and over-budget detection
  - Run Phase 3 automated test suite:
    ```bash
    java TestPhase3
    ```
- [x] **Phase 4: Complete Itinerary & Finalization** (`Trip`, `ui/MainFrame`, `TestPhase4`)
  - Complete consolidated itinerary generation (`generateItinerarySummary`)
  - "Complete Itinerary" tab with monospaced scrollable text view
  - "View Full Itinerary" button on trips table with one-click direct tab navigation
  - Activity date range validation strictly enforcing trip date boundaries
  - Automatic live synchronization across all components on add/remove
  - Run Phase 4 automated test suite:
    ```bash
    java TestPhase4
    ```
- [x] **Phase 5: Export Trip Itinerary to Text File** (`service/ExportService`, `ui/MainFrame`, `TestPhase5`)
  - "Export Itinerary" button integrated in Complete Itinerary tab
  - Plain-text `.txt` export using `JFileChooser`, `PrintWriter`, and `BufferedWriter`
  - Safe filename sanitization removing invalid characters (`\ / : * ? " < > |`)
  - Automatic `.txt` extension appending and cancellation handling
  - Run Phase 5 automated test suite:
    ```bash
    java TestPhase5
    ```

## How to Run

### 1. Compile All Files:
```bash
javac model/*.java service/*.java ui/*.java Main.java TestPhase1.java TestPhase2.java TestPhase3.java TestPhase4.java TestPhase5.java
```

### 2. Run All Automated Test Suites:
```bash
java TestPhase1
java TestPhase2
java TestPhase3
java TestPhase4
java TestPhase5
```

### 3. Run the Desktop Application:
```bash
java Main
```

