package keydust.gui;

import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import keydust.contollers.PwdGeneratorController;
import keydust.gui.core.Builder;

import java.util.function.Consumer;

public final class GeneratorGui extends Stage {

    private final Consumer<String> onPasswordGenerated;

    public GeneratorGui(Stage owner, Consumer<String> onPasswordGenerated) {
        this.onPasswordGenerated = onPasswordGenerated;

        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Password Generator");
        setScene(buildScene());
        setResizable(false);
    }

    private Scene buildScene() {
        Label title = new Label("Password length:");
        title.getStyleClass().add("muted");

        TextField lengthField = new TextField("12");
        lengthField.getStyleClass().add("input-field");

        CheckBox upperBox = new CheckBox("Uppercase (A-Z)");
        upperBox.getStyleClass().add("check-box");

        CheckBox lowerBox = new CheckBox("Lowercase (a-z)");
        lowerBox.getStyleClass().add("check-box");

        CheckBox numberBox = new CheckBox("Numbers (0-9)");
        numberBox.getStyleClass().add("check-box");

        CheckBox specialBox = new CheckBox("Special characters");
        specialBox.getStyleClass().add("check-box");

        upperBox.setSelected(true);
        lowerBox.setSelected(true);
        numberBox.setSelected(true);
        specialBox.setSelected(true);

        TextField genPwdField = new TextField();
        genPwdField.setEditable(false);
        genPwdField.getStyleClass().add("input-field");

        javafx.scene.control.Label errorLabel = new Label();
        errorLabel.getStyleClass().add("danger-text");

        Button generateBtn = new Button("Generate");
        generateBtn.getStyleClass().add("primary");

        Button useBtn = new Button("Use Password");
        useBtn.getStyleClass().add("primary");

        Button cancelBtn = new Button("Cancel");

        generateBtn.setOnAction(e -> {
            try {
                int length = Integer.parseInt(lengthField.getText());

                String password = PwdGeneratorController.generatePassword(
                        length,
                        upperBox.isSelected(),
                        lowerBox.isSelected(),
                        numberBox.isSelected(),
                        specialBox.isSelected()
                );

                genPwdField.setText(password);
                errorLabel.setText("");
            } catch (Exception e1) {
                errorLabel.setText(e1.getMessage());
            }
        });

        useBtn.setOnAction(e -> {
            String pwd = genPwdField.getText();
            if(!pwd.isEmpty()) {
                onPasswordGenerated.accept(pwd);
                close();
            }
        });

        cancelBtn.setOnAction(e -> close());

        HBox buttons = new HBox(10, generateBtn, useBtn, cancelBtn);

        VBox root = new VBox(
                10,
                title,
                lengthField,
                upperBox,
                lowerBox,
                numberBox,
                specialBox,
                genPwdField,
                buttons,
                errorLabel
        );

        root.setPadding(new Insets(15));

        return Builder.createScene(root, 380, 420);
    }
}
