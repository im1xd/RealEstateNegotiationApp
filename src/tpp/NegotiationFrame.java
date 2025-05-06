package tpp;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

public class NegotiationFrame extends JFrame {
    private NegotiationService negotiationService;
    private int houseId;
    private int clientId;
    private int currentAttempt = 1;
    private int maxAttempts;

    private JLabel priceLabel = new JLabel("السعر المقترح:");
    private JTextField priceField = new JTextField(10);
    private JLabel durationLabel = new JLabel("المدة المقترحة (شهور):");
    private JTextField durationField = new JTextField(10);
    private JButton submitButton = new JButton("تقديم العرض");
    private JTextArea resultArea = new JTextArea(10, 30);
    private JLabel attemptsLabel = new JLabel("المحاولة: 1/");

    public NegotiationFrame(NegotiationService negotiationService, int houseId, int clientId) {
        this.negotiationService = negotiationService;
        this.houseId = houseId;
        this.clientId = clientId;
        setTitle("نافذة التفاوض - منزل ID: " + houseId);
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
initUI();
        loadMaxAttempts();
        
            setTitle("تقديم عرض جديد");
            setSize(400, 200);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            initUI();
        }

        
  

    private void initUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));new JPanel(new GridLayout(3, 2, 10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // لوحة الإدخال
        JPanel inputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        inputPanel.add(priceLabel);
        mainPanel.add(new JLabel("السعر المقترح:"));
        inputPanel.add(priceField);
        inputPanel.add(durationLabel);
        mainPanel.add(new JLabel("المدة المقترحة (أشهر):"));
        inputPanel.add(durationField);
        inputPanel.add(new JLabel(""));
        inputPanel.add(submitButton);
        submitButton.addActionListener(e -> submitOffer());

        add(mainPanel);
    
        // منطقة النتائج
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        // لوحة المحاولات
        JPanel attemptsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        attemptsPanel.add(attemptsLabel);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(attemptsPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(new SubmitAction());

        add(mainPanel);
    }
    private void submitOffer() {
        try {
            double price = Double.parseDouble(priceField.getText());
            int duration = Integer.parseInt(durationField.getText());

            // إنشاء كائن Negotiation وحفظه
            Negotiation negotiation = new Negotiation(clientId, houseId, price, duration);
            negotiationService.startNegotiation(negotiation);

            JOptionPane.showMessageDialog(this, "✅ تم إرسال العرض بنجاح!");
            this.dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "❌ أدخل قيماً رقمية صحيحة!");
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "❌ خطأ في قاعدة البيانات: " + ex.getMessage());
        }
    }
    private void loadMaxAttempts() {
        try {
            maxAttempts = negotiationService.getMaxNegotiationAttempts(houseId);
            attemptsLabel.setText("المحاولة: 1/" + maxAttempts);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "خطأ في جلب عدد المحاولات: " + e.getMessage());
            maxAttempts = 3; // قيمة افتراضية
        }
    }

    private class SubmitAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                double price = Double.parseDouble(priceField.getText());
                int duration = Integer.parseInt(durationField.getText());

                Negotiation negotiation = new Negotiation(clientId, houseId, price, duration);
                negotiation.setAttemptCount(currentAttempt);

                String result = processNegotiation(negotiation);
                resultArea.append(result + "\n");

                if (negotiation.getStatus().equals("accepted") || currentAttempt >= maxAttempts) {
                    submitButton.setEnabled(false);
                } else {
                    currentAttempt++;
                    attemptsLabel.setText("المحاولة: " + currentAttempt + "/" + maxAttempts);
                    priceField.setText("");
                    durationField.setText("");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(NegotiationFrame.this, 
                    "يجب إدخال قيم رقمية صحيحة", 
                    "خطأ في الإدخال", 
                    JOptionPane.ERROR_MESSAGE);
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(NegotiationFrame.this, 
                    "خطأ في قاعدة البيانات: " + ex.getMessage(), 
                    "خطأ", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String processNegotiation(Negotiation negotiation) throws SQLException {
        negotiationService.startNegotiation(negotiation);
        StringBuilder result = new StringBuilder();
        result.append("== المحاولة ").append(currentAttempt).append(" ==\n");
        result.append("السعر المقترح: ").append(negotiation.getProposedPrice()).append("\n");
        result.append("المدة المقترحة: ").append(negotiation.getProposedDuration()).append("\n");
        result.append("الحالة: ").append(negotiation.getStatus().equals("accepted") ? "مقبول" : "مرفوض").append("\n");
        
        return result.toString();
    }
}
