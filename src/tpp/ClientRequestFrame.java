package tpp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.List;

public class ClientRequestFrame extends JFrame {
    private JTextField usernameField, passwordField;
    private JTextArea housesArea;
    private JComboBox<Integer> houseIdBox;
    private JTextField priceField, durationField;
    private int clientId;
    private Connection connection;
    private UserService userService;
    private HouseService houseService;
    private NegotiationService negotiationService;

    public ClientRequestFrame(NegotiationService negotiationService, int clientId) {
    	 this.negotiationService = negotiationService;
    	 this.clientId = clientId;
    	setTitle("نظام الزبون");
        setSize(600, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/house_db", "root", "");
            userService = new UserService(connection);
            houseService = new HouseService(connection);
            negotiationService = new NegotiationService(connection);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
            System.exit(1);
        }

        // Panel الدخول
        JPanel loginPanel = new JPanel(new GridLayout(0, 2));
        usernameField = new JTextField();
        passwordField = new JTextField();
        JButton loginButton = new JButton("تسجيل الدخول");

        loginPanel.add(new JLabel("اسم المستخدم:"));
        loginPanel.add(usernameField);
        loginPanel.add(new JLabel("كلمة السر:"));
        loginPanel.add(passwordField);
        loginPanel.add(new JLabel());
        loginPanel.add(loginButton);

        add(loginPanel, BorderLayout.NORTH);

        // منطقة عرض المنازل
        housesArea = new JTextArea();
        housesArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(housesArea);
        add(scrollPane, BorderLayout.CENTER);

        // Panel التفاوض
        JPanel negotiationPanel = new JPanel(new GridLayout(0, 2));
        houseIdBox = new JComboBox<>();
        priceField = new JTextField();
        durationField = new JTextField();
        JButton negotiateButton = new JButton("بدء التفاوض");

        negotiationPanel.add(new JLabel("ID المنزل:"));
        negotiationPanel.add(houseIdBox);
        negotiationPanel.add(new JLabel("السعر المقترح:"));
        negotiationPanel.add(priceField);
        negotiationPanel.add(new JLabel("المدة المطلوبة:"));
        negotiationPanel.add(durationField);
        negotiationPanel.add(new JLabel());
        negotiationPanel.add(negotiateButton);

        add(negotiationPanel, BorderLayout.SOUTH);

        // أحداث الأزرار
        loginButton.addActionListener(e -> handleLogin());
        negotiateButton.addActionListener(e -> handleNegotiation());

        setVisible(true);
    }

    private void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            User user = userService.login(username, password);
            if (user != null && user.getRole().equals("client")) {
                clientId = user.getId();
                JOptionPane.showMessageDialog(this, "✅ تم تسجيل الدخول!");
                displayHouses();
            } else {
                JOptionPane.showMessageDialog(this, "❌ فشل في تسجيل الدخول. تأكد من المعلومات.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "❌ خطأ في قاعدة البيانات: " + e.getMessage());
        }
    }

    private void displayHouses() throws SQLException {
        List<House> houses = houseService.getAllAvailableHouses();
        housesArea.setText("");
        houseIdBox.removeAllItems();

        for (House h : houses) {
            housesArea.append("ID: " + h.getId() + "\nالموقع: " + h.getLocation()
                    + "\nالسعر: " + h.getPrice()
                    + "\nمن: " + h.getMinDuration() + " إلى: " + h.getMaxDuration() + "\n-----------------------\n");
            houseIdBox.addItem(h.getId());
        }
    }

    private void handleNegotiation() {
        try {
            int houseId = (int) houseIdBox.getSelectedItem();
            double proposedPrice = Double.parseDouble(priceField.getText());
            int proposedDuration = Integer.parseInt(durationField.getText());

            Negotiation negotiation = new Negotiation(clientId, houseId, proposedPrice, proposedDuration);
            negotiationService.startNegotiation(negotiation);

            JOptionPane.showMessageDialog(this, "✅ تم إرسال طلب التفاوض!");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "❌ خطأ: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/house_db", "root", "");
            NegotiationService negotiationService = new NegotiationService(conn);
            int clientId = 0; // أو أي ID تختبر به

            SwingUtilities.invokeLater(() -> new ClientRequestFrame(negotiationService, clientId));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
