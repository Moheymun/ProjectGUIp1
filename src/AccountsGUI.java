import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import javax.swing.*;

public class AccountsGUI extends JFrame implements ActionListener {
	private AccountManager manager;
	private JobManager jobManager;
	private StudentDashboardPanel studentDashboard;

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

    private JPanel loginPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    private JPanel registrationPanel = new JPanel(new GridLayout(7, 2, 10, 10));
    private JPanel adminPanel = new JPanel(new BorderLayout());

    // Creates the window and its panels.
    public AccountsGUI(AccountManager manager, JobManager jobManager) {
        this.manager = manager;
        this.jobManager = jobManager;

        setTitle("GPU Accounts and Registration");
        setSize(950, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Login panel.
        loginPanel.add(new JLabel("University ID / admin"));
        loginPanel.add(username);

        loginPanel.add(new JLabel("Password"));
        loginPanel.add(loginPassword);

        loginPanel.add(button("Login"));
        loginPanel.add(button("Register"));

        // Registration panel.
        registrationPanel.add(new JLabel("University ID"));
        registrationPanel.add(id);

        registrationPanel.add(new JLabel("Name"));
        registrationPanel.add(name);

        registrationPanel.add(new JLabel("Email"));
        registrationPanel.add(email);

        registrationPanel.add(new JLabel("Phone"));
        registrationPanel.add(phone);

        registrationPanel.add(new JLabel("Password"));
        registrationPanel.add(password);

        registrationPanel.add(new JLabel("Confirm password"));
        registrationPanel.add(confirm);

        registrationPanel.add(button("Submit"));
        registrationPanel.add(button("Back"));

        // Administrator panel.
        requests.setEditable(false);
        adminPanel.add(new JScrollPane(requests), BorderLayout.CENTER);

        JPanel controls = new JPanel();

        controls.add(new JLabel("Request ID:"));
        controls.add(requestId);

        controls.add(new JLabel("Initial minutes:"));
        controls.add(minutes);

        controls.add(button("Approve"));
        controls.add(button("Reject"));
        controls.add(button("Refresh"));
        controls.add(button("Logout"));

        adminPanel.add(controls, BorderLayout.SOUTH);

        showPanel(loginPanel);
    }

    // Creates a button and connects it to actionPerformed().
    private JButton button(String text) {
        JButton button = new JButton(text);
        button.addActionListener(this);
        return button;
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

            if (jobManager.findAllowance(student.getUsername()) == null) {
                jobManager.addStudentAllowance(student.getTimeAllowance());
            }

            studentDashboard = new StudentDashboardPanel(student.getUsername(), jobManager);

            JPanel panel = new JPanel(new BorderLayout());

            panel.add(studentDashboard, BorderLayout.CENTER);
            panel.add(button("Logout"), BorderLayout.SOUTH);

            showPanel(panel);
        }
    }

    // Handles button clicks and calls the account manager.
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
                // End the session and return to login.
                manager.logout();
                username.setText("");
                showPanel(loginPanel);
            }

        } catch (NumberFormatException e) {
            // Handle an invalid number of minutes.
            message("Minutes must be a whole number within the int range.");

        } catch (AccountException e) {
            // Display account validation or permission errors.
            message(e.getMessage());

        } catch (IOException e) {
            // Display file-saving errors.
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
            try {
            	AccountManager manager = new AccountManager("accounts.txt");
            	JobManager jobManager = new JobManager();
            	AccountsGUI window = new AccountsGUI(manager, jobManager);
                window.setVisible(true);

            } catch (Exception e) {
                // Report errors that prevent the application from starting.
                JOptionPane.showMessageDialog(null, "Cannot start: " + e.getMessage());
            }
        }
    }
}