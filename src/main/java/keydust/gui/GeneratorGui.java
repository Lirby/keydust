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

import java.util.function.Consumer;

public final class GeneratorGui extends Stage {

    public GeneratorGui(Stage owner, Consumer<String> onPasswordGenerated) {
        initOwner(owner);
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Password Generator");

        TextField lengthField = new TextField("12");

        CheckBox upperBox = new CheckBox("Uppercase (A-Z)");
        CheckBox lowerBox = new CheckBox("Lowercase (a-z)");
        CheckBox numberBox = new CheckBox("Numbers (0-9)");
        CheckBox specialBox = new CheckBox("Special characters");

        upperBox.setSelected(true);
        lowerBox.setSelected(true);
        numberBox.setSelected(true);
        specialBox.setSelected(true);

        TextField genPwdField = new TextField();
        genPwdField.setEditable(false);

        javafx.scene.control.Label errorLabel = new Label();
        errorLabel.getStyleClass().add("danger-text");

        Button generateBtn = new Button("Generate");
        Button useBtn = new Button("Use Password");
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
                new Label("Password length:"),
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

        setScene(new Scene(root, 380, 420));
    }
}
