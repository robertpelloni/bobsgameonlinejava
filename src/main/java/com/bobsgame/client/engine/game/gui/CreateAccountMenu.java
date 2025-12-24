package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.gui.MenuPanel;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;

public class CreateAccountMenu extends MenuPanel {

    private Label userLabel;
    private EditField userField;
    private Label emailLabel;
    private EditField emailField;
    private Label passLabel;
    private EditField passField;
    private Label confirmLabel;
    private EditField confirmField;

    private Button createButton;
    private Button backButton;

    private Label statusLabel;

    public CreateAccountMenu() {
        super();

        Label title = new Label("Create Account");

        userLabel = new Label("Username:");
        userField = new EditField();

        emailLabel = new Label("Email:");
        emailField = new EditField();

        passLabel = new Label("Password:");
        passField = new EditField();
        passField.setPasswordMasking(true);
        passField.setPasswordChar('*');

        confirmLabel = new Label("Confirm:");
        confirmField = new EditField();
        confirmField.setPasswordMasking(true);
        confirmField.setPasswordChar('*');

        statusLabel = new Label("");

        createButton = new Button("Create");
        createButton.addCallback(new Runnable() {
            public void run() {
                doCreate();
            }
        });

        backButton = new Button("Back");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closeCreateAccountMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addWidget(userLabel).addWidget(userField)
                .addWidget(emailLabel).addWidget(emailField)
                .addWidget(passLabel).addWidget(passField)
                .addWidget(confirmLabel).addWidget(confirmField)
                .addWidget(createButton)
                .addWidget(backButton)
                .addWidget(statusLabel)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addWidget(userLabel).addWidget(userField)
                .addWidget(emailLabel).addWidget(emailField)
                .addWidget(passLabel).addWidget(passField)
                .addWidget(confirmLabel).addWidget(confirmField)
                .addWidget(createButton)
                .addWidget(backButton)
                .addWidget(statusLabel)
        );
    }

    private void doCreate() {
        String user = userField.getText();
        String email = emailField.getText();
        String pass = passField.getText();
        String conf = confirmField.getText();

        if(user.length() == 0 || email.length() == 0 || pass.length() == 0) {
            statusLabel.setText("All fields required.");
            return;
        }

        if(!pass.equals(conf)) {
            statusLabel.setText("Passwords do not match.");
            return;
        }

        statusLabel.setText("Creating account...");

        if(Network() != null) {
            Network().sendCreateAccountRequest(email, pass);
            // Note: C++ sends username too? Java GameClientTCP.sendCreateAccountRequest(email, password) only takes email/pass?
            // Let's verify GameClientTCP signature.
            // sendCreateAccountRequest(String email, String password) -> "CreateAccountRequest`email`,`password`"
            // It seems it relies on email being unique. Username might be set later or via PlayerEditMenu?
            // Or maybe the packet handling on server does something?
            // The C++ version sends username.
            // If the Java protocol is older/different, we stick to what `GameClientTCP` supports.
            // However, `PlayerEditMenu` sets `characterName`.
            // The user requested "backport...". If the C++ protocol supports username in CreateAccount, I might need to update GameClientTCP.
            // But I should avoid breaking the server if I don't control it.
            // I'll stick to `GameClientTCP`'s existing method.
        }
    }

    @Override
    public void update() {
        super.update();
        if(Network() != null) {
            if(Network().getGotCreateAccountResponse_S()) {
                statusLabel.setText("Account Created! Please Login.");
                Network().setGotCreateAccountResponse_S(false);
                // Delay?
            }
        }
    }
}
