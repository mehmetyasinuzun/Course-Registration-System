package view;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.FontPosture;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import controller.StudentController;
import controller.AuthController;
import model.*;
import util.NavigationManager;
import util.SessionManager;
import java.util.List;
import java.util.ArrayList;

public class StudentView {

    private Stage stage;
    private User currentUser;
    private StudentController studentController = new StudentController();
    private BorderPane mainLayout;

    private TableView<Course> courseTable;
    private TableView<Course> myCoursesTable;
    private TableView<Registration> registrationTable;
    private TableView<Notification> notificationTable;

    public StudentView(Stage stage, User user) {
        this.stage = stage;
        this.currentUser = user;
    }

    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f2f5;");

        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        showDashboard();

        Scene scene = new Scene(mainLayout, 1200, 750);
        stage.setScene(scene);
        stage.setTitle("Student Panel - " + currentUser.getFullName());
        stage.centerOnScreen();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #16a085;");

        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 30, 20));
        header.setStyle("-fx-background-color: #0e6655;");

        Label logoLabel = new Label("👨‍🎓");
        logoLabel.setFont(Font.font(40));

        Label titleLabel = new Label("Student Panel");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Label userLabel = new Label(currentUser.getFullName());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web("#a3e4d7"));

        header.getChildren().addAll(logoLabel, titleLabel, userLabel);

        if (currentUser instanceof Student student) {
            Label studentNumLabel = new Label(student.getStudentNumber());
            studentNumLabel.setFont(Font.font("System", 11));
            studentNumLabel.setTextFill(Color.web("#a3e4d7"));

            // Year and semester info
            Label yearSemLabel = new Label("📅 " + student.getYearSemesterDisplay());
            yearSemLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
            yearSemLabel.setTextFill(Color.WHITE);

            // Credit status
            int currentCredits = studentController.getCurrentCredits(student.getId());
            int maxCredits = studentController.getMaxCredits();
            Label creditLabel = new Label("💳 Credits: " + currentCredits + "/" + maxCredits);
            creditLabel.setFont(Font.font("System", 11));
            if (currentCredits >= maxCredits) {
                creditLabel.setTextFill(Color.web("#e74c3c"));  // Red - limit reached
            } else if (currentCredits >= maxCredits * 0.8) {
                creditLabel.setTextFill(Color.web("#f39c12"));  // Orange - close to limit
            } else {
                creditLabel.setTextFill(Color.web("#a3e4d7"));  // Normal
            }

            header.getChildren().addAll(studentNumLabel, yearSemLabel, creditLabel);

            // Failed courses indicator - show TOTAL failed courses from transcript
            int failedCount = studentController.getTotalFailedCourseCount(student.getId());
            if (failedCount > 0) {
                Label failedLabel = new Label("🔴 Failed Courses: " + failedCount);
                failedLabel.setFont(Font.font("System", FontWeight.BOLD, 11));
                failedLabel.setTextFill(Color.web("#ff6b6b"));
                header.getChildren().add(failedLabel);
            }
        }

        VBox menuBox = new VBox(2);
        menuBox.setPadding(new Insets(20, 10, 20, 10));

        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button allCoursesBtn = createMenuButton("📚 All Courses", false);
        Button myCoursesBtn = createMenuButton("📖 My Courses", false);
        Button transcriptBtn = createMenuButton("📜 My Transcript", false);
        Button registrationsBtn = createMenuButton("📋 My Registrations", false);
        Button notificationsBtn = createMenuButton("🔔 Notifications", false);
        Button logoutBtn = createMenuButton("🚪 Log Out", false);

        // Show failed course count on All Courses button - use TOTAL failed course count
        int failedCourseCount = studentController.getTotalFailedCourseCount(currentUser.getId());
        if (failedCourseCount > 0) {
            allCoursesBtn.setText("📚 All Courses ⚠️");
            allCoursesBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff6b6b; -fx-font-size: 14px; -fx-font-weight: bold; -fx-padding: 12 20; -fx-cursor: hand;");
        }

        int unreadCount = studentController.getUnreadCount(currentUser.getId());
        if (unreadCount > 0) {
            notificationsBtn.setText("🔔 Notifications (" + unreadCount + ")");
        }

        dashboardBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(dashboardBtn); 
            NavigationManager.navigateTo("Dashboard");  // Stack'e ekle
            showDashboard(); 
        });
        allCoursesBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(allCoursesBtn); 
            NavigationManager.navigateTo("AllCourses");  // Stack'e ekle
            showAllCourses(); 
        });
        myCoursesBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(myCoursesBtn); 
            NavigationManager.navigateTo("MyCourses");  // Stack'e ekle
            showMyCourses(); 
        });
        transcriptBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(transcriptBtn); 
            NavigationManager.navigateTo("Transcript");  // Stack'e ekle
            showTranscript(); 
        });
        registrationsBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(registrationsBtn); 
            NavigationManager.navigateTo("Registrations");  // Stack'e ekle
            showRegistrations(); 
        });
        notificationsBtn.setOnAction(e -> { 
            resetMenuButtons(menuBox); 
            setActiveButton(notificationsBtn); 
            NavigationManager.navigateTo("Notifications");  // Stack'e ekle
            showNotifications(); 
        });
        logoutBtn.setOnAction(e -> logout());

        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e74c3c;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;"
        );

        menuBox.getChildren().addAll(dashboardBtn, allCoursesBtn, myCoursesBtn, transcriptBtn, registrationsBtn, notificationsBtn);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox logoutBox = new VBox();
        logoutBox.setPadding(new Insets(10));
        logoutBox.getChildren().add(logoutBtn);

        sidebar.getChildren().addAll(header, menuBox, spacer, logoutBox);
        return sidebar;
    }

    private Button createMenuButton(String text, boolean active) {
        Button btn = new Button(text);
        btn.setPrefWidth(230);
        btn.setAlignment(Pos.CENTER_LEFT);

        if (active) {
            setActiveButton(btn);
        } else {
            btn.setStyle(
                    "-fx-background-color: transparent;" +
                            "-fx-text-fill: #a3e4d7;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 20;" +
                            "-fx-cursor: hand;"
            );
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#1abc9c")) {
                btn.setStyle(
                        "-fx-background-color: #1abc9c;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 5;"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#27ae60")) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #a3e4d7;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 20;" +
                                "-fx-cursor: hand;"
                );
            }
        });

        return btn;
    }

    private void setActiveButton(Button btn) {
        btn.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
    }

    private void resetMenuButtons(VBox menuBox) {
        for (var node : menuBox.getChildren()) {
            if (node instanceof Button) {
                Button btn = (Button) node;
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #a3e4d7;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 20;" +
                                "-fx-cursor: hand;"
                );
            }
        }
    }

    // ========== DASHBOARD ==========
    private void showDashboard() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("Dashboard");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Student info card
        if (currentUser instanceof Student student) {
            HBox studentInfoBox = new HBox(20);
            studentInfoBox.setStyle("-fx-background-color: linear-gradient(to right, #27ae60, #16a085); -fx-background-radius: 10;");
            studentInfoBox.setPadding(new Insets(20));
            studentInfoBox.setAlignment(Pos.CENTER_LEFT);

            VBox infoLeft = new VBox(5);
            Label welcomeLabel = new Label("Welcome, " + student.getFullName() + "!");
            welcomeLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
            welcomeLabel.setTextFill(Color.WHITE);

            Label studentNumLabel = new Label("📌 Student No: " + student.getStudentNumber());
            studentNumLabel.setTextFill(Color.web("#a3e4d7"));

            Label yearSemLabel = new Label("📅 " + student.getYearSemesterDisplay());
            yearSemLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
            yearSemLabel.setTextFill(Color.WHITE);

            infoLeft.getChildren().addAll(welcomeLabel, studentNumLabel, yearSemLabel);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            // Credit status
            VBox creditBox = new VBox(5);
            creditBox.setAlignment(Pos.CENTER);
            int currentCredits = studentController.getCurrentCredits(student.getId());
            int maxCredits = studentController.getMaxCredits();

            Label creditLabel = new Label(currentCredits + "/" + maxCredits);
            creditLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
            creditLabel.setTextFill(Color.WHITE);

            Label creditTextLabel = new Label("Credit Usage");
            creditTextLabel.setTextFill(Color.web("#a3e4d7"));

            // Credit bar
            double percentage = maxCredits > 0 ? (double) currentCredits / maxCredits : 0;
            if (percentage > 1.0) percentage = 1.0;

            StackPane creditBarContainer = new StackPane();
            creditBarContainer.setPrefWidth(150);
            creditBarContainer.setPrefHeight(20);
            creditBarContainer.setStyle("-fx-background-color: rgba(255,255,255,0.3); -fx-background-radius: 10;");

            Region creditBarFill = new Region();
            creditBarFill.setPrefHeight(20);
            creditBarFill.setMaxWidth(150 * percentage);
            creditBarFill.setPrefWidth(150 * percentage);

            String barColor;
            if (currentCredits >= maxCredits) {
                barColor = "#e74c3c";
            } else if (currentCredits >= maxCredits * 0.8) {
                barColor = "#f39c12";
            } else {
                barColor = "#2ecc71";
            }
            creditBarFill.setStyle("-fx-background-color: " + barColor + "; -fx-background-radius: 10;");
            StackPane.setAlignment(creditBarFill, Pos.CENTER_LEFT);

            creditBarContainer.getChildren().add(creditBarFill);

            creditBox.getChildren().addAll(creditLabel, creditBarContainer, creditTextLabel);

            studentInfoBox.getChildren().addAll(infoLeft, spacer, creditBox);
            content.getChildren().add(studentInfoBox);
        }

        // Get ALL failed courses from transcript (DC, DD, FD, FF - below CC/2.00)
        List<String> allFailedCourseCodes = studentController.getAllFailedCourseCodes(currentUser.getId());
        // Get failed courses that have open sections (for registration)
        List<String> failedCoursesWithOpenSections = studentController.getFailedCourseCodesWithOpenCourses(currentUser.getId());

        // Failed courses alert banner on dashboard - show if ANY failed courses exist
        if (!allFailedCourseCodes.isEmpty()) {
            HBox alertBanner = new HBox(15);
            alertBanner.setAlignment(Pos.CENTER_LEFT);
            alertBanner.setStyle("-fx-background-color: linear-gradient(to right, #dc3545, #c82333); -fx-padding: 15; -fx-background-radius: 10;");

            Label alertIcon = new Label("⚠️");
            alertIcon.setFont(Font.font(24));

            VBox alertTextBox = new VBox(3);
            Label alertTitle = new Label("ATTENTION: You have " + allFailedCourseCodes.size() + " failed course(s)! (Grade < CC)");
            alertTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            alertTitle.setTextFill(Color.WHITE);

            String subtitleText = "Failed courses: " + String.join(", ", allFailedCourseCodes);
            if (!failedCoursesWithOpenSections.isEmpty()) {
                subtitleText += "\n📌 Open for registration: " + String.join(", ", failedCoursesWithOpenSections);
            } else {
                subtitleText += "\n(No open sections available for these courses yet)";
            }
            Label alertSubtitle = new Label(subtitleText);
            alertSubtitle.setTextFill(Color.web("#ffcccb"));
            alertSubtitle.setWrapText(true);

            alertTextBox.getChildren().addAll(alertTitle, alertSubtitle);

            Region alertSpacer = new Region();
            HBox.setHgrow(alertSpacer, Priority.ALWAYS);

            if (!failedCoursesWithOpenSections.isEmpty()) {
                Button goToFailedBtn = new Button("Register Now →");
                goToFailedBtn.setStyle("-fx-background-color: white; -fx-text-fill: #dc3545; -fx-font-weight: bold; -fx-cursor: hand;");
                goToFailedBtn.setOnAction(e -> showFailedCoursesOnly());
                alertBanner.getChildren().addAll(alertIcon, alertTextBox, alertSpacer, goToFailedBtn);
            } else {
                alertBanner.getChildren().addAll(alertIcon, alertTextBox, alertSpacer);
            }

            content.getChildren().add(alertBanner);
        }

        List<Course> myCourses = studentController.getMyCourses(currentUser.getId());
        List<Registration> myRegs = studentController.getMyRegistrations(currentUser.getId());
        long pendingCount = myRegs.stream().filter(r -> r.getStatus().equals("PENDING")).count();

        int totalCredits = myCourses.stream().mapToInt(Course::getCredits).sum();

        // Add failed course count to stats - show TOTAL failed courses
        int totalFailedCourseCount = allFailedCourseCodes.size();

        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
                createStatCard("📖", "Enrolled Courses", String.valueOf(myCourses.size()), "#27ae60"),
                createStatCard("💳", "Enrolled Credits", String.valueOf(totalCredits), "#3498db"),
                createStatCard("📋", "Pending Registrations", String.valueOf(pendingCount), "#e67e22"),
                createStatCard("🔴", "Failed Courses", String.valueOf(totalFailedCourseCount), "#dc3545")
        );

        // My enrolled courses
        VBox myCoursesBox = new VBox(15);
        myCoursesBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        myCoursesBox.setPadding(new Insets(20));

        Label myCoursesTitle = new Label("My Enrolled Courses (" + myCourses.size() + " courses)");
        myCoursesTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        myCoursesTitle.setTextFill(Color.web("#2c3e50"));

        if (myCourses.isEmpty()) {
            Label noData = new Label("You don't have any enrolled courses yet. You can register from the 'All Courses' section.");
            noData.setTextFill(Color.web("#555555"));
            noData.setWrapText(true);
            myCoursesBox.getChildren().addAll(myCoursesTitle, noData);
        } else {
            VBox listBox = new VBox(10);
            listBox.setStyle("-fx-background-color: white;");

            for (Course course : myCourses) {
                HBox item = new HBox(15);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #c8e6c9; -fx-border-radius: 5;");

                Label codeLabel = new Label(course.getCode());
                codeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                codeLabel.setStyle("-fx-background-color: #2e7d32; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");

                VBox infoBox = new VBox(3);
                Label nameLabel = new Label(course.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web("#1a1a1a"));

                Label scheduleLabel = new Label(course.getScheduleDisplay() + " | " + course.getCredits() + " Credits | " + course.getSemesterDisplay());
                scheduleLabel.setFont(Font.font("System", 12));
                scheduleLabel.setTextFill(Color.web("#444444"));
                infoBox.getChildren().addAll(nameLabel, scheduleLabel);

                item.getChildren().addAll(codeLabel, infoBox);
                listBox.getChildren().add(item);
            }

            ScrollPane listScrollPane = new ScrollPane(listBox);
            listScrollPane.setFitToWidth(true);
            listScrollPane.setMaxHeight(250);
            listScrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
            listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            myCoursesBox.getChildren().addAll(myCoursesTitle, listScrollPane);
        }

        content.getChildren().addAll(title, statsBox, myCoursesBox);
        mainLayout.setCenter(content);
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(180);
        card.setPrefHeight(120);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        card.setPadding(new Insets(20));

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font(30));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("System", FontWeight.BOLD, 28));
        valueLabel.setTextFill(Color.web(color));

        Label textLabel = new Label(label);
        textLabel.setFont(Font.font("System", 12));
        textLabel.setTextFill(Color.web("#7f8c8d"));

        card.getChildren().addAll(iconLabel, valueLabel, textLabel);
        return card;
    }

    // ========== ALL COURSES ==========
    private void showAllCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("All Courses");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Check for failed courses first
        List<String> failedCourseCodes = studentController.getFailedCourseCodesWithOpenCourses(currentUser.getId());

        // Failed courses warning banner
        if (!failedCourseCodes.isEmpty()) {
            VBox warningBanner = new VBox(10);
            warningBanner.setStyle("-fx-background-color: #fff3cd; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #ffc107; -fx-border-width: 2; -fx-border-radius: 8;");

            HBox warningHeader = new HBox(10);
            warningHeader.setAlignment(Pos.CENTER_LEFT);
            Label warningIcon = new Label("⚠️");
            warningIcon.setFont(Font.font(20));
            Label warningTitle = new Label("FAILED COURSE PRIORITY!");
            warningTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
            warningTitle.setTextFill(Color.web("#856404"));
            warningHeader.getChildren().addAll(warningIcon, warningTitle);

            Label warningText = new Label("You must first register for your failed courses before registering for other courses.\nFailed courses with open sections: " + String.join(", ", failedCourseCodes));
            warningText.setWrapText(true);
            warningText.setTextFill(Color.web("#856404"));

            Button showFailedBtn = new Button("🔴 Show Only Failed Courses");
            showFailedBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");

            warningBanner.getChildren().addAll(warningHeader, warningText, showFailedBtn);
            content.getChildren().add(warningBanner);

            // Add action to show only failed courses
            showFailedBtn.setOnAction(e -> showFailedCoursesOnly());
        }

        // Credit status and semester info
        HBox infoBox = new HBox(30);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        int currentCredits = studentController.getCurrentCredits(currentUser.getId());
        int maxCredits = studentController.getMaxCredits();
        int remainingCredits = maxCredits - currentCredits;

        Label creditInfo = new Label("💳 Used Credits: " + currentCredits + "/" + maxCredits + " (Remaining: " + remainingCredits + ")");
        creditInfo.setFont(Font.font("System", FontWeight.BOLD, 14));
        if (remainingCredits <= 0) {
            creditInfo.setTextFill(Color.web("#e74c3c"));
        } else if (remainingCredits < 10) {
            creditInfo.setTextFill(Color.web("#f39c12"));
        } else {
            creditInfo.setTextFill(Color.web("#27ae60"));
        }

        // Semester filter
        HBox filterBox = new HBox(10);
        filterBox.setAlignment(Pos.CENTER_LEFT);
        Label filterLabel = new Label("Semester Filter:");
        ComboBox<String> semesterFilter = new ComboBox<>();
        semesterFilter.getItems().add("All Courses");
        semesterFilter.getItems().add("🔴 Failed Courses (Retake)"); // Add failed courses filter option
        for (int i = 1; i <= 8; i++) {
            int year = (i + 1) / 2;
            String period = (i % 2 == 1) ? "Fall" : "Spring";
            semesterFilter.getItems().add(year + ". Year - " + period + " (" + i + ". Semester)");
        }

        // Set default selection - if there are failed courses, select that filter first
        if (!failedCourseCodes.isEmpty()) {
            semesterFilter.getSelectionModel().select(1); // Select "Failed Courses" filter
        } else if (currentUser instanceof Student student) {
            int totalSem = student.getTotalSemester();
            if (totalSem >= 1 && totalSem <= 8) {
                semesterFilter.getSelectionModel().select(totalSem);
            } else {
                semesterFilter.getSelectionModel().select(0);
            }
        } else {
            semesterFilter.getSelectionModel().select(0);
        }
        filterBox.getChildren().addAll(filterLabel, semesterFilter);

        infoBox.getChildren().addAll(creditInfo, filterBox);

        Label subtitle = new Label("Select the course you want to register for and click the 'Register' button.");
        subtitle.setTextFill(Color.web("#7f8c8d"));

        courseTable = new TableView<>();
        courseTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(60);

        TableColumn<Course, String> semesterCol = new TableColumn<>("Semester");
        semesterCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getSemesterDisplay()));
        semesterCol.setPrefWidth(100);

        TableColumn<Course, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleDisplay()));
        scheduleCol.setPrefWidth(140);

        TableColumn<Course, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> {
            Course course = cellData.getValue();
            int enrolled = studentController.getEnrollmentCount(course.getId());
            return new javafx.beans.property.SimpleStringProperty(enrolled + "/" + course.getCapacity());
        });
        capacityCol.setPrefWidth(80);

        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(cellData -> {
            User instructor = studentController.getInstructorById(cellData.getValue().getInstructorId());
            return new javafx.beans.property.SimpleStringProperty(instructor != null ? instructor.getFullName() : "-");
        });
        instructorCol.setPrefWidth(130);

        // Get failed course codes for highlighting
        List<String> myFailedCodes = studentController.getFailedCourseCodesWithOpenCourses(currentUser.getId());

        TableColumn<Course, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(140);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button registerBtn = new Button("📝 Register");
            {
                registerBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    registerForCourse(course);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Course course = getTableView().getItems().get(getIndex());
                    boolean isFailedCourse = myFailedCodes.contains(course.getCode());

                    if (isFailedCourse) {
                        registerBtn.setText("🔄 Retake");
                        registerBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                    } else {
                        registerBtn.setText("📝 Register");
                        registerBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                    }
                    setGraphic(registerBtn);
                }
            }
        });

        // Row factory to highlight failed courses
        courseTable.setRowFactory(tv -> new TableRow<Course>() {
            @Override
            protected void updateItem(Course course, boolean empty) {
                super.updateItem(course, empty);
                if (course == null || empty) {
                    setStyle("");
                } else if (myFailedCodes.contains(course.getCode())) {
                    setStyle("-fx-background-color: #ffebee;"); // Light red background for failed courses
                } else {
                    setStyle("");
                }
            }
        });

        courseTable.getColumns().addAll(codeCol, nameCol, creditsCol, semesterCol, scheduleCol, capacityCol, instructorCol, actionCol);

        // Initial load - based on selected semester
        loadCoursesBySemester(semesterFilter.getSelectionModel().getSelectedIndex());

        // Update table when semester changes
        semesterFilter.setOnAction(e -> {
            loadCoursesBySemester(semesterFilter.getSelectionModel().getSelectedIndex());
        });

        // Show details on double click
        courseTable.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Course selected = courseTable.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    showCourseDetails(selected);
                }
            }
        });

        VBox.setVgrow(courseTable, Priority.ALWAYS);
        content.getChildren().addAll(title, infoBox, subtitle, courseTable);
        mainLayout.setCenter(content);
    }

    private void loadCoursesBySemester(int filterIndex) {
        List<Course> courses;
        if (filterIndex == 0) {
            // All courses
            courses = studentController.getAllCourses();
        } else if (filterIndex == 1) {
            // Failed courses only - get courses that match failed course codes
            List<String> failedCodes = studentController.getFailedCourseCodesWithOpenCourses(currentUser.getId());
            courses = studentController.getAllCourses().stream()
                .filter(c -> failedCodes.contains(c.getCode()))
                .toList();
        } else {
            // Specific semester (adjusted index because we added "Failed Courses" option)
            courses = studentController.getCoursesBySemester(filterIndex - 1);
        }
        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    // Show only failed courses page
    private void showFailedCoursesOnly() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("🔴 Failed Courses - Must Register First!");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#dc3545"));

        List<String> failedCodes = studentController.getFailedCourseCodesWithOpenCourses(currentUser.getId());

        // Info banner
        VBox infoBanner = new VBox(10);
        infoBanner.setStyle("-fx-background-color: #f8d7da; -fx-padding: 15; -fx-background-radius: 8; -fx-border-color: #f5c6cb; -fx-border-width: 1; -fx-border-radius: 8;");

        Label infoTitle = new Label("⚠️ You have " + failedCodes.size() + " failed course(s) that need to be retaken:");
        infoTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
        infoTitle.setTextFill(Color.web("#721c24"));

        Label infoText = new Label("You must register for ALL failed courses below before you can register for any other courses.");
        infoText.setWrapText(true);
        infoText.setTextFill(Color.web("#721c24"));

        infoBanner.getChildren().addAll(infoTitle, infoText);

        // Failed courses table
        TableView<Course> failedTable = new TableView<>();
        failedTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(60);

        TableColumn<Course, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleDisplay()));
        scheduleCol.setPrefWidth(150);

        TableColumn<Course, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> {
            Course course = cellData.getValue();
            int enrolled = studentController.getEnrollmentCount(course.getId());
            return new javafx.beans.property.SimpleStringProperty(enrolled + "/" + course.getCapacity());
        });
        capacityCol.setPrefWidth(80);

        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(cellData -> {
            User instructor = studentController.getInstructorById(cellData.getValue().getInstructorId());
            return new javafx.beans.property.SimpleStringProperty(instructor != null ? instructor.getFullName() : "-");
        });
        instructorCol.setPrefWidth(150);

        TableColumn<Course, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(150);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button registerBtn = new Button("🔄 Retake Course");
            {
                registerBtn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
                registerBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    registerForCourse(course);
                    // Refresh the page after registration
                    showFailedCoursesOnly();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : registerBtn);
            }
        });

        failedTable.getColumns().addAll(codeCol, nameCol, creditsCol, scheduleCol, capacityCol, instructorCol, actionCol);

        // Load failed courses
        List<Course> failedCourses = studentController.getAllCourses().stream()
            .filter(c -> failedCodes.contains(c.getCode()))
            .toList();
        failedTable.setItems(FXCollections.observableArrayList(failedCourses));

        // Back button
        Button backBtn = new Button("← Back to All Courses");
        backBtn.setStyle("-fx-background-color: #6c757d; -fx-text-fill: white; -fx-cursor: hand;");
        backBtn.setOnAction(e -> showAllCourses());

        VBox.setVgrow(failedTable, Priority.ALWAYS);
        content.getChildren().addAll(title, infoBanner, failedTable, backBtn);
        mainLayout.setCenter(content);
    }

    private void registerForCourse(Course course) {
        int studentTotalSemester = 0;
        if (currentUser instanceof Student student) {
            studentTotalSemester = student.getTotalSemester();
        }

        String result = studentController.registerForCourse(currentUser.getId(), course.getId(), studentTotalSemester);

        if (result.startsWith("SUCCESS")) {
            showAlert(Alert.AlertType.INFORMATION, "Registration Request", result);
            refreshSidebar();
        } else {
            showAlert(Alert.AlertType.WARNING, "Registration Error", result);
        }
    }

    private void refreshSidebar() {
        VBox newSidebar = createSidebar();
        mainLayout.setLeft(newSidebar);
    }

    private void showCourseDetails(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Details");
        dialog.setHeaderText(course.getCode() + " - " + course.getName());

        VBox content = new VBox(15);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        GridPane grid = new GridPane();
        grid.setHgap(15);
        grid.setVgap(10);

        grid.add(new Label("Credits:"), 0, 0);
        grid.add(new Label(String.valueOf(course.getCredits())), 1, 0);

        grid.add(new Label("Schedule:"), 0, 1);
        grid.add(new Label(course.getScheduleDisplay()), 1, 1);

        int enrolled = studentController.getEnrollmentCount(course.getId());
        grid.add(new Label("Capacity:"), 0, 2);
        Label capacityLabel = new Label(enrolled + "/" + course.getCapacity());
        if (enrolled >= course.getCapacity()) {
            capacityLabel.setTextFill(Color.web("#e74c3c"));
            capacityLabel.setText(capacityLabel.getText() + " (FULL)");
        }
        grid.add(capacityLabel, 1, 2);

        User instructor = studentController.getInstructorById(course.getInstructorId());
        grid.add(new Label("Instructor:"), 0, 3);
        grid.add(new Label(instructor != null ? instructor.getFullName() : "-"), 1, 3);

        content.getChildren().add(grid);

        // Prerequisites
        List<Course> prereqs = studentController.getPrerequisites(course.getId());
        if (!prereqs.isEmpty()) {
            Label prereqTitle = new Label("Prerequisite Courses:");
            prereqTitle.setFont(Font.font("System", FontWeight.BOLD, 13));
            content.getChildren().add(prereqTitle);

            for (Course prereq : prereqs) {
                HBox prereqItem = new HBox(10);
                prereqItem.setStyle("-fx-background-color: #fff3cd; -fx-padding: 8; -fx-background-radius: 3;");

                boolean completed = studentController.hasCompletedCourse(currentUser.getId(), prereq.getId());
                Label prereqLabel = new Label((completed ? "✓ " : "✗ ") + prereq.getCode() + " - " + prereq.getName());
                prereqLabel.setTextFill(completed ? Color.web("#27ae60") : Color.web("#e74c3c"));

                prereqItem.getChildren().add(prereqLabel);
                content.getChildren().add(prereqItem);
            }
        }

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ========== MY COURSES ==========
    private void showMyCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("My Enrolled Courses");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        myCoursesTable = new TableView<>();
        myCoursesTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Course, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<Course, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(250);

        TableColumn<Course, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(60);

        TableColumn<Course, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleDisplay()));
        scheduleCol.setPrefWidth(180);

        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(cellData -> {
            User instructor = studentController.getInstructorById(cellData.getValue().getInstructorId());
            return new javafx.beans.property.SimpleStringProperty(instructor != null ? instructor.getFullName() : "-");
        });
        instructorCol.setPrefWidth(200);

        myCoursesTable.getColumns().addAll(codeCol, nameCol, creditsCol, scheduleCol, instructorCol);

        List<Course> myCourses = studentController.getMyCourses(currentUser.getId());
        myCoursesTable.setItems(FXCollections.observableArrayList(myCourses));

        int totalCredits = myCourses.stream().mapToInt(Course::getCredits).sum();
        Label totalLabel = new Label("Total: " + myCourses.size() + " courses, " + totalCredits + " credits");
        totalLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        totalLabel.setTextFill(Color.web("#27ae60"));

        VBox.setVgrow(myCoursesTable, Priority.ALWAYS);
        content.getChildren().addAll(title, myCoursesTable, totalLabel);
        mainLayout.setCenter(content);
    }

    // ========== MY REGISTRATIONS ==========
    private void showRegistrations() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("My Registration Requests");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        registrationTable = new TableView<>();
        registrationTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Registration, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> {
            Course course = studentController.getCourseById(cellData.getValue().getCourseId());
            return new javafx.beans.property.SimpleStringProperty(course != null ? course.getCode() + " - " + course.getName() : "-");
        });
        courseCol.setPrefWidth(300);

        TableColumn<Registration, String> dateCol = new TableColumn<>("Request Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateCol.setPrefWidth(150);

        TableColumn<Registration, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplay()));
        statusCol.setPrefWidth(120);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    switch (item) {
                        case "Pending":
                            setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-alignment: center; -fx-background-radius: 3;");
                            break;
                        case "Approved":
                            setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-alignment: center; -fx-background-radius: 3;");
                            break;
                        case "Rejected":
                            setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-alignment: center; -fx-background-radius: 3;");
                            break;
                        default:
                            setStyle("-fx-alignment: center;");
                    }
                }
            }
        });

        TableColumn<Registration, String> responseDateCol = new TableColumn<>("Response Date");
        responseDateCol.setCellValueFactory(cellData -> {
            String date = cellData.getValue().getResponseDate();
            return new javafx.beans.property.SimpleStringProperty(date != null ? date : "-");
        });
        responseDateCol.setPrefWidth(150);

        TableColumn<Registration, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button cancelBtn = new Button("Cancel");
            {
                cancelBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                cancelBtn.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    if (reg.getStatus().equals("PENDING")) {
                        if (showConfirmDialog("Cancel", "The registration request will be cancelled. Are you sure?")) {
                            studentController.cancelRegistration(reg.getId());
                            refreshRegistrationTable();
                        }
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Registration reg = getTableView().getItems().get(getIndex());
                    setGraphic(reg.getStatus().equals("PENDING") ? cancelBtn : null);
                }
            }
        });

        registrationTable.getColumns().addAll(courseCol, dateCol, statusCol, responseDateCol, actionCol);
        refreshRegistrationTable();

        VBox.setVgrow(registrationTable, Priority.ALWAYS);
        content.getChildren().addAll(title, registrationTable);
        mainLayout.setCenter(content);
    }

    private void refreshRegistrationTable() {
        List<Registration> registrations = studentController.getMyRegistrations(currentUser.getId());
        registrationTable.setItems(FXCollections.observableArrayList(registrations));
    }

    // ========== NOTIFICATIONS ==========
    private void showNotifications() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Notifications");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button markAllBtn = new Button("Mark All as Read");
        markAllBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
        markAllBtn.setOnAction(e -> {
            studentController.markAllNotificationsAsRead(currentUser.getId());
            refreshNotificationTable();
        });

        header.getChildren().addAll(title, spacer, markAllBtn);

        notificationTable = new TableView<>();
        notificationTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Notification, String> titleCol = new TableColumn<>("Title");
        titleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        titleCol.setPrefWidth(200);

        TableColumn<Notification, String> messageCol = new TableColumn<>("Message");
        messageCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        messageCol.setPrefWidth(400);

        TableColumn<Notification, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("createdAt"));
        dateCol.setPrefWidth(150);

        TableColumn<Notification, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData -> {
            return new javafx.beans.property.SimpleStringProperty(cellData.getValue().isRead() ? "Read" : "New");
        });
        statusCol.setPrefWidth(80);
        statusCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("New")) {
                        setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-alignment: center;");
                    }
                }
            }
        });

        notificationTable.getColumns().addAll(titleCol, messageCol, dateCol, statusCol);
        refreshNotificationTable();

        notificationTable.setOnMouseClicked(e -> {
            Notification selected = notificationTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                studentController.markNotificationAsRead(selected.getId());
                refreshNotificationTable();
            }
        });

        VBox.setVgrow(notificationTable, Priority.ALWAYS);
        content.getChildren().addAll(header, notificationTable);
        mainLayout.setCenter(content);
    }

    private void refreshNotificationTable() {
        List<Notification> notifications = studentController.getNotifications(currentUser.getId());
        notificationTable.setItems(FXCollections.observableArrayList(notifications));
    }

    // ========== HELPERS ==========
    private void logout() {
        new AuthController().logout();
        new LoginView().start(stage);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // ========== TRANSCRIPT PAGE ==========
    private void showTranscript() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("📜 My Transcript");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Get transcript
        List<TranscriptEntry> transcript = studentController.getMyTranscript(currentUser.getId());

        VBox transcriptContent = new VBox(15);
        transcriptContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        if (transcript.isEmpty()) {
            Label emptyLabel = new Label("You haven't completed any courses yet.");
            emptyLabel.setFont(Font.font("System", 14));
            emptyLabel.setTextFill(Color.web("#e74c3c"));
            transcriptContent.getChildren().add(emptyLabel);
        } else {
            Student student = (Student) currentUser;

            // ========== OFFICIAL TRANSCRIPT HEADER ==========
            VBox officialHeader = new VBox(8);
            officialHeader.setAlignment(Pos.CENTER);
            officialHeader.setStyle("-fx-background-color: linear-gradient(to bottom, #37474f, #455a64); -fx-padding: 25; -fx-background-radius: 8;");

            Label univName = new Label("SIVAS UNIVERSITY OF SCIENCE AND TECHNOLOGY");
            univName.setFont(Font.font("Arial", FontWeight.BOLD, 20));
            univName.setTextFill(Color.WHITE);

            Label transcriptTitle = new Label("OFFICIAL ACADEMIC TRANSCRIPT");
            transcriptTitle.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            transcriptTitle.setTextFill(Color.web("#b0bec5"));

            Label dateLabel = new Label("Issue Date: " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("MMMM dd, yyyy")));
            dateLabel.setFont(Font.font("Arial", 11));
            dateLabel.setTextFill(Color.web("#cfd8dc"));

            officialHeader.getChildren().addAll(univName, transcriptTitle, dateLabel);

            // ========== STUDENT INFORMATION ==========
            VBox studentInfo = new VBox(12);
            studentInfo.setStyle("-fx-background-color: #eceff1; -fx-padding: 20; -fx-border-color: #cfd8dc; -fx-border-width: 1;");

            // Student name and status badge
            HBox nameStatusBox = new HBox(15);
            nameStatusBox.setAlignment(Pos.CENTER_LEFT);

            Label studentNameLabel = new Label(student.getFullName());
            studentNameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));
            studentNameLabel.setTextFill(Color.web("#263238"));

            // Status badge
            Label statusBadge = new Label("GOOD STANDING");
            statusBadge.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            statusBadge.setTextFill(Color.WHITE);
            statusBadge.setStyle("-fx-background-color: #43a047; -fx-padding: 5 15; -fx-background-radius: 15;");

            nameStatusBox.getChildren().addAll(studentNameLabel, statusBadge);

            Label infoTitle = new Label("STUDENT INFORMATION");
            infoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 11));
            infoTitle.setTextFill(Color.web("#546e7a"));

            GridPane infoGrid = new GridPane();
            infoGrid.setHgap(40);
            infoGrid.setVgap(8);

            addInfoRow(infoGrid, 0, "Student ID:", student.getStudentNumber());
            addInfoRow(infoGrid, 1, "Academic Year:", "Year " + ((student.getTotalSemester() + 1) / 2));
            addInfoRow(infoGrid, 2, "Current Semester:", student.getYearSemesterDisplay());
            addInfoRow(infoGrid, 3, "Department:", student.getDepartment());
            addInfoRow(infoGrid, 4, "Faculty:", student.getFaculty());

            studentInfo.getChildren().addAll(nameStatusBox, infoTitle, infoGrid);

            // ========== CUMULATIVE SUMMARY ==========
            int completedCredits = studentController.getCompletedCredits(currentUser.getId());
            double cumulativeGPA = studentController.calculateMyGPA(currentUser.getId());
            int completedCount = studentController.getCompletedCourseCount(currentUser.getId());

            HBox summaryBox = new HBox(15);
            summaryBox.setAlignment(Pos.CENTER);
            summaryBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

            VBox cgpaBox = createStatBox("CGPA", String.format("%.2f / 4.00", cumulativeGPA), "#1e88e5");
            VBox creditsBox = createStatBox("Total Credits", String.valueOf(completedCredits), "#7b1fa2");
            VBox coursesBox = createStatBox("Courses Taken", String.valueOf(completedCount), "#00897b");

            // Calculate passed and failed counts dynamically
            int failedCourseCount = studentController.getTotalFailedCourseCount(currentUser.getId());
            int passedCourseCount = completedCount - failedCourseCount;

            VBox passedBox = createStatBox("Passed", String.valueOf(passedCourseCount), "#43a047");
            VBox failedBox = createStatBox("Failed", String.valueOf(failedCourseCount), "#e53935");

            summaryBox.getChildren().addAll(cgpaBox, creditsBox, coursesBox, passedBox, failedBox);

            transcriptContent.getChildren().addAll(officialHeader, studentInfo, summaryBox);

            // ========== ACADEMIC RECORD BY SEMESTER ==========
            Label academicTitle = new Label("ACADEMIC RECORD");
            academicTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
            academicTitle.setTextFill(Color.web("#1976d2"));
            academicTitle.setUnderline(true);
            academicTitle.setStyle("-fx-padding: 15 0 10 0;");
            transcriptContent.getChildren().add(academicTitle);

            // Group courses by semester (normalize electives to 0)
            java.util.Map<Integer, List<TranscriptEntry>> groupedBySemester = new java.util.TreeMap<>((a, b) -> {
                if (a == 0) return 1;
                if (b == 0) return -1;
                return Integer.compare(a, b);
            });
            for (TranscriptEntry entry : transcript) {
                // Normalize: all elective courses (semester <= 0) go to semester 0
                int normalizedSemester = entry.getSemester() <= 0 ? 0 : entry.getSemester();
                groupedBySemester.computeIfAbsent(normalizedSemester, k -> new ArrayList<>()).add(entry);
            }

            // Display each semester
            for (java.util.Map.Entry<Integer, List<TranscriptEntry>> semEntry : groupedBySemester.entrySet()) {
                int sem = semEntry.getKey();

                VBox semesterBox = new VBox(5);
                semesterBox.setStyle("-fx-border-color: #bdbdbd; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white;");

                // Semester header
                HBox semesterHeader = new HBox(15);
                semesterHeader.setAlignment(Pos.CENTER_LEFT);
                semesterHeader.setStyle("-fx-background-color: #263238; -fx-padding: 10;");

                String yearText, semesterText;
                if (sem == 0) {
                    yearText = "ELECTIVE COURSES";
                    semesterText = "";
                } else {
                    int year = (sem + 1) / 2;
                    int semester = (sem % 2 == 1) ? 1 : 2;
                    yearText = "YEAR " + year;
                    semesterText = " - " + (semester == 1 ? "FALL SEMESTER" : "SPRING SEMESTER");
                }

                Label semLabel = new Label(yearText + semesterText);
                semLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
                semLabel.setTextFill(Color.WHITE);

                semesterHeader.getChildren().add(semLabel);

                // Courses table
                TableView<TranscriptEntry> table = new TableView<>();
                table.setStyle("-fx-background-color: white; -fx-table-cell-border-color: #e0e0e0;");
                table.setFixedCellSize(35);
                table.setPrefHeight(semEntry.getValue().size() * 35 + 35);

                TableColumn<TranscriptEntry, String> codeCol = new TableColumn<>("COURSE CODE");
                codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
                codeCol.setPrefWidth(120);
                codeCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

                TableColumn<TranscriptEntry, String> nameCol = new TableColumn<>("COURSE TITLE");
                nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
                nameCol.setPrefWidth(340);

                TableColumn<TranscriptEntry, Integer> creditsCol = new TableColumn<>("CREDITS");
                creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
                creditsCol.setPrefWidth(80);
                creditsCol.setStyle("-fx-alignment: CENTER;");

                TableColumn<TranscriptEntry, String> gradeCol = new TableColumn<>("GRADE");
                gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
                gradeCol.setPrefWidth(80);
                // Color-code grades: Red for failing grades (DC, DD, FD, FF - below CC/2.00)
                gradeCol.setCellFactory(column -> new TableCell<TranscriptEntry, String>() {
                    @Override
                    protected void updateItem(String grade, boolean empty) {
                        super.updateItem(grade, empty);
                        if (empty || grade == null) {
                            setText(null);
                            setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
                        } else {
                            setText(grade);
                            // Failing grades: DC, DD, FD, FF (below CC/2.00)
                            if (grade.equals("FF") || grade.equals("FD") || grade.equals("DD") || grade.equals("DC")) {
                                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #e53935;"); // Red for failed
                            } else {
                                setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #43a047;"); // Green for passed
                            }
                        }
                    }
                });

                TableColumn<TranscriptEntry, String> typeCol = new TableColumn<>("TYPE");
                typeCol.setCellValueFactory(cellData -> {
                    String display = cellData.getValue().getEntryTypeDisplay();
                    return new javafx.beans.property.SimpleStringProperty(display);
                });
                typeCol.setPrefWidth(110);
                typeCol.setStyle("-fx-alignment: CENTER;");

                table.getColumns().addAll(codeCol, nameCol, creditsCol, gradeCol, typeCol);
                table.setItems(FXCollections.observableArrayList(semEntry.getValue()));

                // Semester summary
                int semCredits = semEntry.getValue().stream().mapToInt(TranscriptEntry::getCredits).sum();
                double semGPA = calculateSemesterGPA(semEntry.getValue());

                HBox semSummary = new HBox(30);
                semSummary.setAlignment(Pos.CENTER_RIGHT);
                semSummary.setStyle("-fx-background-color: #f5f5f5; -fx-padding: 10;");

                Label creditsLabel = new Label("Semester Credits: " + semCredits);
                creditsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

                Label gpaLabel = new Label("Semester GPA: " + String.format("%.2f", semGPA));
                gpaLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
                gpaLabel.setTextFill(Color.web("#1976d2"));

                semSummary.getChildren().addAll(creditsLabel, gpaLabel);

                semesterBox.getChildren().addAll(semesterHeader, table, semSummary);
                transcriptContent.getChildren().add(semesterBox);
            }

            // ========== GRADING SCALE LEGEND ==========
            VBox legend = new VBox(8);
            legend.setStyle("-fx-background-color: #fafafa; -fx-padding: 15; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

            Label legendTitle = new Label("GRADING SCALE");
            legendTitle.setFont(Font.font("Arial", FontWeight.BOLD, 12));
            legendTitle.setTextFill(Color.web("#424242"));

            GridPane gradeScale = new GridPane();
            gradeScale.setHgap(25);
            gradeScale.setVgap(5);

            String[] grades = {"AA", "BA", "BB", "CB", "CC", "DC", "DD", "FD", "FF"};
            String[] points = {"4.00", "3.50", "3.00", "2.50", "2.00", "1.50", "1.00", "0.50", "0.00"};

            for (int i = 0; i < grades.length; i++) {
                Label gradeLabel = new Label(grades[i] + " = " + points[i]);
                gradeLabel.setFont(Font.font("Consolas", 11));
                gradeScale.add(gradeLabel, i % 5, i / 5);
            }

            Label passingNote = new Label("Note: Minimum passing grade is CC (2.00)");
            passingNote.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            passingNote.setTextFill(Color.web("#757575"));

            legend.getChildren().addAll(legendTitle, gradeScale, passingNote);
            transcriptContent.getChildren().add(legend);

            // ========== FOOTER ==========
            VBox footer = new VBox(5);
            footer.setAlignment(Pos.CENTER);
            footer.setStyle("-fx-padding: 20 0 10 0;");

            Label footerLine = new Label("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            footerLine.setFont(Font.font(10));
            footerLine.setTextFill(Color.web("#bdbdbd"));

            Label footerText = new Label("This is an official academic transcript • Generated by Student Information System");
            footerText.setFont(Font.font("Arial", FontPosture.ITALIC, 10));
            footerText.setTextFill(Color.web("#9e9e9e"));

            // Print Button
            Button printButton = new Button("🖶 Print Transcript");
            printButton.setStyle(
                "-fx-background-color: #1976d2; " +
                "-fx-text-fill: white; " +
                "-fx-font-size: 14px; " +
                "-fx-font-weight: bold; " +
                "-fx-padding: 12 30; " +
                "-fx-background-radius: 5; " +
                "-fx-cursor: hand;"
            );
            printButton.setOnAction(ev -> {
                showAlert(Alert.AlertType.INFORMATION, "Print", "Print functionality will be implemented.\nYou can use your browser's print function (Ctrl+P) for now.");
            });

            footer.getChildren().addAll(footerLine, footerText, printButton);
            transcriptContent.getChildren().add(footer);
        }

        ScrollPane scrollPane = new ScrollPane(transcriptContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(title, scrollPane);
        mainLayout.setCenter(content);
    }

    // Helper methods for transcript
    private void addInfoRow(GridPane grid, int row, String label, String value) {
        Label labelNode = new Label(label);
        labelNode.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        labelNode.setTextFill(Color.web("#546e7a"));

        Label valueNode = new Label(value);
        valueNode.setFont(Font.font("Arial", 11));
        valueNode.setTextFill(Color.web("#263238"));

        grid.add(labelNode, 0, row);
        grid.add(valueNode, 1, row);
    }

    private VBox createStatBox(String title, String value, String color) {
        VBox box = new VBox(5);
        box.setAlignment(Pos.CENTER);
        box.setPrefWidth(120);
        box.setPrefHeight(80);
        box.setStyle("-fx-border-color: " + color + "; -fx-border-width: 0 0 0 4; -fx-padding: 10;");

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        titleLabel.setTextFill(Color.web("#757575"));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        valueLabel.setTextFill(Color.web(color));

        box.getChildren().addAll(titleLabel, valueLabel);
        return box;
    }

    private double calculateSemesterGPA(List<TranscriptEntry> courses) {
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (TranscriptEntry entry : courses) {
            // GPA hesaplamasında TÜM dersler dahil edilir (başarısız dersler dahil)
            totalPoints += entry.getGradePoint() * entry.getCredits();
            totalCredits += entry.getCredits();
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
}
