package tpp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;

public class CustomerConsoleApp extends JFrame {

    private JTextField nameField, priceField, durationField;
    private JTable houseTable;
    private DefaultTableModel tableModel;
    private Connection connection;
    private int customerId;

    public CustomerConsoleApp() {
        setTitle("تسجيل الزبون والتفاوض");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            connection = Database.getConnection();
        } catch (SQLException e) {
            showError("فشل الاتصال بقاعدة البيانات: " + e.getMessage());
            return;
        }

        // Panel للتسجيل
        JPanel registerPanel = new JPanel(new FlowLayout());
        registerPanel.add(new JLabel("اسم الزبون:"));
        nameField = new JTextField(10);
        registerPanel.add(nameField);
        JButton registerButton = new JButton("تسجيل");
        registerPanel.add(registerButton);

        // جدول عرض المنازل
        tableModel = new DefaultTableModel(new String[]{"ID", "الموقع", "السعر", "مدة الكراء"}, 0);
        houseTable = new JTable(tableModel);
        JScrollPane tableScroll = new JScrollPane(houseTable);

        // Panel للتفاوض
        JPanel negotiationPanel = new JPanel(new GridLayout(3, 2));
        negotiationPanel.setBorder(BorderFactory.createTitledBorder("التفاوض"));
        negotiationPanel.add(new JLabel("السعر المقترح:"));
        priceField = new JTextField();
        negotiationPanel.add(priceField);
        negotiationPanel.add(new JLabel("مدة الكراء (بالأيام):"));
        durationField = new JTextField();
        negotiationPanel.add(durationField);
        JButton negotiateButton = new JButton("بدء التفاوض");
        negotiationPanel.add(negotiateButton);

        add(registerPanel, BorderLayout.NORTH);
        add(tableScroll, BorderLayout.CENTER);
        add(negotiationPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> registerCustomer());
        negotiateButton.addActionListener(e -> startNegotiation());

        loadHouses();
    }

    private void registerCustomer() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showError("يرجى إدخال اسمك.");
            return;
        }

        try {
            CustomerService customerService = new CustomerService(connection);
            customerId = customerService.registerCustomer(name);
            JOptionPane.showMessageDialog(this, "✅ تم تسجيلك بنجاح! رقمك: " + customerId);
        } catch (SQLException e) {
            showError("فشل التسجيل: " + e.getMessage());
        }
    }

    private void loadHouses() {
        try {
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM houses");

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                    rs.getInt("id"),
                    rs.getString("location"),
                    rs.getDouble("price"),
                    rs.getInt("min_duration") + " - " + rs.getInt("max_duration")
                });
            }
        } catch (SQLException e) {
            showError("خطأ في تحميل المنازل: " + e.getMessage());
        }
    }

    private void startNegotiation() {
        int selectedRow = houseTable.getSelectedRow();
        if (selectedRow == -1) {
            showError("يرجى اختيار منزل.");
            return;
        }

        if (customerId == 0) {
            showError("يرجى التسجيل أولاً.");
            return;
        }

        try {
            int houseId = (int) tableModel.getValueAt(selectedRow, 0);
            double price = Double.parseDouble(priceField.getText().trim());
            int duration = Integer.parseInt(durationField.getText().trim());

            NegotiationService negotiationService = new NegotiationService(connection);
            Negotiation negotiation = new Negotiation(customerId, houseId, price, duration);
            negotiationService.startNegotiation(negotiation);

            JOptionPane.showMessageDialog(this, "✅ تم إرسال طلب التفاوض.");
        } catch (NumberFormatException ex) {
            showError("يرجى إدخال أرقام صحيحة للسعر والمدة.");
        } catch (SQLException e) {
            showError("فشل في التفاوض: " + e.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "خطأ", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new CustomerConsoleApp().setVisible(true));
    }
}
