package tpp;

import javax.swing.SwingUtilities;

import jade.core.Agent;

public class OwnerAgentConsoleApp extends Agent {
    public static void main(String[] args) {
        try {
            Database db = new Database();
            db.connect();

            NegotiationService service = new NegotiationService(db.getConnection());

            int houseId = 1; // أو اسحب المعرف الحقيقي من قاعدة البيانات حسب المالك

            SwingUtilities.invokeLater(() -> {
                OwnerReviewFrame frame = new OwnerReviewFrame(service, houseId);
                frame.setVisible(true);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
