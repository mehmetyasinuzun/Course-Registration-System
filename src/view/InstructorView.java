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
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import controller.InstructorController;
import controller.AuthController;
import model.*;
import java.util.List;

public class InstructorView {

    private Stage stage;
    private User currentUser;
    private InstructorController instructorController = new InstructorController();
    private BorderPane mainLayout;

    private TableView<Course> courseTable;
    private TableView<Registration> registrationTable;
    private TableView<Notification> notificationTable;

    public InstructorView(Stage stage, User user) {
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
        stage.setTitle("Instructor Panel - " + currentUser.getFullName());
        stage.centerOnScreen();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #1e3a5f;");

        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 30, 20));
        header.setStyle("-fx-background-color: #152d4a;");

        Label logoLabel = new Label("👨‍🏫");
        logoLabel.setFont(Font.font(40));

        Label titleLabel = new Label("Instructor Panel");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Label userLabel = new Label(currentUser.getFullName());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web("#8fb8de"));

        header.getChildren().addAll(logoLabel, titleLabel, userLabel);

        VBox menuBox = new VBox(2);
        menuBox.setPadding(new Insets(20, 10, 20, 10));

        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button coursesBtn = createMenuButton("📚 My Courses", false);
        Button myRequestsBtn = createMenuButton("📝 My Course Requests", false);
        Button registrationsBtn = createMenuButton("📋 Registration Requests", false);
        Button notificationsBtn = createMenuButton("🔔 Notifications", false);
        Button logoutBtn = createMenuButton("🚪 Logout", false);

        // Unread notification count
        int unreadCount = instructorController.getUnreadCount(currentUser.getId());
        if (unreadCount > 0) {
            notificationsBtn.setText("🔔 Notifications (" + unreadCount + ")");
        }

        dashboardBtn.setOnAction(e -> { resetMenuButtons(menuBox); setActiveButton(dashboardBtn); showDashboard(); });
        coursesBtn.setOnAction(e -> { resetMenuButtons(menuBox); setActiveButton(coursesBtn); showCourses(); });
        myRequestsBtn.setOnAction(e -> { resetMenuButtons(menuBox); setActiveButton(myRequestsBtn); showMyRequests(); });
        registrationsBtn.setOnAction(e -> { resetMenuButtons(menuBox); setActiveButton(registrationsBtn); showRegistrations(); });
        notificationsBtn.setOnAction(e -> { resetMenuButtons(menuBox); setActiveButton(notificationsBtn); showNotifications(); });
        logoutBtn.setOnAction(e -> logout());

        logoutBtn.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: #e74c3c;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 12 20;" +
                        "-fx-alignment: CENTER_LEFT;" +
                        "-fx-cursor: hand;"
        );

        menuBox.getChildren().addAll(dashboardBtn, coursesBtn, myRequestsBtn, registrationsBtn, notificationsBtn);

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
                            "-fx-text-fill: #8fb8de;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 20;" +
                            "-fx-cursor: hand;"
            );
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#2980b9")) {
                btn.setStyle(
                        "-fx-background-color: #2a4a6b;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 5;"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#2980b9")) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #8fb8de;" +
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
                "-fx-background-color: #2980b9;" +
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
                                "-fx-text-fill: #8fb8de;" +
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

        List<Course> myCourses = instructorController.getMyCourses(currentUser.getId());
        List<Registration> pendingRegs = instructorController.getPendingRegistrations(currentUser.getId());

        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
                createStatCard("📚", "My Courses", String.valueOf(myCourses.size()), "#2980b9"),
                createStatCard("📋", "Pending Registrations", String.valueOf(pendingRegs.size()), "#e67e22"),
                createStatCard("🔔", "Unread", String.valueOf(instructorController.getUnreadCount(currentUser.getId())), "#9b59b6")
        );

        // My courses list - ScrollPane + VBox for responsive design
        VBox coursesBox = new VBox(15);
        coursesBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        coursesBox.setPadding(new Insets(20));

        Label coursesTitle = new Label("My Courses (" + myCourses.size() + " courses)");
        coursesTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        coursesTitle.setTextFill(Color.web("#2c3e50"));

        if (myCourses.isEmpty()) {
            Label noData = new Label("No courses assigned yet.");
            noData.setTextFill(Color.web("#555555"));
            coursesBox.getChildren().addAll(coursesTitle, noData);
        } else {
            VBox listBox = new VBox(10);
            listBox.setStyle("-fx-background-color: white;");

            for (Course course : myCourses) {
                HBox item = new HBox(15);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-background-color: #e3f2fd; -fx-padding: 15; -fx-background-radius: 5; -fx-border-color: #bbdefb; -fx-border-radius: 5;");

                Label codeLabel = new Label(course.getCode());
                codeLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                codeLabel.setStyle("-fx-background-color: #1565c0; -fx-text-fill: white; -fx-padding: 5 10; -fx-background-radius: 3;");

                VBox infoBox = new VBox(3);
                Label nameLabel = new Label(course.getName());
                nameLabel.setFont(Font.font("System", FontWeight.BOLD, 14));
                nameLabel.setTextFill(Color.web("#1a1a1a"));

                Label scheduleLabel = new Label(course.getScheduleDisplay());
                scheduleLabel.setFont(Font.font("System", 12));
                scheduleLabel.setTextFill(Color.web("#444444"));
                infoBox.getChildren().addAll(nameLabel, scheduleLabel);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                int enrolled = instructorController.getEnrollmentCount(course.getId());
                Label capacityLabel = new Label(enrolled + "/" + course.getCapacity() + " students");
                capacityLabel.setFont(Font.font("System", FontWeight.BOLD, 12));
                capacityLabel.setTextFill(Color.web("#2e7d32"));

                item.getChildren().addAll(codeLabel, infoBox, spacer, capacityLabel);
                listBox.getChildren().add(item);
            }

            // Wrap course list with ScrollPane - max 250px height
            ScrollPane listScrollPane = new ScrollPane(listBox);
            listScrollPane.setFitToWidth(true);
            listScrollPane.setMaxHeight(250);
            listScrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
            listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            coursesBox.getChildren().addAll(coursesTitle, listScrollPane);
        }

        content.getChildren().addAll(title, statsBox, coursesBox);
        mainLayout.setCenter(content);
    }

    private VBox createStatCard(String icon, String label, String value, String color) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(200);
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

    // ========== MY COURSES ==========
    private void showCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        HBox headerBox = new HBox(20);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("My Courses");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addCourseBtn = new Button("📝 Course Opening Request");
        addCourseBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand; -fx-background-radius: 5;");
        addCourseBtn.setOnAction(e -> showAddCourseDialog());

        headerBox.getChildren().addAll(title, spacer, addCourseBtn);

        courseTable = new TableView<>();
        courseTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

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

        TableColumn<Course, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> {
            Course course = cellData.getValue();
            int enrolled = instructorController.getEnrollmentCount(course.getId());
            return new javafx.beans.property.SimpleStringProperty(enrolled + "/" + course.getCapacity());
        });
        capacityCol.setPrefWidth(100);

        TableColumn<Course, Void> actionCol = new TableColumn<>("Students");
        actionCol.setPrefWidth(120);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button viewBtn = new Button("👁 View");
            {
                viewBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-cursor: hand;");
                viewBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    showCourseStudents(course);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : viewBtn);
            }
        });

        courseTable.getColumns().addAll(codeCol, nameCol, creditsCol, scheduleCol, capacityCol, actionCol);

        List<Course> courses = instructorController.getMyCourses(currentUser.getId());
        courseTable.setItems(FXCollections.observableArrayList(courses));

        VBox.setVgrow(courseTable, Priority.ALWAYS);
        content.getChildren().addAll(headerBox, courseTable);
        mainLayout.setCenter(content);
    }

    // Course opening request dialog
    private void showAddCourseDialog() {
        // Check if there are courses in the catalog first
        java.util.List<CourseCatalog> catalogCourses = instructorController.getAllCatalogCourses();
        if (catalogCourses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "There are no courses in the course catalog yet!\n\n" +
                            "Please contact the administrator.");
            return;
        }

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Course Opening Request");
        dialog.setHeaderText("Select a course from the catalog and create a request.\nThe course will become active after admin approval.");

        // Form fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(15);
        grid.setPadding(new Insets(20));

        // Search field
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by course code or name...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-font-size: 14px;");

        // Course selection ComboBox
        ComboBox<CourseCatalog> catalogCombo = new ComboBox<>();
        ObservableList<CourseCatalog> allCatalogCourses = FXCollections.observableArrayList(catalogCourses);
        catalogCombo.setItems(allCatalogCourses);
        catalogCombo.setPrefWidth(350);
        catalogCombo.setPromptText("Select a course...");
        catalogCombo.setVisibleRowCount(10);

        // Display catalog course
        catalogCombo.setCellFactory(lv -> new ListCell<CourseCatalog>() {
            @Override
            protected void updateItem(CourseCatalog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("");
                } else {
                    String semesterInfo = item.getSemester() == 0 ? "Elective" : item.getSemester() + ". Semester";
                    setText(item.getCode() + " - " + item.getName() + " (" + item.getCredits() + " Cr, " + semesterInfo + ")");
                }
            }
        });
        catalogCombo.setButtonCell(new ListCell<CourseCatalog>() {
            @Override
            protected void updateItem(CourseCatalog item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText("Select a course...");
                } else {
                    setText(item.getCode() + " - " + item.getName() + " (" + item.getCredits() + " Cr)");
                }
            }
        });

        // Search/Filter feature
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                catalogCombo.setItems(allCatalogCourses);
            } else {
                String search = newVal.toUpperCase();
                ObservableList<CourseCatalog> filtered = allCatalogCourses.filtered(c ->
                        c.getCode().toUpperCase().contains(search) ||
                                c.getName().toUpperCase().contains(search)
                );
                catalogCombo.setItems(filtered);
                if (!filtered.isEmpty()) {
                    catalogCombo.getSelectionModel().selectFirst();
                }
            }
        });

        // Info label
        Label infoLabel = new Label("💡 Type in the box above to search, then select from below");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 11px;");

        // Update info on selection
        catalogCombo.setOnAction(e -> {
            CourseCatalog selected = catalogCombo.getValue();
            if (selected != null) {
                infoLabel.setText("✅ " + selected.getCode() + " | " + selected.getCredits() + " Credits | " + selected.getSemesterDisplay());
                infoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-font-size: 12px;");
            }
        });

        Spinner<Integer> capacitySpinner = new Spinner<>(5, 200, 30, 5);
        capacitySpinner.setEditable(true);

        ComboBox<String> dayBox = new ComboBox<>();
        dayBox.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        dayBox.setValue("Monday");

        ComboBox<String> startTimeBox = new ComboBox<>();
        ComboBox<String> endTimeBox = new ComboBox<>();
        
        // Class schedule: 40 min class + 10 min break, lunch break 12:00-13:00
        // Each option shows the time slot (e.g., "08:00-08:40" means class starts at 08:00 and ends at 08:40)
        String[] classTimes = {
            "08:00-08:40", "08:50-09:30", "09:40-10:20", "10:30-11:10", "11:20-12:00",
            "13:00-13:40", "13:50-14:30", "14:40-15:20", "15:30-16:10", "16:20-17:00",
            "17:10-17:50", "18:00-18:40", "18:50-19:30", "19:40-20:20", "20:30-21:10"
        };
        
        startTimeBox.getItems().addAll(classTimes);
        endTimeBox.getItems().addAll(classTimes);
        startTimeBox.setValue("08:00-08:40");
        endTimeBox.setValue("08:00-08:40");

        grid.add(new Label("🔍 Search:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(new Label("📖 Course:"), 0, 1);
        grid.add(catalogCombo, 1, 1);
        grid.add(infoLabel, 1, 2);
        grid.add(new Label("👥 Capacity:"), 0, 3);
        grid.add(capacitySpinner, 1, 3);
        grid.add(new Label("📅 Day:"), 0, 4);
        grid.add(dayBox, 1, 4);
        grid.add(new Label("🕐 Start:"), 0, 5);
        grid.add(startTimeBox, 1, 5);
        grid.add(new Label("🕐 End:"), 0, 6);
        grid.add(endTimeBox, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Customize OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Create Request");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        // On OK
        okButton.addEventFilter(javafx.event.ActionEvent.ACTION, event -> {
            // Validations
            if (catalogCombo.getValue() == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course from the catalog!");
                event.consume();
                return;
            }

            String startTime = startTimeBox.getValue();
            String endTime = endTimeBox.getValue();

            // Time validation for format "08:00-08:40"
            if (!isValidTimeRange(startTime, endTime)) {
                showAlert(Alert.AlertType.ERROR, "Error", "End time must be after or equal to start time!");
                event.consume();
                return;
            }

            // Parse time slots to extract start and end times
            // Format: "08:00-08:40" -> startTime="08:00", endTime="08:40"
            String actualStartTime = startTime.split("-")[0];
            String actualEndTime = endTime.split("-")[1];

            CourseCatalog selectedCatalog = catalogCombo.getValue();

            // Create request
            String result = instructorController.createCourseRequestFromCatalog(
                    selectedCatalog.getId(),
                    capacitySpinner.getValue(),
                    currentUser.getId(),
                    dayBox.getValue(),
                    actualStartTime,
                    actualEndTime
            );

            if (result.startsWith("SUCCESS")) {
                showAlert(Alert.AlertType.INFORMATION, "Request Created",
                        selectedCatalog.getCode() + " - " + selectedCatalog.getName() + "\n\n" + result);
                // Let dialog close
            } else {
                showAlert(Alert.AlertType.ERROR, "Error", result);
                event.consume(); // Keep dialog open
            }
        });

        dialog.showAndWait();
    }

    // Show instructor's course requests
    private void showMyRequests() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("My Course Requests");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Label subtitle = new Label("Your course opening requests are listed below.");
        subtitle.setTextFill(Color.web("#7f8c8d"));

        TableView<CourseRequest> requestTable = new TableView<>();
        requestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<CourseRequest, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<CourseRequest, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(200);

        TableColumn<CourseRequest, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleDisplay()));
        scheduleCol.setPrefWidth(150);

        TableColumn<CourseRequest, String> statusCol = new TableColumn<>("Status");
        statusCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getStatusDisplay()));
        statusCol.setPrefWidth(100);
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
                        case "Pending" -> setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-alignment: center;");
                        case "Approved" -> setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-alignment: center;");
                        case "Rejected" -> setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-alignment: center;");
                        default -> setStyle("-fx-alignment: center;");
                    }
                }
            }
        });

        TableColumn<CourseRequest, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateCol.setPrefWidth(150);

        requestTable.getColumns().addAll(codeCol, nameCol, scheduleCol, statusCol, dateCol);

        List<CourseRequest> requests = instructorController.getMyCourseRequests(currentUser.getId());
        requestTable.setItems(FXCollections.observableArrayList(requests));

        VBox.setVgrow(requestTable, Priority.ALWAYS);
        content.getChildren().addAll(title, subtitle, requestTable);
        mainLayout.setCenter(content);
    }

    private void showCourseStudents(Course course) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(course.getCode() + " - Enrolled Students");
        dialog.setHeaderText(course.getName());

        VBox content = new VBox(10);
        content.setPadding(new Insets(20));
        content.setPrefWidth(400);

        List<Registration> registrations = instructorController.getCourseRegistrations(course.getId());

        if (registrations.isEmpty()) {
            content.getChildren().add(new Label("No students are enrolled in this course."));
        } else {
            for (Registration reg : registrations) {
                User student = instructorController.getStudentById(reg.getStudentId());
                if (student != null) {
                    HBox item = new HBox(10);
                    item.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 10; -fx-background-radius: 5;");

                    Label nameLabel = new Label(student.getFullName());
                    if (student instanceof Student) {
                        nameLabel.setText(student.getFullName() + " (" + ((Student) student).getStudentNumber() + ")");
                    }

                    item.getChildren().add(nameLabel);
                    content.getChildren().add(item);
                }
            }
        }

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(300);

        dialog.getDialogPane().setContent(scrollPane);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ========== REGISTRATION REQUESTS ==========
    private void showRegistrations() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("Registration Requests");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        registrationTable = new TableView<>();
        registrationTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<Registration, String> studentCol = new TableColumn<>("Student");
        studentCol.setCellValueFactory(cellData -> {
            User student = instructorController.getStudentById(cellData.getValue().getStudentId());
            String text = student != null ? student.getFullName() : "-";
            if (student instanceof Student) {
                text += " (" + ((Student) student).getStudentNumber() + ")";
            }
            return new javafx.beans.property.SimpleStringProperty(text);
        });
        studentCol.setPrefWidth(250);

        TableColumn<Registration, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> {
            Course course = instructorController.getCourseById(cellData.getValue().getCourseId());
            return new javafx.beans.property.SimpleStringProperty(course != null ? course.getCode() + " - " + course.getName() : "-");
        });
        courseCol.setPrefWidth(250);

        TableColumn<Registration, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateCol.setPrefWidth(150);

        TableColumn<Registration, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✓ Approve");
            private final Button rejectBtn = new Button("✗ Reject");
            private final HBox box = new HBox(5, approveBtn, rejectBtn);
            {
                approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                approveBtn.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    instructorController.approveRegistration(reg.getId());
                    refreshRegistrationTable();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration approved!");
                });

                rejectBtn.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    instructorController.rejectRegistration(reg.getId());
                    refreshRegistrationTable();
                    showAlert(Alert.AlertType.INFORMATION, "Info", "Registration rejected.");
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        registrationTable.getColumns().addAll(studentCol, courseCol, dateCol, actionCol);
        refreshRegistrationTable();

        VBox.setVgrow(registrationTable, Priority.ALWAYS);
        content.getChildren().addAll(title, registrationTable);
        mainLayout.setCenter(content);
    }

    private void refreshRegistrationTable() {
        List<Registration> pending = instructorController.getPendingRegistrations(currentUser.getId());
        registrationTable.setItems(FXCollections.observableArrayList(pending));
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
            instructorController.markAllNotificationsAsRead(currentUser.getId());
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
                        setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-alignment: center;");
                    } else {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-alignment: center;");
                    }
                }
            }
        });

        notificationTable.getColumns().addAll(titleCol, messageCol, dateCol, statusCol);
        refreshNotificationTable();

        // Mark as read on click
        notificationTable.setOnMouseClicked(e -> {
            Notification selected = notificationTable.getSelectionModel().getSelectedItem();
            if (selected != null && !selected.isRead()) {
                instructorController.markNotificationAsRead(selected.getId());
                refreshNotificationTable();
            }
        });

        VBox.setVgrow(notificationTable, Priority.ALWAYS);
        content.getChildren().addAll(header, notificationTable);
        mainLayout.setCenter(content);
    }

    private void refreshNotificationTable() {
        List<Notification> notifications = instructorController.getNotifications(currentUser.getId());
        notificationTable.setItems(FXCollections.observableArrayList(notifications));
    }

    // ========== HELPERS ==========
    private void logout() {
        new AuthController().logout();
        new LoginView().start(stage);
    }

    // Time range check for format "08:00-08:40"
    private boolean isValidTimeRange(String startTime, String endTime) {
        try {
            // Extract end time from the time range (e.g., "08:00-08:40" -> "08:40")
            String startEndTime = startTime.split("-")[1];
            String endEndTime = endTime.split("-")[1];
            
            String[] startParts = startEndTime.split(":");
            String[] endParts = endEndTime.split(":");
            int startMinutes = Integer.parseInt(startParts[0]) * 60 + Integer.parseInt(startParts[1]);
            int endMinutes = Integer.parseInt(endParts[0]) * 60 + Integer.parseInt(endParts[1]);
            
            // End time must be greater than or equal to start time (allows same slot for 1 hour course)
            return endMinutes >= startMinutes;
        } catch (Exception e) {
            return false;
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Text Overflow fix - Text Wrapping
        alert.getDialogPane().setMinWidth(450);
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setWrapText(true));

        alert.showAndWait();
    }
}
