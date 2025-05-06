package tpp;

import tpp.Negotiation;
import tpp.NegotiationService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class CustomerNegotiationFrame extends JFrame {
    private NegotiationService service;
    private int clientId;
    private int houseId;

    private JTextField priceField;
    private JTextField durationField;
    private JTable negotiationTable;
    private DefaultTableModel tableModel;

    public CustomerNegotiationFrame(NegotiationService service, int clientId, int houseId) {
        this.service = service;
        this.clientId = clientId;
        this.houseId = houseId;

        setTitle("واجهة الزبون - التفاوض");
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        loadNegotiations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // Panel لإدخال البيانات
        JPanel inputPanel = new JPanel(new GridLayout(2, 2));
        inputPanel.add(new JLabel("السعر المقترح:"));
        priceField = new JTextField();
        inputPanel.add(priceField);

        inputPanel.add(new JLabel("المدة (بالأيام):"));
        durationField = new JTextField();
        inputPanel.add(durationField);

        JButton sendButton = new JButton("📤 إرسال العرض");
        sendButton.addActionListener(e -> sendOffer());

        // جدول عرض المفاوضات
        String[] columns = {"رقم", "السعر", "المدة", "الحالة", "عدد المحاولات"};
        tableModel = new DefaultTableModel(columns, 0);
        negotiationTable = new JTable(tableModel);

        add(inputPanel, BorderLayout.NORTH);
        add(new JScrollPane(negotiationTable), BorderLayout.CENTER);
        add(sendButton, BorderLayout.SOUTH);
    }

    private void sendOffer() {
        try {
            double price = Double.parseDouble(priceField.getText());
            int duration = Integer.parseInt(durationField.getText());

            Negotiation negotiation = new Negotiation(clientId, houseId, price, duration);
            negotiation.setStatus("PENDING"); // مبدئياً انتظار
            negotiation.setAttemptCount(1);   // أول محاولة

            service.startNegotiation(negotiation);
            loadNegotiations();

            priceField.setText("");
            durationField.setText("");

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "يرجى إدخال قيم صحيحة!", "خطأ", JOptionPane.ERROR_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "فشل في إرسال العرض: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadNegotiations() {
        try {
            tableModel.setRowCount(0);
            List<Negotiation> list = service.getAllNegotiationsForClient(clientId, houseId);
            for (Negotiation n : list) {
                tableModel.addRow(new Object[]{
                    n.getId(), n.getProposedPrice(), n.getProposedDuration(),
                    n.getStatus(), n.getAttemptCount()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "فشل في تحميل المفاوضات: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
