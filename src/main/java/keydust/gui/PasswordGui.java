package keydust.gui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.collections.transformation.SortedList;
import keydust.contollers.ShowPwdController;
import keydust.db.SqliteDB;
import keydust.gui.core.Builder;

import java.sql.SQLException;

public class PasswordGui extends Stage {

    private final String encryptionPassword;
    private final SqliteDB sqlite;

    private final TextField searchField = new TextField();
    private final TableView<CredentialRow> tableView = new TableView<>();
    private final ObservableList<CredentialRow> rows = FXCollections.observableArrayList();
    private final FilteredList<CredentialRow> filteredRows = new FilteredList<>(rows, r -> true);
    private final SortedList<CredentialRow> sortedRows = new SortedList<>(filteredRows);
    private final Label feedback = new Label();
    private CredentialRow visibleRow = null;

    public PasswordGui(String password, SqliteDB sqlite) {
        this.encryptionPassword = password;
        this.sqlite = sqlite;

        setTitle("KeyDust v1.0");
        setScene(buildScene());
        setMinWidth(760);
        setMinHeight(520);

        refreshTable();
    }

    private Scene buildScene() {
        Label title = new Label("Welcome to KeyDust");
        title.getStyleClass().add("heading");

        searchField.setPromptText("Search");
        searchField.getStyleClass().add("input-field");
        searchField.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case ESCAPE -> searchField.clear();
            }
        });

        TableColumn<CredentialRow, String> descriptionCol = new TableColumn<>("Description");
        descriptionCol.setCellValueFactory(data -> data.getValue().descriptionProperty());
        TableColumn<CredentialRow, String> usernameCol = new TableColumn<>("Username");
        usernameCol.setCellValueFactory(data -> data.getValue().usernameProperty());
        TableColumn<CredentialRow, String> passwordCol = new TableColumn<>("Password");
        passwordCol.setCellValueFactory(data -> data.getValue().passwordProperty());
        passwordCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String pwd, boolean empty) {
                super.updateItem(pwd, empty);

                if (empty || pwd == null) {
                    setText(null);
                    return;
                }
                CredentialRow row = getTableView().getItems().get(getIndex());
                setText(row == visibleRow ? pwd :"************");
            }
        });

        tableView.getColumns().addAll(descriptionCol, usernameCol, passwordCol);

        sortedRows.comparatorProperty().bind(tableView.comparatorProperty());
        tableView.setItems(sortedRows);
        setupSearch();

        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tableView.setPlaceholder(new Label("No Password yet. Add one to get started."));



        Button addPwd = new Button("Add Password");
        addPwd.getStyleClass().add("primary");
        addPwd.setOnAction(e -> openAddDialog());

        Button editPwd = new Button("Edit Password");
        editPwd.setOnAction(e -> openEditDialog());

        Button copyBtn = new Button("Copy Password");
        copyBtn.getStyleClass().add("primary");
        copyBtn.setOnAction(e -> copyPwd() );

        Button showPwd = new Button("Show Password");
        showPwd.getStyleClass().add("primary");
        showPwd.setDisable(true);
        showPwd.setOnAction(e -> showSelectedPassword(showPwd));

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            showPwd.setDisable(newSel == null);
            copyBtn.setDisable(newSel == null);

            if (visibleRow != null && visibleRow != newSel) {
                hidePassword(showPwd);
            }
        });

        HBox leftBtn = new HBox(10, editPwd, addPwd);
        leftBtn.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox rightBtn = new HBox(10, copyBtn, showPwd);
        rightBtn.setAlignment(Pos.CENTER_RIGHT);

        HBox actions = new HBox(10, leftBtn, spacer, rightBtn);
        actions.setAlignment(Pos.CENTER);

        feedback.getStyleClass().add("muted");

        VBox header = new VBox(4, title);
        header.setAlignment(Pos.BOTTOM_CENTER);

        VBox content = new VBox(14,
                header,
                searchField,
                tableView,
                actions,
                feedback
        );
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(6, 4, 6, 4));
        content.setPrefWidth(880);

        return Builder.createScene(content, 900, 580);

    }

    private void openAddDialog() {
        AddPasswordGui dialog = new AddPasswordGui(this, encryptionPassword, sqlite, this::refreshTable);
        dialog.show();
    }

    private void openEditDialog() {
        CredentialRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Select a credential to edit.", "danger-text");
            return;
        }

        EditPasswordGui dialog = new EditPasswordGui(
                this,
                encryptionPassword,
                sqlite,
                selected.getId(),
                selected.getDescription(),
                selected.getUsername(),
                selected.getPassword(),
                this::refreshTable
        );
        dialog.show();
        showFeedback("", "muted");
    }

    private void copyPwd() {
        CredentialRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showFeedback("Select a credential to copy.", "danger-text");
            return;
        }

        String rowPwd = selected.getPassword();
        if (rowPwd == null) rowPwd = "";

        Clipboard clipboard = Clipboard.getSystemClipboard();
        ClipboardContent content = new ClipboardContent();
        content.putString(rowPwd);
        clipboard.setContent(content);
    }

    private void showSelectedPassword(Button showPwd) {
        CredentialRow selected = tableView.getSelectionModel().getSelectedItem();
        if (selected == null) {
            hidePassword(showPwd);
            return;
        }

        boolean isVisible = (visibleRow == selected);
        if (isVisible) {
            hidePassword(showPwd);
        } else {
            showPwdForRow(selected, showPwd);
        }
    }

    private void showPwdForRow(CredentialRow row, Button showPwd) {
        visibleRow = row;
        showPwd.setText("Hide Password");
        tableView.refresh();
    }

    private void hidePassword(Button showPwd) {
        visibleRow = null;
        showPwd.setText("Show Password");
        tableView.refresh();
    }

    public void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            final String filter = (newVal == null) ? "" : newVal.trim().toLowerCase();

            filteredRows.setPredicate(r -> {
                if (filter.isEmpty()) return true;

                String desc = safeLower(r.getDescription());
                String user = safeLower(r.getUsername());

                return desc.contains(filter) || user.contains(filter);
            });
        });
    }

    private static String safeLower(String s) {
        return s == null ? "" : s.toLowerCase();
    }

    public void refreshTable() {
        ShowPwdController controller = new ShowPwdController(encryptionPassword, sqlite);
        try {
            String[][] data = controller.loadPasswords();
            rows.clear();
            for (String[] entry : data) {
                if (entry.length >= 4) {
                    rows.add(new CredentialRow(entry[0], entry[1], entry[2], entry[3]));
                }
            }
            showFeedback("", "muted");
        } catch (SQLException e) {
            showFeedback("Could not reload passwords: " + e.getMessage(), "danger-text");
        }
    }

    private void showFeedback(String message, String styleClass) {
        feedback.getStyleClass().removeAll("danger-text", "success-text", "muted");
        feedback.getStyleClass().add(styleClass);
        feedback.setText(message);
    }



    private static class CredentialRow {
        private final StringProperty id;
        private final StringProperty description;
        private final StringProperty username;
        private final StringProperty password;

        CredentialRow(String id, String description, String username, String password) {
            this.id = new SimpleStringProperty(id);
            this.description = new SimpleStringProperty(description);
            this.username = new SimpleStringProperty(username);
            this.password = new SimpleStringProperty(password);
        }

        public int getId() {
            return Integer.parseInt(id.get());
        }

        public String getDescription() {
            return description.get();
        }

        public StringProperty descriptionProperty() {
            return description;
        }

        public String getUsername() {
            return username.get();
        }

        public StringProperty usernameProperty() {
            return username;
        }

        public String getPassword() {
            return password.get();
        }

        public StringProperty passwordProperty() {
            return password;
        }
    }
}
