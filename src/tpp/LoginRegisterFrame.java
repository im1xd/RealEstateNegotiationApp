package tpp;

import javax.swing.*;
import java.awt.*;
// other imports remain the same
import java.sql.Connection;
import java.sql.SQLException;

public class LoginRegisterFrame extends JFrame {
    private UserService userService;
    private Connection connection;

    // Components remain the same...
    private JTextField regUsernameField, regFullNameField, regPhoneField, regEmailField;
    private JPasswordField regPasswordField;
    private JTextField loginUsernameField;
    private JPasswordField loginPasswordField;

    public LoginRegisterFrame(Connection connection) {
        if (connection == null) {
            throw new IllegalArgumentException("Connection cannot be null");
        }
        this.connection = connection;
        // Ensure userService is initialized correctly
        this.userService = new UserService(connection);

        setTitle("تسجيل الدخول / إنشاء حساب");
        setSize(500, 350); // Adjusted size slightly
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initComponents();
    }

    private void initComponents() {
        // initComponents content remains largely the same...
        JTabbedPane tabs = new JTabbedPane();

        // لوحة تسجيل الدخول
        JPanel loginPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Use GridLayout for better alignment
        loginPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        loginUsernameField = new JTextField();
        loginPasswordField = new JPasswordField();
        JButton loginButton = new JButton("تسجيل الدخول");

        loginButton.addActionListener(e -> handleLogin()); // Use lambda

        loginPanel.add(new JLabel("اسم المستخدم:"));
        loginPanel.add(loginUsernameField);
        loginPanel.add(new JLabel("كلمة السر:"));
        loginPanel.add(loginPasswordField);
        loginPanel.add(new JLabel()); // Placeholder
        loginPanel.add(loginButton);

        // لوحة إنشاء الحساب
        JPanel registerPanel = new JPanel(new GridLayout(0, 2, 10, 10)); // Use GridLayout
        registerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Add padding
        regUsernameField = new JTextField();
        regFullNameField = new JTextField();
        regPhoneField = new JTextField();
        regEmailField = new JTextField();
        regPasswordField = new JPasswordField();
        JButton registerButton = new JButton("إنشاء حساب");

        registerButton.addActionListener(e -> handleRegister()); // Use lambda

        registerPanel.add(new JLabel("اسم المستخدم:"));
        registerPanel.add(regUsernameField);
        registerPanel.add(new JLabel("الاسم الكامل:"));
        registerPanel.add(regFullNameField);
        registerPanel.add(new JLabel("رقم الهاتف:"));
        registerPanel.add(regPhoneField);
        registerPanel.add(new JLabel("البريد الإلكتروني:"));
        registerPanel.add(regEmailField);
        registerPanel.add(new JLabel("كلمة السر:"));
        registerPanel.add(regPasswordField);
        registerPanel.add(new JLabel("حدد الدور:")); // Add role selection for registration
        JComboBox<String> roleComboBox = new JComboBox<>(new String[]{"client", "owner"});
        registerPanel.add(roleComboBox);
        registerPanel.add(new JLabel()); // Placeholder
        registerPanel.add(registerButton);

        // Add action listener for registration button to use the selected role
        registerButton.addActionListener(e -> handleRegister((String) roleComboBox.getSelectedItem()));


        tabs.addTab("تسجيل الدخول", loginPanel); // Use addTab
        tabs.addTab("إنشاء حساب", registerPanel); // Use addTab

        // Add tabs to the frame's content pane
        getContentPane().add(tabs);
    }


