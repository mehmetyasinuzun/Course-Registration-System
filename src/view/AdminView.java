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
import javafx.scene.control.cell.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import controller.AdminController;
import controller.AuthController;
import model.*;
import util.NavigationManager;
import util.SessionManager;
import java.util.List;
import java.util.ArrayList;

public class AdminView {

    private Stage stage;
    private User currentUser;
    private AdminController adminController = new AdminController();
    private BorderPane mainLayout;

    // Tables
    private TableView<User> studentTable;
    private TableView<User> instructorTable;
    private TableView<Course> courseTable;
    private TableView<Registration> registrationTable;
    private TableView<CourseRequest> courseRequestTable;
    private TableView<CourseCatalog> catalogTable;

    public AdminView(Stage stage, User user) {
        this.stage = stage;
        this.currentUser = user;
    }

    public void show() {
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f2f5;");

        // Left menu
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);

        // Main content - start with Dashboard
        showDashboard();

        Scene scene = new Scene(mainLayout, 1200, 750);
        stage.setScene(scene);
        stage.setTitle("Faculty Panel - " + currentUser.getFullName());
        stage.centerOnScreen();
    }

    private VBox createSidebar() {
        VBox sidebar = new VBox(5);
        sidebar.setPrefWidth(250);
        sidebar.setStyle("-fx-background-color: #2c3e50;");
        sidebar.setPadding(new Insets(0));

        // Logo/Title
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(30, 20, 30, 20));
        header.setStyle("-fx-background-color: #1a252f;");

        Label logoLabel = new Label("🎓");
        logoLabel.setFont(Font.font(40));

        Label titleLabel = new Label("Faculty Panel");
        titleLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        titleLabel.setTextFill(Color.WHITE);

        Label userLabel = new Label(currentUser.getFullName());
        userLabel.setFont(Font.font("System", 12));
        userLabel.setTextFill(Color.web("#95a5a6"));

        header.getChildren().addAll(logoLabel, titleLabel, userLabel);

        // Menu buttons
        VBox menuBox = new VBox(2);
        menuBox.setPadding(new Insets(20, 10, 20, 10));

        Button dashboardBtn = createMenuButton("📊 Dashboard", true);
        Button studentsBtn = createMenuButton("👨‍🎓 Students", false);
        Button instructorsBtn = createMenuButton("👨‍🏫 Instructors", false);
        Button transcriptBtn = createMenuButton("📜 Transcript", false);
        Button creditTransferBtn = createMenuButton("🔄 Credit Transfer", false);
        Button catalogBtn = createMenuButton("📖 Course Catalog", false);
        Button coursesBtn = createMenuButton("📚 Open Courses", false);
        Button courseRequestsBtn = createMenuButton("📝 Course Requests", false);
        Button registrationsBtn = createMenuButton("📋 Registration Requests", false);
        Button logoutBtn = createMenuButton("🚪 Log Out", false);

        // Show pending course request count
        int pendingCourseRequests = adminController.getPendingCourseRequestCount();
        if (pendingCourseRequests > 0) {
            courseRequestsBtn.setText("📝 Course Requests (" + pendingCourseRequests + ")");
        }

        dashboardBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(dashboardBtn);
            NavigationManager.navigateTo("Dashboard");  // Stack'e ekle
            showDashboard();
        });
        studentsBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(studentsBtn);
            NavigationManager.navigateTo("Students");  // Stack'e ekle
            showStudents();
        });
        instructorsBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(instructorsBtn);
            NavigationManager.navigateTo("Instructors");  // Stack'e ekle
            showInstructors();
        });
        transcriptBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(transcriptBtn);
            NavigationManager.navigateTo("Transcript");  // Stack'e ekle
            showTranscript();
        });
        creditTransferBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(creditTransferBtn);
            NavigationManager.navigateTo("CreditTransfer");  // Stack'e ekle
            showCreditTransfer();
        });
        catalogBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(catalogBtn);
            NavigationManager.navigateTo("CourseCatalog");  // Stack'e ekle
            showCourseCatalog();
        });
        coursesBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(coursesBtn);
            NavigationManager.navigateTo("Courses");  // Stack'e ekle
            showCourses();
        });
        courseRequestsBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(courseRequestsBtn);
            NavigationManager.navigateTo("CourseRequests");  // Stack'e ekle
            showCourseRequests();
        });
        registrationsBtn.setOnAction(e -> {
            resetMenuButtons(menuBox);
            setActiveButton(registrationsBtn);
            NavigationManager.navigateTo("Registrations");  // Stack'e ekle
            showRegistrations();
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

        menuBox.getChildren().addAll(dashboardBtn, studentsBtn, instructorsBtn, transcriptBtn, creditTransferBtn, catalogBtn, coursesBtn, courseRequestsBtn, registrationsBtn);

        // Spacer
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
                            "-fx-text-fill: #bdc3c7;" +
                            "-fx-font-size: 14px;" +
                            "-fx-padding: 12 20;" +
                            "-fx-cursor: hand;"
            );
        }

        btn.setOnMouseEntered(e -> {
            if (!btn.getStyle().contains("#3498db")) {
                btn.setStyle(
                        "-fx-background-color: #34495e;" +
                                "-fx-text-fill: white;" +
                                "-fx-font-size: 14px;" +
                                "-fx-padding: 12 20;" +
                                "-fx-cursor: hand;" +
                                "-fx-background-radius: 5;"
                );
            }
        });

        btn.setOnMouseExited(e -> {
            if (!btn.getStyle().contains("#3498db")) {
                btn.setStyle(
                        "-fx-background-color: transparent;" +
                                "-fx-text-fill: #bdc3c7;" +
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
                "-fx-background-color: #3498db;" +
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
                                "-fx-text-fill: #bdc3c7;" +
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

        // Stat cards
        HBox statsBox = new HBox(20);
        statsBox.getChildren().addAll(
                createStatCard("👨‍🎓", "Total Students", String.valueOf(adminController.getTotalStudents()), "#3498db"),
                createStatCard("👨‍🏫", "Total Instructors", String.valueOf(adminController.getTotalInstructors()), "#2ecc71"),
                createStatCard("📚", "Total Courses", String.valueOf(adminController.getTotalCourses()), "#9b59b6"),
                createStatCard("📋", "Pending Registrations", String.valueOf(adminController.getPendingCount()), "#e74c3c")
        );

        // Recent registration requests - responsive with ScrollPane + VBox
        VBox recentBox = new VBox(15);
        recentBox.setStyle("-fx-background-color: white; -fx-background-radius: 10;");
        recentBox.setPadding(new Insets(20));

        List<Registration> pending = adminController.getPendingRegistrations();

        Label recentTitle = new Label("Recent Registration Requests (" + pending.size() + " pending)");
        recentTitle.setFont(Font.font("System", FontWeight.BOLD, 16));
        recentTitle.setTextFill(Color.web("#2c3e50"));

        if (pending.isEmpty()) {
            Label noData = new Label("No pending registration requests.");
            noData.setTextFill(Color.web("#555555"));
            recentBox.getChildren().addAll(recentTitle, noData);
        } else {
            VBox listBox = new VBox(10);
            listBox.setStyle("-fx-background-color: white;");

            // AdminView.java içindeki showDashboard metodundaki döngü kısmı:

            for (int i = 0; i < Math.min(10, pending.size()); i++) {
                Registration reg = pending.get(i);
                User student = adminController.getUserById(reg.getStudentId());
                Course course = adminController.getCourseById(reg.getCourseId());

                // KRİTİK GÜNCELLEME: Eğer öğrenci veya ders bulunamazsa bu kaydı atla
                if (student == null || course == null) {
                    continue;
                }

                HBox item = new HBox(10);
                item.setAlignment(Pos.CENTER_LEFT);
                item.setStyle("-fx-background-color: #fff3e0; -fx-padding: 12; -fx-background-radius: 5; -fx-border-color: #ffe0b2; -fx-border-radius: 5;");

                // Artık course.getCode() güvenle çağrılabilir
                Label info = new Label(student.getFullName() + " → " + course.getCode() + " - " + course.getName());
                info.setFont(Font.font("System", FontWeight.BOLD, 13));
                info.setTextFill(Color.web("#1a1a1a"));

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label status = new Label("Pending");
                status.setStyle("-fx-background-color: #e65100; -fx-text-fill: white; -fx-padding: 3 8; -fx-background-radius: 3;");

                item.getChildren().addAll(info, spacer, status);
                listBox.getChildren().add(item);
            }

            // Wrap list with ScrollPane - max 250px height
            ScrollPane listScrollPane = new ScrollPane(listBox);
            listScrollPane.setFitToWidth(true);
            listScrollPane.setMaxHeight(250);
            listScrollPane.setStyle("-fx-background-color: white; -fx-background: white;");
            listScrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            listScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

            recentBox.getChildren().addAll(recentTitle, listScrollPane);
        }

        content.getChildren().addAll(title, statsBox, recentBox);
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

    // ========== STUDENTS ==========
    private void showStudents() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        // Title and button
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Student Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ New Student");
        addBtn.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> showAddStudentDialog());

        header.getChildren().addAll(title, spacer, addBtn);

        // Table
        studentTable = new TableView<>();
        studentTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(200);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(200);

        TableColumn<User, String> studentNumCol = new TableColumn<>("Student No");
        studentNumCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user instanceof Student) {
                return new javafx.beans.property.SimpleStringProperty(((Student) user).getStudentNumber());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        studentNumCol.setPrefWidth(100);

        TableColumn<User, String> yearSemCol = new TableColumn<>("Year/Semester");
        yearSemCol.setCellValueFactory(cellData -> {
            User user = cellData.getValue();
            if (user instanceof Student student) {
                return new javafx.beans.property.SimpleStringProperty(student.getYearSemesterDisplay());
            }
            return new javafx.beans.property.SimpleStringProperty("");
        });
        yearSemCol.setPrefWidth(120);

        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");

            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (showConfirmDialog("Delete Confirmation", user.getFullName() + " will be deleted. Are you sure?")) {
                        adminController.deleteUser(user.getId());
                        refreshStudentTable();
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        studentTable.getColumns().addAll(idCol, nameCol, usernameCol, emailCol, studentNumCol, yearSemCol, actionCol);

        refreshStudentTable();

        VBox.setVgrow(studentTable, Priority.ALWAYS);
        content.getChildren().addAll(header, studentTable);
        mainLayout.setCenter(content);
    }

    private void refreshStudentTable() {
        List<User> students = adminController.getAllStudents();
        studentTable.setItems(FXCollections.observableArrayList(students));
    }

    private void showAddStudentDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Student");
        dialog.setHeaderText("Enter student information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        nameField.setPromptText("Full Name");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Username");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");

        // --- ŞİFRE ALANI ---
        VBox passwordBox = new VBox(5);
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-font-size: 11px;");
        strengthLabel.setWrapText(true);
        strengthLabel.setPrefWidth(300);
        strengthLabel.setMinHeight(90);
        strengthLabel.setText("✗ At least 8 characters\n✗ One uppercase letter (A-Z)\n✗ One lowercase letter (a-z)\n✗ One digit (0-9)\n✗ One special character (!@#$%...)");
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
        passwordBox.getChildren().addAll(passwordField, strengthLabel);

        // --- E-POSTA ALANI ---
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        VBox emailBox = new VBox(5);
        Label emailStatusLabel = new Label("");
        emailStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
        emailBox.getChildren().addAll(emailField, emailStatusLabel);

        TextField studentNumField = new TextField();
        studentNumField.setPromptText("Student No");
        // Only allow numeric input for student number
        studentNumField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*")) {
                studentNumField.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });

        ComboBox<String> yearCombo = new ComboBox<>();
        yearCombo.getItems().addAll("Year 1", "Year 2", "Year 3", "Year 4");
        yearCombo.setValue("Year 1");

        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().addAll("Fall (1)", "Spring (2)");
        semesterCombo.setValue("Fall (1)");

        Label nameLabel = new Label("Full Name:");
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Label emailLabel = new Label("Email:");
        Label studentNoLabel = new Label("Student No:");
        Label yearLabel = new Label("Year:");
        Label semesterLabel = new Label("Semester:");
        
        // Align labels to top for multi-line fields
        GridPane.setValignment(passwordLabel, javafx.geometry.VPos.TOP);
        GridPane.setValignment(emailLabel, javafx.geometry.VPos.TOP);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordBox, 1, 2);
        grid.add(emailLabel, 0, 3);
        grid.add(emailBox, 1, 3);
        grid.add(studentNoLabel, 0, 4);
        grid.add(studentNumField, 1, 4);
        grid.add(yearLabel, 0, 5);
        grid.add(yearCombo, 1, 5);
        grid.add(semesterLabel, 0, 6);
        grid.add(semesterCombo, 1, 6);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Validation helper method
        Runnable validateForm = () -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String email = emailField.getText().trim();
            String studentNum = studentNumField.getText().trim();
            
            service.UserService.PasswordStrength strength = new service.UserService.PasswordStrength(password);
            boolean isPasswordValid = strength.isValid();
            boolean isEmailValid = email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
            boolean isNameValid = !name.isEmpty();
            boolean isUsernameValid = !username.isEmpty();
            boolean isStudentNumValid = !studentNum.isEmpty();
            
            // Update email error label with specific messages
            if (email.isEmpty()) {
                emailStatusLabel.setText("");
            } else if (!email.contains("@")) {
                emailStatusLabel.setText("✗ Email must contain @");
                emailStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            } else if (!email.contains(".")) {
                emailStatusLabel.setText("✗ Email must contain . (dot)");
                emailStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            } else if (email.indexOf("@") >= email.lastIndexOf(".")) {
                emailStatusLabel.setText("✗ Invalid format (. must come after @)");
                emailStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            } else {
                emailStatusLabel.setText("✓ Valid email");
                emailStatusLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
            }
            
            // Enable OK button only if all fields are valid
            okButton.setDisable(!(isNameValid && isUsernameValid && isPasswordValid && isEmailValid && isStudentNumValid));
        };

        // Şifre Doğrulama Dinleyicisi
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            service.UserService.PasswordStrength strength = new service.UserService.PasswordStrength(newVal);
            StringBuilder feedback = new StringBuilder();
            feedback.append(strength.hasMinLength ? "✓" : "✗").append(" At least 8 characters\n");
            feedback.append(strength.hasUpperCase ? "✓" : "✗").append(" One uppercase letter (A-Z)\n");
            feedback.append(strength.hasLowerCase ? "✓" : "✗").append(" One lowercase letter (a-z)\n");
            feedback.append(strength.hasDigit ? "✓" : "✗").append(" One digit (0-9)\n");
            feedback.append(strength.hasSpecial ? "✓" : "✗").append(" One special character (!@#$%...)");
            strengthLabel.setText(feedback.toString());

            if (strength.isValid()) strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
            else strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");

            validateForm.run();
        });

        // Add listeners for other fields
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        studentNumField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = nameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String email = emailField.getText().trim();
                String studentNum = studentNumField.getText().trim();

                int year = yearCombo.getSelectionModel().getSelectedIndex() + 1;
                int semester = semesterCombo.getSelectionModel().getSelectedIndex() + 1;

                // Final validation with specific error messages
                StringBuilder errors = new StringBuilder();
                
                if (name.isEmpty()) {
                    errors.append("• Full Name is required\n");
                }
                if (username.isEmpty()) {
                    errors.append("• Username is required\n");
                }
                
                String passwordValidation = service.UserService.validatePassword(password);
                if (!passwordValidation.equals("VALID")) {
                    errors.append("• ").append(passwordValidation).append("\n");
                }
                
                if (email.isEmpty()) {
                    errors.append("• Email is required\n");
                } else if (!email.contains("@") || !email.contains(".") || email.indexOf("@") >= email.lastIndexOf(".")) {
                    errors.append("• Please enter a valid email address\n");
                }
                
                if (studentNum.isEmpty()) {
                    errors.append("• Student No is required\n");
                }
                
                if (errors.length() > 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
                    return;
                }

                int id = adminController.addStudent(username, password, name, email, studentNum, year, semester);
                if (id > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Student added!");
                    refreshStudentTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Student could not be added! Username may already exist.");
                }
            }
        });
    }

    // ========== INSTRUCTORS ==========
    private void showInstructors() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Instructor Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ New Instructor");
        addBtn.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> showAddInstructorDialog());

        header.getChildren().addAll(title, spacer, addBtn);

        instructorTable = new TableView<>();
        instructorTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<User, Integer> idCol = new TableColumn<>("ID");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(60);

        TableColumn<User, String> nameCol = new TableColumn<>("Full Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        nameCol.setPrefWidth(250);

        TableColumn<User, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(new PropertyValueFactory<>("username"));
        usernameCol.setPrefWidth(150);

        TableColumn<User, String> emailCol = new TableColumn<>("Email");
        emailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        emailCol.setPrefWidth(250);

        TableColumn<User, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(100);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    User user = getTableView().getItems().get(getIndex());
                    if (showConfirmDialog("Delete Confirmation", user.getFullName() + " will be deleted. Are you sure?")) {
                        adminController.deleteUser(user.getId());
                        refreshInstructorTable();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        instructorTable.getColumns().addAll(idCol, nameCol, usernameCol, emailCol, actionCol);
        refreshInstructorTable();

        VBox.setVgrow(instructorTable, Priority.ALWAYS);
        content.getChildren().addAll(header, instructorTable);
        mainLayout.setCenter(content);
    }

    private void refreshInstructorTable() {
        List<User> instructors = adminController.getAllInstructors();
        instructorTable.setItems(FXCollections.observableArrayList(instructors));
    }

    private void showAddInstructorDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Instructor");
        dialog.setHeaderText("Enter instructor information");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField nameField = new TextField();
        TextField usernameField = new TextField();
        PasswordField passwordField = new PasswordField();

        // Password strength indicator
        VBox passwordBox = new VBox(5);
        Label strengthLabel = new Label("");
        strengthLabel.setStyle("-fx-font-size: 11px;");
        strengthLabel.setWrapText(true);
        strengthLabel.setPrefWidth(300);
        strengthLabel.setMinHeight(90);

        // Show initial requirements
        strengthLabel.setText("✗ At least 8 characters\n✗ One uppercase letter (A-Z)\n✗ One lowercase letter (a-z)\n✗ One digit (0-9)\n✗ One special character (!@#$%...)");
        strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");

        passwordBox.getChildren().addAll(passwordField, strengthLabel);

        TextField emailField = new TextField();
        
        // Email validation label
        Label emailErrorLabel = new Label("");
        emailErrorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
        VBox emailBox = new VBox(5);
        emailBox.getChildren().addAll(emailField, emailErrorLabel);

        Label nameLabel = new Label("Full Name:");
        Label usernameLabel = new Label("Username:");
        Label passwordLabel = new Label("Password:");
        Label emailLabel = new Label("Email:");
        
        // Align labels to top for multi-line fields
        GridPane.setValignment(passwordLabel, javafx.geometry.VPos.TOP);
        GridPane.setValignment(emailLabel, javafx.geometry.VPos.TOP);
        
        grid.add(nameLabel, 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(usernameLabel, 0, 1);
        grid.add(usernameField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordBox, 1, 2);
        grid.add(emailLabel, 0, 3);
        grid.add(emailBox, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setDisable(true);

        // Validation helper method
        Runnable validateForm = () -> {
            String name = nameField.getText().trim();
            String username = usernameField.getText().trim();
            String password = passwordField.getText();
            String email = emailField.getText().trim();
            
            service.UserService.PasswordStrength strength = new service.UserService.PasswordStrength(password);
            boolean isPasswordValid = strength.isValid();
            boolean isEmailValid = email.contains("@") && email.contains(".") && email.indexOf("@") < email.lastIndexOf(".");
            boolean isNameValid = !name.isEmpty();
            boolean isUsernameValid = !username.isEmpty();
            
            // Update email error label with specific messages
            if (email.isEmpty()) {
                emailErrorLabel.setText("");
            } else if (!email.contains("@")) {
                emailErrorLabel.setText("✗ Email must contain @");
                emailErrorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            } else if (!email.contains(".")) {
                emailErrorLabel.setText("✗ Email must contain . (dot)");
                emailErrorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            } else if (email.indexOf("@") >= email.lastIndexOf(".")) {
                emailErrorLabel.setText("✗ Invalid format (. must come after @)");
            } else {
                emailErrorLabel.setText("✓ Valid email");
                emailErrorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
            }
            if (!isEmailValid && !email.isEmpty()) {
                emailErrorLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            }
            
            // Enable OK button only if all fields are valid
            okButton.setDisable(!(isNameValid && isUsernameValid && isPasswordValid && isEmailValid));
        };

        // Real-time password validation
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> {
            service.UserService.PasswordStrength strength = new service.UserService.PasswordStrength(newVal);

            StringBuilder feedback = new StringBuilder();
            feedback.append(strength.hasMinLength ? "✓" : "✗").append(" At least 8 characters\n");
            feedback.append(strength.hasUpperCase ? "✓" : "✗").append(" One uppercase letter (A-Z)\n");
            feedback.append(strength.hasLowerCase ? "✓" : "✗").append(" One lowercase letter (a-z)\n");
            feedback.append(strength.hasDigit ? "✓" : "✗").append(" One digit (0-9)\n");
            feedback.append(strength.hasSpecial ? "✓" : "✗").append(" One special character (!@#$%...)");

            strengthLabel.setText(feedback.toString());

            if (strength.isValid()) {
                strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #27ae60;");
            } else {
                strengthLabel.setStyle("-fx-font-size: 11px; -fx-text-fill: #e74c3c;");
            }
            
            validateForm.run();
        });
        
        // Add listeners for other fields
        nameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());
        emailField.textProperty().addListener((obs, oldVal, newVal) -> validateForm.run());

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String name = nameField.getText().trim();
                String username = usernameField.getText().trim();
                String password = passwordField.getText();
                String email = emailField.getText().trim();

                // Final validation with specific error messages
                StringBuilder errors = new StringBuilder();
                
                if (name.isEmpty()) {
                    errors.append("• Full Name is required\n");
                }
                if (username.isEmpty()) {
                    errors.append("• Username is required\n");
                }
                
                String passwordValidation = service.UserService.validatePassword(password);
                if (!passwordValidation.equals("VALID")) {
                    errors.append("• ").append(passwordValidation).append("\n");
                }
                
                if (email.isEmpty()) {
                    errors.append("• Email is required\n");
                } else if (!email.contains("@") || !email.contains(".") || email.indexOf("@") >= email.lastIndexOf(".")) {
                    errors.append("• Please enter a valid email address\n");
                }
                
                if (errors.length() > 0) {
                    showAlert(Alert.AlertType.ERROR, "Validation Error", errors.toString());
                    return;
                }

                int id = adminController.addInstructor(username, password, name, email);
                if (id > 0) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Instructor added!");
                    refreshInstructorTable();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Instructor could not be added! Username may already exist.");
                }
            }
        });
    }

    // ========== COURSES ==========
    private void showCourses() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        Label title = new Label("Course Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = new Button("+ New Course");
        addBtn.setStyle(
                "-fx-background-color: #27ae60;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-padding: 10 20;" +
                        "-fx-background-radius: 5;" +
                        "-fx-cursor: hand;"
        );
        addBtn.setOnAction(e -> showAddCourseDialog());

        header.getChildren().addAll(title, spacer, addBtn);

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
        scheduleCol.setPrefWidth(150);

        TableColumn<Course, String> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(cellData -> {
            Course course = cellData.getValue();
            int enrolled = adminController.getEnrollmentCount(course.getId());
            return new javafx.beans.property.SimpleStringProperty(enrolled + "/" + course.getCapacity());
        });
        capacityCol.setPrefWidth(80);

        TableColumn<Course, String> instructorCol = new TableColumn<>("Instructor");
        instructorCol.setCellValueFactory(cellData -> {
            User instructor = adminController.getUserById(cellData.getValue().getInstructorId());
            return new javafx.beans.property.SimpleStringProperty(instructor != null ? instructor.getFullName() : "-");
        });
        instructorCol.setPrefWidth(130);

        TableColumn<Course, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(80);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button deleteBtn = new Button("🗑");
            {
                deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");
                deleteBtn.setOnAction(e -> {
                    Course course = getTableView().getItems().get(getIndex());
                    if (showConfirmDialog("Delete Confirmation", course.getCode() + " will be deleted. Are you sure?")) {
                        adminController.deleteCourse(course.getId());
                        refreshCourseTable();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : deleteBtn);
            }
        });

        courseTable.getColumns().addAll(codeCol, nameCol, creditsCol, semesterCol, scheduleCol, capacityCol, instructorCol, actionCol);
        refreshCourseTable();

        VBox.setVgrow(courseTable, Priority.ALWAYS);
        content.getChildren().addAll(header, courseTable);
        mainLayout.setCenter(content);
    }

    private void refreshCourseTable() {
        List<Course> courses = adminController.getAllCourses();
        courseTable.setItems(FXCollections.observableArrayList(courses));
    }

    private void showAddCourseDialog() {
        // Check if catalog has courses first
        List<CourseCatalog> catalogCourses = adminController.getAllCatalogCourses();
        if (catalogCourses.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Warning",
                    "There are no courses in the course catalog yet!\n\n" +
                            "You must define courses first from the 'Course Catalog' menu.");
            return;
        }

        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Open New Course");
        dialog.setHeaderText("Select a course from the catalog and enter details");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        // Course Selection - Search TextField + ComboBox
        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by course code or name...");
        searchField.setPrefWidth(350);
        searchField.setStyle("-fx-font-size: 14px;");

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

        // Search/filter feature
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
                // Select first item after filtering (for convenience)
                if (!filtered.isEmpty()) {
                    catalogCombo.getSelectionModel().selectFirst();
                }
            }
        });

        // Show selected course info
        Label infoLabel = new Label("💡 Type in the box above to search, then select from the list below");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        // Update info label when selection changes
        catalogCombo.setOnAction(e -> {
            CourseCatalog selected = catalogCombo.getValue();
            if (selected != null) {
                infoLabel.setText("✅ " + selected.getCode() + " | " + selected.getCredits() + " Credits | " + selected.getSemesterDisplay());
                infoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
            }
        });

        Spinner<Integer> capacitySpinner = new Spinner<>(5, 200, 30);

        ComboBox<String> dayCombo = new ComboBox<>();
        dayCombo.getItems().addAll("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        dayCombo.setValue("Monday");

        // Time ComboBoxes - 40 min class + 10 min break system
        ComboBox<String> startTimeCombo = new ComboBox<>();
        ComboBox<String> endTimeCombo = new ComboBox<>();

        // Class schedule: 40 min class + 10 min break, lunch break 12:00-13:00
        // Each option shows the time slot (e.g., "08:00-08:40" means class starts at 08:00 and ends at 08:40)
        String[] classTimes = {
            "08:00-08:40", "08:50-09:30", "09:40-10:20", "10:30-11:10", "11:20-12:00",
            "13:00-13:40", "13:50-14:30", "14:40-15:20", "15:30-16:10", "16:20-17:00",
            "17:10-17:50", "18:00-18:40", "18:50-19:30", "19:40-20:20", "20:30-21:10"
        };

        startTimeCombo.getItems().addAll(classTimes);
        endTimeCombo.getItems().addAll(classTimes);
        startTimeCombo.setValue("08:00-08:40");
        endTimeCombo.setValue("08:00-08:40");

        ComboBox<User> instructorCombo = new ComboBox<>();
        List<User> instructors = adminController.getAllInstructors();
        instructorCombo.getItems().addAll(instructors);
        instructorCombo.setPromptText("Select an instructor...");

        // Display instructor name
        instructorCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "" : item.getFullName());
            }
        });
        instructorCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? "Select an instructor..." : item.getFullName());
            }
        });

        // Layout
        grid.add(new Label("🔍 Search:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(new Label("📖 Course:"), 0, 1);
        grid.add(catalogCombo, 1, 1);
        grid.add(infoLabel, 1, 2);
        grid.add(new Label("👥 Capacity:"), 0, 3);
        grid.add(capacitySpinner, 1, 3);
        grid.add(new Label("📅 Day:"), 0, 4);
        grid.add(dayCombo, 1, 4);
        grid.add(new Label("🕐 Start:"), 0, 5);
        grid.add(startTimeCombo, 1, 5);
        grid.add(new Label("🕐 End:"), 0, 6);
        grid.add(endTimeCombo, 1, 6);
        grid.add(new Label("👨‍🏫 Instructor:"), 0, 7);
        grid.add(instructorCombo, 1, 7);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Customize OK button
        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Open Course");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // Validations
                if (catalogCombo.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course from the catalog!");
                    return;
                }

                // Instructor selected?
                if (instructorCombo.getValue() == null) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Please select an instructor!");
                    return;
                }

                // Time check
                String startTime = startTimeCombo.getValue();
                String endTime = endTimeCombo.getValue();
                if (!isValidTimeRange(startTime, endTime)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "End time must be after start time!");
                    return;
                }

                // Parse time slots to extract start and end times
                // Format: "08:00-08:40" -> startTime="08:00", endTime="08:40"
                String actualStartTime = startTime.split("-")[0];
                String actualEndTime = endTime.split("-")[1];

                CourseCatalog selectedCatalog = catalogCombo.getValue();

                String result = adminController.openCourseFromCatalogWithResult(
                        selectedCatalog.getId(),
                        capacitySpinner.getValue(),
                        instructorCombo.getValue().getId(),
                        dayCombo.getValue(),
                        actualStartTime,
                        actualEndTime
                );

                if (result.startsWith("SUCCESS:")) {
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            selectedCatalog.getCode() + " course opened successfully!\n" +
                                    "Credits: " + selectedCatalog.getCredits() + " | Semester: " + selectedCatalog.getSemesterDisplay());
                    refreshCourseTable();
                } else {
                    // Display the detailed error message (includes conflicting course info)
                    showAlert(Alert.AlertType.ERROR, "Error", result.replace("ERROR: ", ""));
                }
            }
        });
    }

    // Course code format check
    private boolean isValidCourseCode(String code) {
        if (code == null || code.length() < 4) return false;
        int letters = 0, digits = 0;
        for (char c : code.toCharArray()) {
            if (Character.isLetter(c)) letters++;
            else if (Character.isDigit(c)) digits++;
        }
        return letters >= 2 && digits >= 2;
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

    // ========== COURSE REQUESTS (from instructors) ==========
    private void showCourseRequests() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("Course Opening Requests");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        Label subtitle = new Label("Instructor course opening requests can be approved or rejected here.");
        subtitle.setTextFill(Color.web("#7f8c8d"));

        courseRequestTable = new TableView<>();
        courseRequestTable.setStyle("-fx-background-color: white; -fx-background-radius: 10;");

        TableColumn<CourseRequest, String> codeCol = new TableColumn<>("Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(80);

        TableColumn<CourseRequest, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(180);

        TableColumn<CourseRequest, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(60);

        TableColumn<CourseRequest, String> scheduleCol = new TableColumn<>("Schedule");
        scheduleCol.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleStringProperty(cellData.getValue().getScheduleDisplay()));
        scheduleCol.setPrefWidth(150);

        TableColumn<CourseRequest, Integer> capacityCol = new TableColumn<>("Capacity");
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        capacityCol.setPrefWidth(80);

        TableColumn<CourseRequest, String> instructorCol = new TableColumn<>("Requested By");
        instructorCol.setCellValueFactory(cellData -> {
            User instructor = adminController.getUserById(cellData.getValue().getInstructorId());
            return new javafx.beans.property.SimpleStringProperty(instructor != null ? instructor.getFullName() : "-");
        });
        instructorCol.setPrefWidth(150);

        TableColumn<CourseRequest, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(new PropertyValueFactory<>("requestDate"));
        dateCol.setPrefWidth(120);

        TableColumn<CourseRequest, Void> actionCol = new TableColumn<>("Action");
        actionCol.setPrefWidth(200);
        actionCol.setCellFactory(col -> new TableCell<>() {
            private final Button approveBtn = new Button("✓ Approve");
            private final Button rejectBtn = new Button("✗ Reject");
            private final HBox box = new HBox(5, approveBtn, rejectBtn);
            {
                approveBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-cursor: hand;");
                rejectBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-cursor: hand;");

                approveBtn.setOnAction(e -> {
                    CourseRequest req = getTableView().getItems().get(getIndex());
                    if (adminController.approveCourseRequest(req.getId())) {
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Course request approved and the course has been opened!");
                        refreshCourseRequestTable();
                    }
                });

                rejectBtn.setOnAction(e -> {
                    CourseRequest req = getTableView().getItems().get(getIndex());
                    if (adminController.rejectCourseRequest(req.getId())) {
                        showAlert(Alert.AlertType.INFORMATION, "Info", "Course request rejected.");
                        refreshCourseRequestTable();
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        courseRequestTable.getColumns().addAll(codeCol, nameCol, creditsCol, scheduleCol, capacityCol, instructorCol, dateCol, actionCol);
        refreshCourseRequestTable();

        VBox.setVgrow(courseRequestTable, Priority.ALWAYS);
        content.getChildren().addAll(title, subtitle, courseRequestTable);
        mainLayout.setCenter(content);
    }

    private void refreshCourseRequestTable() {
        List<CourseRequest> requests = adminController.getPendingCourseRequests();
        courseRequestTable.setItems(FXCollections.observableArrayList(requests));
    }

    // ========== REGISTRATIONS ==========
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
            User student = adminController.getUserById(cellData.getValue().getStudentId());
            return new javafx.beans.property.SimpleStringProperty(student != null ? student.getFullName() : "-");
        });
        studentCol.setPrefWidth(200);

        TableColumn<Registration, String> courseCol = new TableColumn<>("Course");
        courseCol.setCellValueFactory(cellData -> {
            Course course = adminController.getCourseById(cellData.getValue().getCourseId());
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
                    adminController.approveRegistration(reg.getId());
                    refreshRegistrationTable();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Registration approved!");
                });

                rejectBtn.setOnAction(e -> {
                    Registration reg = getTableView().getItems().get(getIndex());
                    adminController.rejectRegistration(reg.getId());
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
        List<Registration> pending = adminController.getPendingRegistrations();
        registrationTable.setItems(FXCollections.observableArrayList(pending));
    }

    // ========== HELPER METHODS ==========
    private void logout() {
        new AuthController().logout();
        new LoginView().start(stage);
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        // Text overflow fix - wrapping
        alert.getDialogPane().setMinWidth(450);
        alert.getDialogPane().getChildren().stream()
                .filter(node -> node instanceof Label)
                .forEach(node -> ((Label) node).setWrapText(true));

        alert.showAndWait();
    }

    private boolean showConfirmDialog(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        return alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK;
    }

    // ========== COURSE CATALOG ==========
    private void showCourseCatalog() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setStyle("-fx-background-color: white;");

        Label title = new Label("📖 Course Catalog");
        title.setFont(Font.font("System", FontWeight.BOLD, 24));

        // Search box
        HBox searchBox = new HBox(10);
        searchBox.setAlignment(Pos.CENTER_LEFT);

        TextField searchField = new TextField();
        searchField.setPromptText("🔍 Search by course code or name...");
        searchField.setPrefWidth(300);
        searchField.setStyle("-fx-font-size: 14px; -fx-padding: 8;");

        ComboBox<String> semesterFilter = new ComboBox<>();
        semesterFilter.getItems().add("All Semesters");
        for (int i = 1; i <= 8; i++) {
            int year = (i + 1) / 2;
            String period = (i % 2 == 1) ? "Fall" : "Spring";
            semesterFilter.getItems().add(i + ". Semester (" + year + ". Year " + period + ")");
        }
        semesterFilter.getItems().add("Elective Courses");
        semesterFilter.setValue("All Semesters");
        semesterFilter.setStyle("-fx-font-size: 14px;");

        Label countLabel = new Label("Total: 0 courses");
        countLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        searchBox.getChildren().addAll(new Label("Search:"), searchField, new Label("Filter:"), semesterFilter, countLabel);

        // Toolbar
        HBox toolbar = new HBox(10);
        toolbar.setAlignment(Pos.CENTER_LEFT);

        Button addBtn = new Button("➕ Add New Course");
        addBtn.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        addBtn.setOnAction(e -> showAddCatalogCourseDialog());

        Button editBtn = new Button("✏️ Edit");
        editBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        editBtn.setOnAction(e -> {
            CourseCatalog selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                showEditCatalogCourseDialog(selected);
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course to edit!");
            }
        });

        Button deleteBtn = new Button("🗑️ Delete");
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 10 20; -fx-cursor: hand;");
        deleteBtn.setOnAction(e -> {
            CourseCatalog selected = catalogTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                if (showConfirmDialog("Delete Confirmation", "Are you sure you want to delete the course with code " + selected.getCode() + " from the catalog?")) {
                    adminController.deleteCatalogCourse(selected.getId());
                    refreshCatalogTable();
                    filterCatalogTable(searchField.getText(), semesterFilter.getSelectionModel().getSelectedIndex(), countLabel);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted from the catalog!");
                }
            } else {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course to delete!");
            }
        });

        toolbar.getChildren().addAll(addBtn, editBtn, deleteBtn);

        // Table
        catalogTable = new TableView<>();
        catalogTable.setPlaceholder(new Label("No courses in the catalog yet.\nClick the button above to add a new course."));

        TableColumn<CourseCatalog, Integer> idCol = new TableColumn<>("#");
        idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
        idCol.setPrefWidth(50);
        idCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CourseCatalog, String> codeCol = new TableColumn<>("Course Code");
        codeCol.setCellValueFactory(new PropertyValueFactory<>("code"));
        codeCol.setPrefWidth(120);

        TableColumn<CourseCatalog, String> nameCol = new TableColumn<>("Course Name");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        nameCol.setPrefWidth(300);

        TableColumn<CourseCatalog, Integer> creditsCol = new TableColumn<>("Credits");
        creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
        creditsCol.setPrefWidth(70);
        creditsCol.setStyle("-fx-alignment: CENTER;");

        TableColumn<CourseCatalog, String> semesterCol = new TableColumn<>("Semester");
        semesterCol.setCellValueFactory(cellData -> {
            CourseCatalog catalog = cellData.getValue();
            return new javafx.beans.property.SimpleStringProperty(catalog.getSemesterDisplay());
        });
        semesterCol.setPrefWidth(150);

        catalogTable.getColumns().addAll(idCol, codeCol, nameCol, creditsCol, semesterCol);
        refreshCatalogTable();

        // Search and filter
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            filterCatalogTable(newVal, semesterFilter.getSelectionModel().getSelectedIndex(), countLabel);
        });

        semesterFilter.setOnAction(e -> {
            filterCatalogTable(searchField.getText(), semesterFilter.getSelectionModel().getSelectedIndex(), countLabel);
        });

        // Show count on first load
        filterCatalogTable("", 0, countLabel);

        VBox.setVgrow(catalogTable, Priority.ALWAYS);

        // Info
        Label infoLabel = new Label("💡 Courses in the catalog are used when opening courses. The code, name, and credit information defined here remains fixed.");
        infoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

        content.getChildren().addAll(title, searchBox, toolbar, catalogTable, infoLabel);
        mainLayout.setCenter(content);
    }

    private void refreshCatalogTable() {
        List<CourseCatalog> catalogs = adminController.getAllCatalogCourses();
        catalogTable.setItems(FXCollections.observableArrayList(catalogs));
    }

    private void filterCatalogTable(String searchText, int semesterIndex, Label countLabel) {
        List<CourseCatalog> allCatalogs = adminController.getAllCatalogCourses();

        List<CourseCatalog> filtered = allCatalogs.stream()
                .filter(c -> {
                    // Text search
                    boolean matchesText = searchText == null || searchText.isEmpty() ||
                            c.getCode().toUpperCase().contains(searchText.toUpperCase()) ||
                            c.getName().toUpperCase().contains(searchText.toUpperCase());

                    // Semester filter: 0=All, 1-8=Semesters, 9=Electives (semester=0)
                    boolean matchesSemester = semesterIndex == 0 || // All
                            (semesterIndex == 9 && c.getSemester() == 0) || // Electives
                            (semesterIndex >= 1 && semesterIndex <= 8 && c.getSemester() == semesterIndex);

                    return matchesText && matchesSemester;
                })
                .toList();

        catalogTable.setItems(FXCollections.observableArrayList(filtered));
        countLabel.setText("Total: " + filtered.size() + " courses");
    }

    private void showAddCatalogCourseDialog() {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Add New Course");
        dialog.setHeaderText("Add a new course to the catalog");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeField = new TextField();
        codeField.setPromptText("e.g., CS101, MATH201");
        codeField.setPrefWidth(200);

        TextField nameField = new TextField();
        nameField.setPromptText("Enter course name");

        Spinner<Integer> creditsSpinner = new Spinner<>(1, 10, 3);

        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().add("All Semesters (General)");
        for (int i = 1; i <= 8; i++) {
            int year = (i + 1) / 2;
            String period = (i % 2 == 1) ? "Fall" : "Spring";
            semesterCombo.getItems().add(year + ". Year - " + period + " (" + i + ". Semester)");
        }
        semesterCombo.setValue("All Semesters (General)");

        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Course Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Credits:"), 0, 2);
        grid.add(creditsSpinner, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semesterCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Add");
        okButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();

                if (code.isEmpty() || name.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Course code and name are required!");
                    return;
                }

                if (!isValidCourseCode(code)) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Invalid course code format!\nExample: CS101, MATH201");
                    return;
                }

                int semester = semesterCombo.getSelectionModel().getSelectedIndex();

                int id = adminController.addCatalogCourse(code, name, creditsSpinner.getValue(), semester);
                if (id > 0) {
                    refreshCatalogTable();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course added to the catalog!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "This course code already exists!");
                }
            }
        });
    }

    private void showEditCatalogCourseDialog(CourseCatalog catalog) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit the course with code " + catalog.getCode());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        TextField codeField = new TextField(catalog.getCode());
        codeField.setPrefWidth(200);

        TextField nameField = new TextField(catalog.getName());

        Spinner<Integer> creditsSpinner = new Spinner<>(1, 10, catalog.getCredits());

        ComboBox<String> semesterCombo = new ComboBox<>();
        semesterCombo.getItems().add("All Semesters (General)");
        for (int i = 1; i <= 8; i++) {
            int year = (i + 1) / 2;
            String period = (i % 2 == 1) ? "Fall" : "Spring";
            semesterCombo.getItems().add(year + ". Year - " + period + " (" + i + ". Semester)");
        }
        semesterCombo.getSelectionModel().select(catalog.getSemester());

        grid.add(new Label("Course Code:"), 0, 0);
        grid.add(codeField, 1, 0);
        grid.add(new Label("Course Name:"), 0, 1);
        grid.add(nameField, 1, 1);
        grid.add(new Label("Credits:"), 0, 2);
        grid.add(creditsSpinner, 1, 2);
        grid.add(new Label("Semester:"), 0, 3);
        grid.add(semesterCombo, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Button okButton = (Button) dialog.getDialogPane().lookupButton(ButtonType.OK);
        okButton.setText("Save");
        okButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white;");

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                String code = codeField.getText().trim().toUpperCase();
                String name = nameField.getText().trim();

                if (code.isEmpty() || name.isEmpty()) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Course code and name are required!");
                    return;
                }

                catalog.setCode(code);
                catalog.setName(name);
                catalog.setCredits(creditsSpinner.getValue());
                catalog.setSemester(semesterCombo.getSelectionModel().getSelectedIndex());

                if (adminController.updateCatalogCourse(catalog)) {
                    refreshCatalogTable();
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Update failed!");
                }
            }
        });
    }

    // ========== TRANSCRIPT PAGE ==========
    private void showTranscript() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("📜 Student Transcript");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Student selection with search
        HBox selectionBox = new HBox(15);
        selectionBox.setAlignment(Pos.CENTER_LEFT);

        Label selectLabel = new Label("Search Student:");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        // Search field for filtering students
        TextField studentSearchField = new TextField();
        studentSearchField.setPromptText("🔍 Type to search by name or number...");
        studentSearchField.setPrefWidth(250);
        studentSearchField.setStyle("-fx-font-size: 14px;");

        ComboBox<User> studentCombo = new ComboBox<>();
        studentCombo.setPrefWidth(350);
        studentCombo.setPromptText("Choose a student...");
        studentCombo.setVisibleRowCount(10);

        ObservableList<User> allStudents = FXCollections.observableArrayList(adminController.getAllStudents());
        studentCombo.setItems(allStudents);

        // Add listener to search field with auto-select first
        studentSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                studentCombo.setItems(allStudents);
            } else {
                String search = newVal.toUpperCase();
                ObservableList<User> filtered = allStudents.filtered(u -> {
                    Student s = (Student) u;
                    return s.getFullName().toUpperCase().contains(search) ||
                           s.getStudentNumber().toUpperCase().contains(search);
                });
                studentCombo.setItems(filtered);
                // Auto-select first item after filtering
                if (!filtered.isEmpty()) {
                    studentCombo.getSelectionModel().selectFirst();
                }
            }
        });

        // Custom cell factory to show student info
        studentCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    Student s = (Student) student;
                    setText(s.getFullName() + " (" + s.getStudentNumber() + ") - " + s.getYearSemesterDisplay());
                }
            }
        });

        studentCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText("Choose a student...");
                } else {
                    Student s = (Student) student;
                    setText(s.getFullName() + " (" + s.getStudentNumber() + ")");
                }
            }
        });

        Button viewButton = new Button("View Transcript");
        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        selectionBox.getChildren().addAll(selectLabel, studentSearchField, studentCombo, viewButton);

        // Transcript content area
        VBox transcriptContent = new VBox(15);
        transcriptContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label noDataLabel = new Label("Please select a student to view their transcript.");
        noDataLabel.setFont(Font.font("System", 14));
        noDataLabel.setTextFill(Color.web("#7f8c8d"));
        transcriptContent.getChildren().add(noDataLabel);

        // View button action
        viewButton.setOnAction(e -> {
            User selected = studentCombo.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a student!");
                return;
            }

            transcriptContent.getChildren().clear();

            // Get transcript
            List<model.TranscriptEntry> transcript = adminController.getStudentTranscript(selected.getId());

            if (transcript.isEmpty()) {
                Label emptyLabel = new Label("No completed courses found for this student.");
                emptyLabel.setFont(Font.font("System", 14));
                emptyLabel.setTextFill(Color.web("#e74c3c"));
                transcriptContent.getChildren().add(emptyLabel);
                return;
            }

            Student student = (Student) selected;

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
            addInfoRow(infoGrid, 3, "Department:", student.getDepartment() != null ? student.getDepartment() : "Not Assigned");
            addInfoRow(infoGrid, 4, "Faculty:", student.getFaculty() != null ? student.getFaculty() : "Not Assigned");

            studentInfo.getChildren().addAll(nameStatusBox, infoTitle, infoGrid);

            // ========== CUMULATIVE SUMMARY ==========
            int completedCredits = adminController.getCompletedCredits(selected.getId());
            double cumulativeGPA = adminController.calculateStudentGPA(selected.getId());
            int completedCount = adminController.getCompletedCourseCount(selected.getId());
            int failedCourseCount = adminController.getTotalFailedCourseCount(selected.getId());
            int passedCourseCount = completedCount - failedCourseCount;

            HBox summaryBox = new HBox(15);
            summaryBox.setAlignment(Pos.CENTER);
            summaryBox.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-border-color: #e0e0e0; -fx-border-width: 1;");

            VBox cgpaBox = createStatBox("CGPA", String.format("%.2f / 4.00", cumulativeGPA), "#1e88e5");
            VBox creditsBox = createStatBox("Total Credits", String.valueOf(completedCredits), "#7b1fa2");
            VBox coursesBox = createStatBox("Courses Taken", String.valueOf(completedCount), "#00897b");
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
            java.util.Map<Integer, List<model.TranscriptEntry>> groupedBySemester = new java.util.TreeMap<>((a, b) -> {
                if (a == 0) return 1; // Electives last
                if (b == 0) return -1;
                return Integer.compare(a, b);
            });
            for (model.TranscriptEntry entry : transcript) {
                // Normalize: all elective courses (semester <= 0) go to semester 0
                int normalizedSemester = entry.getSemester() <= 0 ? 0 : entry.getSemester();
                groupedBySemester.computeIfAbsent(normalizedSemester, k -> new ArrayList<>()).add(entry);
            }

            // Display each semester
            for (java.util.Map.Entry<Integer, List<model.TranscriptEntry>> semEntry : groupedBySemester.entrySet()) {
                int sem = semEntry.getKey();

                VBox semesterBox = new VBox(5);
                semesterBox.setStyle("-fx-border-color: #bdbdbd; -fx-border-width: 1; -fx-padding: 15; -fx-background-color: white;");

                // Semester header with year and term info
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
                TableView<model.TranscriptEntry> table = new TableView<>();
                table.setStyle("-fx-background-color: white; -fx-table-cell-border-color: #e0e0e0;");
                table.setFixedCellSize(35);
                table.setPrefHeight(semEntry.getValue().size() * 35 + 35);

                TableColumn<model.TranscriptEntry, String> codeCol = new TableColumn<>("COURSE CODE");
                codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
                codeCol.setPrefWidth(120);
                codeCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

                TableColumn<model.TranscriptEntry, String> nameCol = new TableColumn<>("COURSE TITLE");
                nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
                nameCol.setPrefWidth(340);

                TableColumn<model.TranscriptEntry, Integer> creditsCol = new TableColumn<>("CREDITS");
                creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
                creditsCol.setPrefWidth(80);
                creditsCol.setStyle("-fx-alignment: CENTER;");

                TableColumn<model.TranscriptEntry, String> gradeCol = new TableColumn<>("GRADE");
                gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
                gradeCol.setPrefWidth(80);
                gradeCol.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

                TableColumn<model.TranscriptEntry, String> typeCol = new TableColumn<>("TYPE");
                typeCol.setCellValueFactory(cellData -> {
                    String display = cellData.getValue().getEntryTypeDisplay();
                    return new javafx.beans.property.SimpleStringProperty(display);
                });
                typeCol.setPrefWidth(110);
                typeCol.setStyle("-fx-alignment: CENTER;");

                table.getColumns().addAll(codeCol, nameCol, creditsCol, gradeCol, typeCol);
                table.setItems(FXCollections.observableArrayList(semEntry.getValue()));

                // Semester summary
                int semCredits = semEntry.getValue().stream().mapToInt(model.TranscriptEntry::getCredits).sum();
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
        });

        ScrollPane scrollPane = new ScrollPane(transcriptContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(title, selectionBox, scrollPane);
        mainLayout.setCenter(content);
    }

    // ========== CREDIT TRANSFER PAGE ==========
    private void showCreditTransfer() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));

        Label title = new Label("🔄 Credit Transfer Management");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#2c3e50"));

        // Student selection with search
        VBox selectionBox = new VBox(10);
        selectionBox.setStyle("-fx-background-color: white; -fx-padding: 15; -fx-background-radius: 10;");

        Label selectLabel = new Label("Select Student:");
        selectLabel.setFont(Font.font("System", FontWeight.BOLD, 14));

        TextField studentSearchField = new TextField();
        studentSearchField.setPromptText("🔍 Type to search by name or number...");
        studentSearchField.setPrefWidth(400);
        studentSearchField.setStyle("-fx-font-size: 14px;");

        ComboBox<User> studentCombo = new ComboBox<>();
        studentCombo.setPrefWidth(400);
        studentCombo.setPromptText("Choose a student...");
        studentCombo.setVisibleRowCount(10);

        ObservableList<User> allStudents = FXCollections.observableArrayList(adminController.getAllStudents());
        studentCombo.setItems(allStudents);

        // Search filter with auto-select first
        studentSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.isEmpty()) {
                studentCombo.setItems(allStudents);
            } else {
                String search = newVal.toUpperCase();
                ObservableList<User> filtered = allStudents.filtered(u -> {
                    Student s = (Student) u;
                    return s.getFullName().toUpperCase().contains(search) ||
                           s.getStudentNumber().toUpperCase().contains(search);
                });
                studentCombo.setItems(filtered);
                // Auto-select first item after filtering
                if (!filtered.isEmpty()) {
                    studentCombo.getSelectionModel().selectFirst();
                }
            }
        });

        studentCombo.setCellFactory(lv -> new ListCell<User>() {
            @Override
            protected void updateItem(User student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText(null);
                } else {
                    Student s = (Student) student;
                    setText(s.getFullName() + " (" + s.getStudentNumber() + ") - " + s.getYearSemesterDisplay());
                }
            }
        });

        studentCombo.setButtonCell(new ListCell<User>() {
            @Override
            protected void updateItem(User student, boolean empty) {
                super.updateItem(student, empty);
                if (empty || student == null) {
                    setText("Choose a student...");
                } else {
                    Student s = (Student) student;
                    setText(s.getFullName() + " (" + s.getStudentNumber() + ")");
                }
            }
        });

        Button viewButton = new Button("View & Edit");
        viewButton.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 20;");

        HBox buttonBox = new HBox(10, studentCombo, viewButton);
        buttonBox.setAlignment(Pos.CENTER_LEFT);

        selectionBox.getChildren().addAll(selectLabel, studentSearchField, buttonBox);

        // Transfer content area
        VBox transferContent = new VBox(15);
        transferContent.setStyle("-fx-background-color: white; -fx-background-radius: 10; -fx-padding: 20;");

        Label noDataLabel = new Label("Please select a student to manage their credit transfers.");
        noDataLabel.setFont(Font.font("System", 14));
        noDataLabel.setTextFill(Color.web("#7f8c8d"));
        transferContent.getChildren().add(noDataLabel);

        // View button action
        viewButton.setOnAction(e -> {
            User selected = studentCombo.getValue();
            if (selected == null) {
                showAlert(Alert.AlertType.WARNING, "Warning", "Please select a student!");
                return;
            }

            transferContent.getChildren().clear();

            // Student info
            Student student = (Student) selected;
            VBox headerBox = new VBox(5);
            headerBox.setStyle("-fx-background-color: #e8f5e9; -fx-padding: 15; -fx-background-radius: 5;");

            Label nameLabel = new Label("👤 " + student.getFullName() + " (" + student.getStudentNumber() + ")");
            nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

            double gpa = adminController.calculateStudentGPA(selected.getId());
            int completedCredits = adminController.getCompletedCredits(selected.getId());

            Label statsLabel = new Label("🎓 GPA: " + String.format("%.2f", gpa) + " | 📊 " + completedCredits + " credits");
            statsLabel.setFont(Font.font("System", 13));
            statsLabel.setTextFill(Color.web("#27ae60"));

            headerBox.getChildren().addAll(nameLabel, statsLabel);

            // Add new course section
            VBox addSection = new VBox(10);
            addSection.setStyle("-fx-border-color: #3498db; -fx-border-width: 2; -fx-border-radius: 5; -fx-padding: 15;");

            Label addTitle = new Label("➕ Add Transferred Course");
            addTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            addTitle.setTextFill(Color.web("#2c3e50"));

            GridPane addGrid = new GridPane();
            addGrid.setHgap(10);
            addGrid.setVgap(10);

            // Course search field
            Label searchLabel = new Label("🔍 Search:");
            TextField courseSearchField = new TextField();
            courseSearchField.setPromptText("Type to search by code or name...");
            courseSearchField.setPrefWidth(400);
            courseSearchField.setStyle("-fx-font-size: 14px;");

            Label courseLabel = new Label("📖 Course:");
            ComboBox<CourseCatalog> courseCombo = new ComboBox<>();
            courseCombo.setPrefWidth(400);
            courseCombo.setPromptText("Select a course...");
            courseCombo.setVisibleRowCount(10);

            ObservableList<CourseCatalog> allCatalogCourses = FXCollections.observableArrayList(adminController.getAllCatalogCourses());
            courseCombo.setItems(allCatalogCourses);

            // Info label
            Label courseInfoLabel = new Label("💡 Type in the box above to search, then select from the list below");
            courseInfoLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-style: italic;");

            // Course search filter with auto-select first
            courseSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal == null || newVal.isEmpty()) {
                    courseCombo.setItems(allCatalogCourses);
                } else {
                    String search = newVal.toUpperCase();
                    ObservableList<CourseCatalog> filtered = allCatalogCourses.filtered(c ->
                        c.getCode().toUpperCase().contains(search) ||
                        c.getName().toUpperCase().contains(search)
                    );
                    courseCombo.setItems(filtered);
                    // Auto-select first item after filtering
                    if (!filtered.isEmpty()) {
                        courseCombo.getSelectionModel().selectFirst();
                    }
                }
            });

            // Update info label when selection changes
            courseCombo.setOnAction(event -> {
                CourseCatalog selectedCourse = courseCombo.getValue();
                if (selectedCourse != null) {
                    courseInfoLabel.setText("✅ " + selectedCourse.getCode() + " | " + selectedCourse.getCredits() + " Credits | " + selectedCourse.getSemesterDisplay());
                    courseInfoLabel.setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            });

            courseCombo.setCellFactory(lv -> new ListCell<CourseCatalog>() {
                @Override
                protected void updateItem(CourseCatalog course, boolean empty) {
                    super.updateItem(course, empty);
                    if (empty || course == null) {
                        setText(null);
                    } else {
                        String semInfo = course.getSemester() == 0 ? "Elective" : "Sem " + course.getSemester();
                        setText(course.getCode() + " - " + course.getName() + " (" + course.getCredits() + " cr, " + semInfo + ")");
                    }
                }
            });

            courseCombo.setButtonCell(new ListCell<CourseCatalog>() {
                @Override
                protected void updateItem(CourseCatalog course, boolean empty) {
                    super.updateItem(course, empty);
                    if (empty || course == null) {
                        setText("Select a course...");
                    } else {
                        setText(course.getCode() + " - " + course.getName());
                    }
                }
            });

            Label gradeLabel = new Label("Grade:");
            ComboBox<String> gradeCombo = new ComboBox<>();
            gradeCombo.getItems().addAll("AA", "BA", "BB", "CB", "CC", "DC", "DD", "FD", "FF");
            gradeCombo.setValue("CC");
            gradeCombo.setPrefWidth(100);

            Label typeLabel = new Label("Type:");
            ComboBox<String> typeCombo = new ComboBox<>();
            typeCombo.getItems().addAll("TRANSFER", "SUMMER_SCHOOL", "ERASMUS", "SYSTEM_ERROR");
            typeCombo.setValue("TRANSFER");
            typeCombo.setPrefWidth(150);

            Button addButton = new Button("Add Course");
            addButton.setStyle("-fx-background-color: #27ae60; -fx-text-fill: white; -fx-font-weight: bold;");

            addGrid.add(searchLabel, 0, 0);
            addGrid.add(courseSearchField, 1, 0);
            addGrid.add(courseLabel, 0, 1);
            addGrid.add(courseCombo, 1, 1);
            addGrid.add(courseInfoLabel, 1, 2);
            addGrid.add(gradeLabel, 0, 3);
            addGrid.add(gradeCombo, 1, 3);
            addGrid.add(typeLabel, 0, 4);
            addGrid.add(typeCombo, 1, 4);
            addGrid.add(addButton, 1, 5);

            addSection.getChildren().addAll(addTitle, addGrid);

            // Current transcript table
            VBox transcriptSection = new VBox(10);
            transcriptSection.setStyle("-fx-border-color: #95a5a6; -fx-border-width: 1; -fx-border-radius: 5; -fx-padding: 15;");

            Label transcriptTitle = new Label("📜 Current Transcript (Editable)");
            transcriptTitle.setFont(Font.font("System", FontWeight.BOLD, 14));
            transcriptTitle.setTextFill(Color.web("#2c3e50"));

            TableView<model.TranscriptEntry> transcriptTable = new TableView<>();
            transcriptTable.setPrefHeight(300);

            TableColumn<model.TranscriptEntry, String> codeCol = new TableColumn<>("Code");
            codeCol.setCellValueFactory(new PropertyValueFactory<>("courseCode"));
            codeCol.setPrefWidth(90);

            TableColumn<model.TranscriptEntry, String> nameCol = new TableColumn<>("Course Name");
            nameCol.setCellValueFactory(new PropertyValueFactory<>("courseName"));
            nameCol.setPrefWidth(200);

            TableColumn<model.TranscriptEntry, String> semesterCol = new TableColumn<>("Semester");
            semesterCol.setCellValueFactory(cellData -> {
                int sem = cellData.getValue().getSemester();
                String display = sem == 0 ? "Elective" : "Sem " + sem;
                return new javafx.beans.property.SimpleStringProperty(display);
            });
            semesterCol.setPrefWidth(80);
            semesterCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<model.TranscriptEntry, Integer> creditsCol = new TableColumn<>("Credits");
            creditsCol.setCellValueFactory(new PropertyValueFactory<>("credits"));
            creditsCol.setPrefWidth(60);
            creditsCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<model.TranscriptEntry, String> gradeCol = new TableColumn<>("Grade");
            gradeCol.setCellValueFactory(new PropertyValueFactory<>("grade"));
            gradeCol.setPrefWidth(60);
            gradeCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<model.TranscriptEntry, String> typeCol = new TableColumn<>("Type");
            typeCol.setCellValueFactory(cellData -> {
                String display = cellData.getValue().getEntryTypeDisplay();
                return new javafx.beans.property.SimpleStringProperty(display);
            });
            typeCol.setPrefWidth(100);
            typeCol.setStyle("-fx-alignment: CENTER;");

            TableColumn<model.TranscriptEntry, Void> actionCol = new TableColumn<>("Actions");
            actionCol.setPrefWidth(150);
            actionCol.setCellFactory(param -> new TableCell<>() {
                private final Button editBtn = new Button("Edit");
                private final Button deleteBtn = new Button("Delete");

                {
                    editBtn.setStyle("-fx-background-color: #f39c12; -fx-text-fill: white; -fx-font-size: 10;");
                    deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; -fx-font-size: 10;");

                    editBtn.setOnAction(event -> {
                        model.TranscriptEntry entry = getTableView().getItems().get(getIndex());
                        showEditCourseDialog(student, entry, transcriptTable, headerBox);
                    });

                    deleteBtn.setOnAction(event -> {
                        model.TranscriptEntry entry = getTableView().getItems().get(getIndex());
                        if (showConfirmDialog("Delete Course", "Are you sure you want to delete this course from transcript?")) {
                            int courseId = getCourseIdByCode(entry.getCourseCode());
                            if (courseId > 0 && adminController.deleteCompletedCourse(student.getId(), courseId)) {
                                refreshTranscriptTable(student.getId(), transcriptTable);
                                updateStudentHeader(student, headerBox);
                                showAlert(Alert.AlertType.INFORMATION, "Success", "Course deleted!");
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
                        HBox buttons = new HBox(5, editBtn, deleteBtn);
                        setGraphic(buttons);
                    }
                }
            });

            transcriptTable.getColumns().addAll(codeCol, nameCol, semesterCol, creditsCol, gradeCol, typeCol, actionCol);

            // Load transcript
            refreshTranscriptTable(student.getId(), transcriptTable);

            transcriptSection.getChildren().addAll(transcriptTitle, transcriptTable);

            // Add button action
            addButton.setOnAction(ev -> {
                CourseCatalog course = courseCombo.getValue();
                String grade = gradeCombo.getValue();
                String type = typeCombo.getValue();

                if (course == null) {
                    showAlert(Alert.AlertType.WARNING, "Warning", "Please select a course!");
                    return;
                }

                if (adminController.addCompletedCourseWithGrade(student.getId(), course.getCode(), grade, type)) {
                    refreshTranscriptTable(student.getId(), transcriptTable);
                    updateStudentHeader(student, headerBox);
                    courseCombo.setValue(null);
                    courseSearchField.clear();
                    gradeCombo.setValue("CC");
                    typeCombo.setValue("TRANSFER");
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course added to transcript!");
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Course already exists or couldn't be added!");
                }
            });

            transferContent.getChildren().addAll(headerBox, addSection, transcriptSection);
        });

        ScrollPane scrollPane = new ScrollPane(transferContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(title, selectionBox, scrollPane);
        mainLayout.setCenter(content);
    }

    private void refreshTranscriptTable(int studentId, TableView<model.TranscriptEntry> table) {
        List<model.TranscriptEntry> transcript = adminController.getStudentTranscript(studentId);
        table.setItems(FXCollections.observableArrayList(transcript));
        table.refresh(); // Force table refresh
    }

    private void updateStudentHeader(Student student, VBox headerBox) {
        headerBox.getChildren().clear();

        Label nameLabel = new Label("👤 " + student.getFullName() + " (" + student.getStudentNumber() + ")");
        nameLabel.setFont(Font.font("System", FontWeight.BOLD, 16));

        double gpa = adminController.calculateStudentGPA(student.getId());
        int completedCredits = adminController.getCompletedCredits(student.getId());

        Label statsLabel = new Label("🎓 GPA: " + String.format("%.2f", gpa) + " | 📊 " + completedCredits + " credits");
        statsLabel.setFont(Font.font("System", 13));
        statsLabel.setTextFill(Color.web("#27ae60"));

        headerBox.getChildren().addAll(nameLabel, statsLabel);
    }

    private void showEditCourseDialog(Student student, model.TranscriptEntry entry, TableView<model.TranscriptEntry> table, VBox headerBox) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Course");
        dialog.setHeaderText("Edit: " + entry.getCourseCode() + " - " + entry.getCourseName());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20));

        Label gradeLabel = new Label("Grade:");
        ComboBox<String> gradeCombo = new ComboBox<>();
        gradeCombo.getItems().addAll("AA", "BA", "BB", "CB", "CC", "DC", "DD", "FD", "FF");
        gradeCombo.setValue(entry.getGrade());

        Label typeLabel = new Label("Type:");
        ComboBox<String> typeCombo = new ComboBox<>();
        typeCombo.getItems().addAll("REGULAR", "TRANSFER", "SUMMER_SCHOOL", "ERASMUS", "SYSTEM_ERROR");
        typeCombo.setValue(entry.getEntryType());

        grid.add(gradeLabel, 0, 0);
        grid.add(gradeCombo, 1, 0);
        grid.add(typeLabel, 0, 1);
        grid.add(typeCombo, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                int courseId = getCourseIdByCode(entry.getCourseCode());
                if (courseId > 0 && adminController.updateCompletedCourse(student.getId(), courseId, gradeCombo.getValue(), typeCombo.getValue())) {
                    refreshTranscriptTable(student.getId(), table);
                    updateStudentHeader(student, headerBox);
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Course updated!");
                }
            }
        });
    }

    private int getCourseIdByCode(String code) {
        List<CourseCatalog> catalogs = adminController.getAllCatalogCourses();
        for (CourseCatalog catalog : catalogs) {
            if (catalog.getCode().equals(code)) {
                return catalog.getId();
            }
        }
        return -1;
    }

    // Helper method to add info rows to transcript
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

    // Helper method to create stat boxes for transcript summary
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

    // Calculate semester GPA
    private double calculateSemesterGPA(List<model.TranscriptEntry> courses) {
        double totalPoints = 0.0;
        int totalCredits = 0;

        for (model.TranscriptEntry entry : courses) {
            // GPA hesaplamasında TÜM dersler dahil edilir (başarısız dersler dahil)
            totalPoints += entry.getGradePoint() * entry.getCredits();
            totalCredits += entry.getCredits();
        }

        return totalCredits > 0 ? totalPoints / totalCredits : 0.0;
    }
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) return false;
        // Standart e-posta formatı kontrolü (regex)
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}

