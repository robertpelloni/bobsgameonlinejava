package com.bobsgame.client.engine.game.gui;

import com.bobsgame.client.engine.game.gui.MenuPanel;
import com.bobsgame.client.network.GameClientTCP;
import com.bobsgame.net.BobNet;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.EditField;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.SimpleBooleanModel;

public class LoginMenu extends MenuPanel {

    private Label userLabel;
    private EditField userField;
    private Label passLabel;
    private EditField passField;
    private ToggleButton stayLoggedInButton;
    private SimpleBooleanModel stayLoggedInModel;

    private Button loginButton;
    private Button createAccountButton;
    private Button forgotPasswordButton;
    private Button backButton;

    private Label statusLabel;

    public LoginMenu() {
        super();

        Label title = new Label("Login");

        userLabel = new Label("Username or Email:");
        userField = new EditField();

        passLabel = new Label("Password:");
        passField = new EditField();
        passField.setPasswordMasking(true);
        passField.setPasswordChar('*');

        stayLoggedInModel = new SimpleBooleanModel();
        stayLoggedInButton = new ToggleButton(stayLoggedInModel);
        stayLoggedInButton.setText("Stay Logged In");

        statusLabel = new Label("");

        loginButton = new Button("Login");
        loginButton.addCallback(new Runnable() {
            public void run() {
                doLogin();
            }
        });

        createAccountButton = new Button("Create Account");
        createAccountButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().openCreateAccountMenu();
            }
        });

        forgotPasswordButton = new Button("Forgot Password");
        forgotPasswordButton.addCallback(new Runnable() {
            public void run() {
                doForgot();
            }
        });

        backButton = new Button("Back");
        backButton.addCallback(new Runnable() {
            public void run() {
                GUIManager().closeLoginMenu();
            }
        });

        insideScrollPaneLayout.setHorizontalGroup(
            insideScrollPaneLayout.createParallelGroup()
                .addWidget(title)
                .addWidget(userLabel).addWidget(userField)
                .addWidget(passLabel).addWidget(passField)
                .addWidget(stayLoggedInButton)
                .addWidget(loginButton)
                .addWidget(createAccountButton)
                .addWidget(forgotPasswordButton)
                .addWidget(backButton)
                .addWidget(statusLabel)
        );

        insideScrollPaneLayout.setVerticalGroup(
            insideScrollPaneLayout.createSequentialGroup()
                .addWidget(title)
                .addWidget(userLabel).addWidget(userField)
                .addWidget(passLabel).addWidget(passField)
                .addWidget(stayLoggedInButton)
                .addWidget(loginButton)
                .addWidget(createAccountButton)
                .addWidget(forgotPasswordButton)
                .addWidget(backButton)
                .addWidget(statusLabel)
        );
    }

    private void doLogin() {
        String user = userField.getText();
        String pass = passField.getText();
        boolean stay = stayLoggedInModel.getValue();

        if(user.length() == 0 || pass.length() == 0) {
            statusLabel.setText("Please enter username and password.");
            return;
        }

        statusLabel.setText("Logging in...");

        // This relies on GameClientTCP.sendLoginRequest
        // But we need access to it.
        // MenuPanel doesn't expose Network() directly usually, but PlayerEditMenu used ClientMain.clientTCP
        // I should use a getter.
        if(Network() != null) {
            // Note: The sendLoginRequest signature in GameClientTCP is (email, password, stats)
            // But we might need to handle the stayLoggedIn flag locally or send it?
            // C++ version sends it. Java GameClientTCP currently: sendLoginRequest(String email, String password, boolean stats)
            // It does not seem to take stayLoggedIn.
            // Let's check GameClientTCP.java again.
            // It has `sendLoginRequest(String email, String password, boolean stats)`.
            // It does not seem to handle 'stay logged in' persistence on the client side in that method.
            // But maybe we can save the token if login succeeds.
            // For now, let's just call login.
            Network().sendLoginRequest(user, pass, true);
        }
    }

    private void doForgot() {
        String user = userField.getText();
        if(user.length() == 0) {
            statusLabel.setText("Please enter username/email first.");
            return;
        }
        statusLabel.setText("Sending recovery email...");
        if(Network() != null) {
            Network().sendPasswordRecoveryRequest(user);
        }
    }

    // We need to poll for success? Or GameClientTCP updates GameSave/State?
    // GameClientTCP.incomingLoginResponse updates GameSave and userID/sessionToken.
    // We can check `ClientMain.clientMain.network.getGotLoginResponse_S()` in update().

    @Override
    public void update() {
        super.update();
        if(Network() != null) {
            if(Network().getGotLoginResponse_S()) {
                if(Network().getWasLoginResponseValid_S()) {
                    statusLabel.setText("Login Successful!");
                    // Close menu after delay?
                    GUIManager().closeLoginMenu();
                } else {
                    statusLabel.setText("Login Failed.");
                    Network().setGotLoginResponse_S(false); // Reset for retry
                }
            }
        }
    }
}
