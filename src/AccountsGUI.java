import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class AccountsGUI extends JFrame implements ActionListener {
    private AccountManager manager;
    private JobManager jobManager;
    private StudentDashboardPanel studentDashboard;

    // Shared GUI colors.
    private final Color background = new Color(244, 247, 251);
    private final Color navy = new Color(30, 49, 75);
    private final Color muted = new Color(105, 119, 137);
    private final Color accent = new Color(62, 133, 158);
    private final Color outline = new Color(220, 228, 236);
    private final Color fieldBackground = new Color(250, 252, 254);

    // Login fields.
    private JTextField username = new JTextField();
    private JPasswordField loginPassword = new JPasswordField();

    // Registration fields.
    private JTextField id = new JTextField();
    private JTextField name = new JTextField();
    private JTextField email = new JTextField();
    private JTextField phone = new JTextField();
    private JPasswordField password = new JPasswordField();
    private JPasswordField confirm = new JPasswordField();

    // Administrator approval fields.
    private JTextField requestId = new JTextField(12);
    private JTextField minutes = new JTextField("100", 5);
    private JTextArea requests = new JTextArea();

    private JPanel loginPanel;
    private JPanel registrationPanel;
    private JPanel adminPanel;

    // Creates the window and its panels.
    public AccountsGUI(AccountManager manager, JobManager jobManager) {
        this.manager = manager;
        this.jobManager = jobManager;

        setTitle("GPU Accounts and Registration");
        setSize(1100, 760);
        setMinimumSize(new Dimension(900, 680));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        loginPanel = createLoginPanel();
        registrationPanel = createRegistrationPanel();
        adminPanel = createAdminPanel();

        showPanel(loginPanel);
    }

    // Creates the shared page background, header, and footer.
    private JPanel createPage(String title, String subtitle) {
        JPanel page = new JPanel(new BorderLayout(0, 22));
        page.setBackground(background);
        page.setBorder(new EmptyBorder(24, 28, 16, 28));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 27));
        titleLabel.setForeground(navy);

        header.add(titleLabel);
        header.add(Box.createVerticalStrut(6));
        header.add(description(subtitle));

        JLabel footer = description("GPU Resource Management | University Computing Services");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));

        page.add(header, BorderLayout.NORTH);
        page.add(footer, BorderLayout.SOUTH);

        return page;
    }

    // Creates a white card with padding and a subtle border.
    private JPanel createCard() {
        JPanel card = new JPanel(new BorderLayout(0, 20));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(outline), new EmptyBorder(24, 24, 24, 24)));

        return card;
    }

    // Centers a form without stretching its fields.
    private JPanel centerCard(JPanel card) {
        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.add(card);

        return wrapper;
    }

    private JLabel heading(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 20));
        label.setForeground(navy);

        return label;
    }

    private JLabel description(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.PLAIN, 12));
        label.setForeground(muted);

        return label;
    }

    // Creates a title and description for a card.
    private JPanel introduction(String title, String text) {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        panel.add(heading(title));
        panel.add(Box.createVerticalStrut(8));
        panel.add(description(text));

        return panel;
    }

    // Removes the square field background so the wrapper can draw rounded corners.
    private void styleField(JTextField field) {
        field.setFont(new Font("SansSerif", Font.PLAIN, 14));
        field.setForeground(navy);
        field.setBackground(fieldBackground);
        field.setCaretColor(accent);
        field.setOpaque(false);
        field.setBorder(new EmptyBorder(10, 12, 10, 12));
    }

    // Adds a label and rounded background to an existing input field.
    private JPanel labeledField(String title, JTextField field) {
        JPanel panel = new JPanel(new BorderLayout(0, 7));
        panel.setOpaque(false);

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));
        label.setForeground(navy);
        label.setLabelFor(field);

        styleField(field);

        JPanel roundedBox = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int arc = 18;

                g2.setColor(fieldBackground);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                // Highlight the field currently being edited.
                g2.setColor(field.hasFocus() ? accent : outline);
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);

                g2.dispose();
            }
        };

        roundedBox.setOpaque(false);
        roundedBox.setBorder(new EmptyBorder(1, 1, 1, 1));
        roundedBox.add(field, BorderLayout.CENTER);

        // Update the outline when focus changes.
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent event) {
                roundedBox.repaint();
            }

            @Override
            public void focusLost(FocusEvent event) {
                roundedBox.repaint();
            }
        });

        panel.add(label, BorderLayout.NORTH);
        panel.add(roundedBox, BorderLayout.CENTER);

        return panel;
    }

    // Creates a styled button and connects it to actionPerformed().
    private JButton button(String text) {
        JButton button = new JButton(text);
        button.setActionCommand(text);
        button.addActionListener(this);

        boolean secondary = text.equals("Register") || text.equals("Back") || text.equals("Refresh") || text.equals("Logout");
        Color normal;

        if (text.equals("Approve")) {
            normal = new Color(76, 137, 91);
        } else if (text.equals("Reject")) {
            normal = new Color(180, 72, 72);
        } else if (secondary) {
            normal = new Color(232, 238, 244);
        } else {
            normal = accent;
        }

        button.setFont(new Font("SansSerif", Font.BOLD, 13));
        button.setBackground(normal);
        button.setForeground(secondary ? navy : Color.WHITE);
        button.setOpaque(true);
        button.setContentAreaFilled(true);
        button.setBorderPainted(false);
        button.setBorder(new EmptyBorder(11, 18, 11, 18));
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Darken the button slightly when the pointer is over it.
        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                button.setBackground(normal.darker());
            }

            @Override
            public void mouseExited(MouseEvent event) {
                button.setBackground(normal);
            }
        });

        return button;
    }

    // Builds the login form using the original fields.
    private JPanel createLoginPanel() {
        JPanel page = createPage("GPU Account Portal", "Sign in to access your university GPU account");

        JPanel card = createCard();
        card.setPreferredSize(new Dimension(440, 370));
        card.add(introduction("Welcome back", "Enter your account details to continue."), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(2, 1, 0, 18));
        fields.setOpaque(false);
        fields.add(labeledField("University ID / admin", username));
        fields.add(labeledField("Password", loginPassword));

        JPanel buttons = new JPanel(new GridLayout(1, 2, 12, 0));
        buttons.setOpaque(false);
        buttons.add(button("Login"));
        buttons.add(button("Register"));

        card.add(fields, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        // Pressing Enter in the password field triggers Login.
        loginPassword.setActionCommand("Login");
        loginPassword.addActionListener(this);

        page.add(centerCard(card), BorderLayout.CENTER);

        return page;
    }

    // Arranges the registration fields in two columns.
    private JPanel createRegistrationPanel() {
        JPanel page = createPage("Create an Account", "Register for access to university GPU resources");

        JPanel card = createCard();
        card.setPreferredSize(new Dimension(680, 450));
        card.add(introduction("Student details", "Your registration will be reviewed by an administrator."), BorderLayout.NORTH);

        JPanel fields = new JPanel(new GridLayout(3, 2, 20, 18));
        fields.setOpaque(false);
        fields.add(labeledField("University ID", id));
        fields.add(labeledField("Full name", name));
        fields.add(labeledField("Email address", email));
        fields.add(labeledField("Phone number", phone));
        fields.add(labeledField("Password", password));
        fields.add(labeledField("Confirm password", confirm));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(button("Back"));
        buttons.add(button("Submit"));

        card.add(fields, BorderLayout.CENTER);
        card.add(buttons, BorderLayout.SOUTH);

        page.add(centerCard(card), BorderLayout.CENTER);

        return page;
    }

    // Builds the request list and administrator approval controls.
    private JPanel createAdminPanel() {
        JPanel page = createPage("Account Management", "Administrator | Review student registrations");

        JPanel content = new JPanel(new BorderLayout(0, 18));
        content.setOpaque(false);

        JPanel requestsCard = createCard();

        JPanel requestsHeader = new JPanel(new BorderLayout());
        requestsHeader.setOpaque(false);
        requestsHeader.add(heading("Registration requests"), BorderLayout.WEST);
        requestsHeader.add(button("Refresh"), BorderLayout.EAST);

        requests.setEditable(false);
        requests.setFont(new Font("Monospaced", Font.PLAIN, 13));
        requests.setForeground(navy);
        requests.setBackground(fieldBackground);
        requests.setMargin(new Insets(14, 14, 14, 14));

        JScrollPane scrollPane = new JScrollPane(requests);
        scrollPane.setBorder(BorderFactory.createLineBorder(outline));

        requestsCard.add(requestsHeader, BorderLayout.NORTH);
        requestsCard.add(scrollPane, BorderLayout.CENTER);

        JPanel approvalCard = createCard();
        approvalCard.add(heading("Review a request"), BorderLayout.NORTH);

        JPanel inputs = new JPanel(new GridLayout(1, 2, 18, 0));
        inputs.setOpaque(false);
        inputs.add(labeledField("Request ID", requestId));
        inputs.add(labeledField("Initial minutes", minutes));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttons.setOpaque(false);
        buttons.add(button("Approve"));
        buttons.add(button("Reject"));
        buttons.add(button("Logout"));

        approvalCard.add(inputs, BorderLayout.CENTER);
        approvalCard.add(buttons, BorderLayout.SOUTH);

        content.add(requestsCard, BorderLayout.CENTER);
        content.add(approvalCard, BorderLayout.SOUTH);

        page.add(content, BorderLayout.CENTER);

        return page;
    }

    // Changes the displayed panel and refreshes the window.
    private void showPanel(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    // Displays a message in a dialog box.
    private void message(String text) {
        JOptionPane.showMessageDialog(this, text);
    }

    // Displays the appropriate panel after login.
    private void showAccount() throws AccountException {
        User user = manager.getCurrentUser();

        if (user instanceof Administrator) {
            requests.setText(manager.getRequestSummary());
            showPanel(adminPanel);
        } else {
            Student student = (Student) user;

            // Keep an existing allowance instead of resetting it on login.
            if (jobManager.findAllowance(student.getUsername()) == null) {
                jobManager.addStudentAllowance(student.getTimeAllowance());
            }

            // Use the existing student dashboard and shared job manager.
            studentDashboard = new StudentDashboardPanel(student.getUsername(), jobManager);

            JPanel page = createPage("Student Dashboard", "Manage your GPU jobs and time allowance");

            JPanel content = new JPanel(new BorderLayout(0, 12));
            content.setOpaque(false);
            content.add(studentDashboard, BorderLayout.CENTER);

            JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            controls.setOpaque(false);
            controls.add(button("Logout"));

            content.add(controls, BorderLayout.SOUTH);
            page.add(content, BorderLayout.CENTER);

            showPanel(page);
        }
    }

    // Handles button clicks using the original account manager calls.
    @Override
    public void actionPerformed(ActionEvent event) {
        String command = event.getActionCommand();

        try {
            if (command.equals("Login")) {
                String entered = new String(loginPassword.getPassword());
                loginPassword.setText("");

                manager.login(username.getText().trim(), entered);
                showAccount();

            } else if (command.equals("Register")) {
                showPanel(registrationPanel);

            } else if (command.equals("Submit")) {
                String chosen = new String(password.getPassword());
                String confirmed = new String(confirm.getPassword());

                // Reject registration if the passwords differ.
                if (!chosen.equals(confirmed)) {
                    throw new AccountException("Passwords do not match.");
                }

                manager.register(id.getText().trim(), name.getText().trim(), email.getText().trim(), phone.getText().trim(), chosen);

                // Clear the form after successful registration.
                id.setText("");
                name.setText("");
                email.setText("");
                phone.setText("");
                password.setText("");
                confirm.setText("");

                message("Registration saved. Wait for approval.");
                showPanel(loginPanel);

            } else if (command.equals("Back")) {
                password.setText("");
                confirm.setText("");
                showPanel(loginPanel);

            } else if (command.equals("Approve")) {
                int allowance = Integer.parseInt(minutes.getText().trim());

                manager.approve(requestId.getText().trim(), allowance);
                requests.setText(manager.getRequestSummary());

                message("Student approved.");

            } else if (command.equals("Reject")) {
                manager.reject(requestId.getText().trim());
                requests.setText(manager.getRequestSummary());

                message("Request rejected.");

            } else if (command.equals("Refresh")) {
                requests.setText(manager.getRequestSummary());

            } else if (command.equals("Logout")) {
                // End the session without resetting the job manager.
                manager.logout();
                username.setText("");
                showPanel(loginPanel);
            }

        } catch (NumberFormatException e) {
            message("Minutes must be a whole number within the int range.");

        } catch (AccountException e) {
            message(e.getMessage());

        } catch (IOException e) {
            message("File error: " + e.getMessage());
        }
    }

    // Starts the GUI on Swing's event thread.
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new StartGUI());
    }

    // Loads account data and opens the window.
    private static class StartGUI implements Runnable {
        @Override
        public void run() {
            // Apply a consistent Swing appearance.
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {
                // Keep the existing appearance if unavailable.
            }

            try {
                AccountManager manager = new AccountManager("accounts.txt");
                JobManager jobManager = new JobManager();
                AccountsGUI window = new AccountsGUI(manager, jobManager);
                window.setVisible(true);

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, "Cannot start: " + e.getMessage());
            }
        }
    }
}