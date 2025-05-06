package tpp;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.SQLException;
import java.util.List;

public class OwnerReviewFrame extends JFrame {
    private NegotiationService service;
    private int houseId;

    private JTable negotiationTable;
    private DefaultTableModel tableModel;

    public OwnerReviewFrame(NegotiationService service, int houseId) {
        if (service == null) {
            throw new IllegalArgumentException("NegotiationService لا يمكن أن يكون null");
        }

        this.service = service;
        this.houseId = houseId;

        setTitle("مراجعة العروض - صاحب المنزل");
        setSize(800, 400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initComponents();
        loadNegotiations();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        // جدول
        String[] columns = {"ID", "الزبون", "السعر", "المدة", "الحالة", "عدد المحاولات"};
        tableModel = new DefaultTableModel(columns, 0);
        negotiationTable = new JTable(tableModel);

        // زر للموافقة / الرفض
        JButton reviewButton = new JButton("✅ مراجعة العرض المحدد");
        reviewButton.addActionListener(e -> reviewSelectedNegotiation());

        add(new JScrollPane(negotiationTable), BorderLayout.CENTER);
        add(reviewButton, BorderLayout.SOUTH);
    }

    private void loadNegotiations() {
        try {
            tableModel.setRowCount(0);
            List<Negotiation> list = service.getAllNegotiationsForHouse(houseId);
            for (Negotiation n : list) {
                tableModel.addRow(new Object[]{
                    n.getId(), n.getClientId(), n.getProposedPrice(),
                    n.getProposedDuration(), n.getStatus(), n.getAttemptCount()
                });
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "فشل تحميل العروض: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reviewSelectedNegotiation() {
        if (service == null) {
            JOptionPane.showMessageDialog(this, "الخدمة غير متوفرة (service is null)", "خطأ برمجي", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int row = negotiationTable.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "يرجى اختيار عرض!", "خطأ", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int negotiationId = (int) tableModel.getValueAt(row, 0);
        String status = (String) tableModel.getValueAt(row, 4);
        if (!"PENDING".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "العرض ليس في وضع انتظار!", "تنبيه", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this, "هل تريد قبول العرض؟", "مراجعة العرض", JOptionPane.YES_NO_OPTION);
        try {
            if (confirm == JOptionPane.YES_OPTION) {
                service.updateNegotiationStatus(negotiationId, "ACCEPTED");
            } else {
                service.updateNegotiationStatus(negotiationId, "REJECTED");
            }
            loadNegotiations();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "خطأ أثناء تحديث الحالة: " + e.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    }
}
