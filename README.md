# 🎓 University Course Registration System

A course registration system developed with Java and JavaFX, using SQLite database.

## 📋 Features

### 👨‍💼 Admin

- Student and instructor management (add, edit, delete)
- Course catalog management (111 courses pre-loaded)
- Open courses from catalog
- Approve course requests
- Approve registration requests

### 👨‍🏫 Instructor

- Create course opening requests
- Approve/reject student registration requests
- View enrolled students in their courses
- Notification system

### 👨‍🎓 Student

- View open courses and request registration
- View course schedule
- Credit tracking
- Notification system

## 🛠️ Requirements

- **Java JDK 21** or higher
- **JavaFX SDK 21.0.2** (included in `lib/` folder)
- **SQLite JDBC Driver** (included in `lib/` folder)

## 📦 Installation

### 1. Clone the Project

```bash
git clone https://github.com/mehmetyasinuzun/Course-Registration-System.git
cd Course-Registration-System
```

### 2. JavaFX SDK

JavaFX SDK 21.0.2 is already included in the `lib/` folder. No download needed.

Folder structure:

```
lib/
├── javafx-sdk-21.0.2/
│   └── lib/
│       ├── javafx.controls.jar
│       ├── javafx.fxml.jar
│       └── ...
└── sqlite-jdbc-3.51.1.0.jar
```

### 3. Build (Windows)

Run the build script:

```cmd
build.bat
```

Or compile manually:

```cmd
javac -encoding UTF-8 --module-path lib\javafx-sdk-21.0.2\lib --add-modules javafx.controls,javafx.fxml -cp "lib\sqlite-jdbc-3.51.1.0.jar" -d out src\Main.java src\model\*.java src\database\*.java src\repository\*.java src\service\*.java src\controller\*.java src\view\*.java
```

### 4. Run (Windows)

Run the application:

```cmd
run.bat
```

Or run manually:

```cmd
set PATH_TO_FX=lib\javafx-sdk-21.0.2\lib
java --module-path %PATH_TO_FX% --add-modules javafx.controls,javafx.fxml -cp "out;lib\sqlite-jdbc-3.51.1.0.jar" Main
```

## 🔐 Default Login Credentials

| Role  | Username | Password |
| ----- | -------- | -------- |
| Admin | admin    | admin123 |

> On first run, the database is automatically created and 111 courses are added to the catalog.

## ⏰ Class Schedule System

The system uses a **40-minute class + 10-minute break** schedule:

- **Class times:** Each slot is 40 minutes (e.g., 08:00-08:40, 08:50-09:30)
- **Breaks:** 10 minutes between classes
- **Lunch break:** 12:00-13:00
- **Last class:** 20:30-21:10
- **Format:** When creating a course, select Start and End from the same time slots
  - Example: 1 class hour → Start: "08:00-08:40", End: "08:00-08:40"
  - Example: 2 class hours → Start: "08:00-08:40", End: "08:50-09:30"

## 📁 Project Structure

```
Course-Registration-System/
├── src/
│   ├── Main.java       # 🚀 Main entry point
│   ├── model/          # Data models (User, Course, Registration...)
│   ├── database/       # Database connection and setup
│   ├── repository/     # Database operations (CRUD)
│   ├── service/        # Business logic layer
│   ├── controller/     # Bridge between View-Service
│   └── view/           # JavaFX interfaces
├── lib/                # External libraries
├── data/               # SQLite database (auto-created)
└── out/                # Compiled files
```

## 🏗️ Architecture

```
Main.java (Entry Point)
    │
    ▼
LoginView (JavaFX GUI)
    │
    ├──► AdminView
    ├──► InstructorView
    └──► StudentView
            │
            ▼
      Controller → Service → Repository → Database (SQLite)
```

## 🧑‍💻 Developers

* Mehmet Yasin Uzun: [@mehmetyasinuzun](https://github.com/mehmetyasinuzun)
* Ömer Osman Karataş: [@Karatas0](https://github.com/Karatas0)
* Yaren Göksu: [@yarengogsu](https://github.com/yarengogsu)
* Hayrunnisa Özdamar: [@hayrunnisaozdamar](https://github.com/hayrunnisaozdamar)

## 📄 License

This project was developed for educational purposes.