    // *** MODIFIED handleLogin ***
    private void handleLogin() {
        String username = loginUsernameField.getText().trim();
        String password = new String(loginPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال اسم المستخدم وكلمة المرور.", "خطأ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Make sure userService is not null
            if (userService == null) {
                 JOptionPane.showMessageDialog(this, "خطأ داخلي: خدمة المستخدم غير مهيأة.", "خطأ فني", JOptionPane.ERROR_MESSAGE);
                 return;
            }

            User user = userService.login(username, password);

            if (user != null) {
                UserSession.setCurrentUser(user); // Store the current user globally

                JOptionPane.showMessageDialog(this,
                    "✅ تم تسجيل الدخول بنجاح! مرحباً " + user.getUsername(),
                    "نجاح",
                    JOptionPane.INFORMATION_MESSAGE);

                // Close the login window
                this.dispose();

                // Open the appropriate frame based on the user's role
                String role = user.getRole().toLowerCase(); // Ensure lowercase comparison
                switch (role) {
                    case "client":
                        // Open Client Interface (HouseFrame)
                        HouseFrame clientFrame = new HouseFrame(connection);
                        clientFrame.setVisible(true);
                        break;
                    case "owner":
                        // Open Owner Interface (OwnerDashboardFrame)
                        OwnerDashboardFrame ownerFrame = new OwnerDashboardFrame(connection, user.getId());
                        ownerFrame.setVisible(true);
                        break;
                    default:
                        JOptionPane.showMessageDialog(null, // Use null parent since 'this' is disposed
                                "دور المستخدم '" + user.getRole() + "' غير معروف أو غير مدعوم.",
                                "خطأ في الدور", JOptionPane.ERROR_MESSAGE);
                        // Optionally re-open login frame or exit
                         new LoginRegisterFrame(connection).setVisible(true);
                        break;
                }

            } else {
                JOptionPane.showMessageDialog(this,
                        "❌ اسم المستخدم أو كلمة المرور غير صحيحة.",
                        "فشل تسجيل الدخول", JOptionPane.ERROR_MESSAGE);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ خطأ في قاعدة البيانات أثناء تسجيل الدخول:\n" + ex.getMessage(),
                    "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "❌ حدث خطأ غير متوقع:\n" + ex.getMessage(),
                    "خطأ", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    // *** MODIFIED handleRegister to accept role ***
     private void handleRegister(String selectedRole) {
        String username = regUsernameField.getText().trim();
        String fullName = regFullNameField.getText().trim();
        String phone = regPhoneField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = new String(regPasswordField.getPassword());

        if (username.isEmpty() || password.isEmpty() || fullName.isEmpty()) { // Added fullName check
            JOptionPane.showMessageDialog(this, "يرجى ملء حقول اسم المستخدم، الاسم الكامل، وكلمة المرور.", "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
            return;
        }
        if (selectedRole == null || selectedRole.trim().isEmpty()){
             JOptionPane.showMessageDialog(this, "الرجاء اختيار الدور (زبون أو مالك).", "بيانات ناقصة", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
             if (userService == null) {
                 JOptionPane.showMessageDialog(this, "خطأ داخلي: خدمة المستخدم غير مهيأة.", "خطأ فني", JOptionPane.ERROR_MESSAGE);
                 return;
            }
            // Create user with the selected role
            User newUser = new User(0, username, password, selectedRole); // Pass selected role
            newUser.setFullName(fullName);
            newUser.setPhone(phone); // Phone is optional, handle if empty if needed
            newUser.setEmail(email); // Email is optional

            userService.registerUser(newUser);
            JOptionPane.showMessageDialog(this,
                "✅ تم إنشاء الحساب بنجاح كـ '" + selectedRole + "'.\n يمكنك الآن تسجيل الدخول.",
                "نجاح التسجيل",
                JOptionPane.INFORMATION_MESSAGE);

            // Clear fields after registration
            regUsernameField.setText("");
            regFullNameField.setText("");
            regPhoneField.setText("");
            regEmailField.setText("");
            regPasswordField.setText("");
             // Optionally switch to the login tab
            ((JTabbedPane)getContentPane().getComponent(0)).setSelectedIndex(0);


        } catch (SQLException ex) {
             JOptionPane.showMessageDialog(this, "❌ خطأ في قاعدة البيانات أثناء التسجيل:\n" + ex.getMessage(), "خطأ قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
             // Check for duplicate username error (MySQL error code 1062)
             if (ex.getErrorCode() == 1062) {
                 JOptionPane.showMessageDialog(this, "❌ اسم المستخدم '" + username + "' موجود بالفعل. الرجاء اختيار اسم آخر.", "خطأ", JOptionPane.ERROR_MESSAGE);
             }
             ex.printStackTrace();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "❌ حدث خطأ غير متوقع أثناء التسجيل:\n" + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }
     // Overload handleRegister to be called without role (e.g., from old button listener if any)
     // This is just for safety, the primary call should be handleRegister(String role)
      private void handleRegister() {
          // Find the JComboBox within the registerPanel to get the role
          // This is less robust than passing it directly
           Component[] components = ((JPanel)((JTabbedPane)getContentPane().getComponent(0)).getComponentAt(1)).getComponents();
            String selectedRole = "client"; // Default
            for(Component comp : components) {
                if (comp instanceof JComboBox) {
                    selectedRole = (String)((JComboBox<?>) comp).getSelectedItem();
                    break;
                }
            }
          handleRegister(selectedRole);
      }


    // Other methods (like showDatabaseError) can remain or be added if needed
    private void showDatabaseError(SQLException ex) {
        JOptionPane.showMessageDialog(this,
            "❌ خطأ في قاعدة البيانات:\n" + ex.getMessage(),
            "خطأ في الاتصال",
            JOptionPane.ERROR_MESSAGE);
    }

    // Remove the old main method if it exists in this class
    /*
    public static void main(String[] args) {
        // This main method should not be used directly anymore
        // Use MainApp.java instead
    }
    */
}