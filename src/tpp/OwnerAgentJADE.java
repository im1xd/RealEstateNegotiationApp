package tpp; // تأكد من الحزمة الصحيحة

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService; // For Directory Facilitator registration
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

import javax.swing.*; // For SwingUtilities
import java.sql.Connection; // To receive connection

public class OwnerAgentJADE extends Agent {

    private int ownerId;
    private Connection dbConnection;
    // private transient OwnerDashboardFrame ownerDashboard; // Reference to GUI

    @Override
    protected void setup() {
        System.out.println("👋 Hello! Owner agent " + getAID().getLocalName() + " starting...");

        // --- استقبال الوسائط ---
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Integer) {
                this.ownerId = (Integer) args[0];
                System.out.println("   Agent initialized for Owner ID: " + this.ownerId);
            }
            if (args.length > 1 && args[1] instanceof Connection) {
                this.dbConnection = (Connection) args[1];
                System.out.println("   Agent received database connection.");
            }
        } else {
             System.out.println("   Warning: No arguments passed to OwnerAgentJADE.");
        }

        // --- (اختياري ولكن مهم) تسجيل الخدمة في الـ DF ---
        // هذا يسمح لوكلاء العملاء بالعثور على هذا الوكيل
        // سنفترض أن هذا الوكيل يقدم خدمة "مراجعة-العروض"
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID()); // اسم الوكيل
        ServiceDescription sd = new ServiceDescription();
        sd.setType("house-ownership"); // نوع الخدمة (يمكن أن يكون أي شيء وصفي)
        sd.setName("owner-review-service-" + ownerId); // اسم فريد للخدمة (مرتبط بالمالك)
        dfd.addServices(sd);
        try {
            DFService.register(this, dfd); // تسجيل الوكيل في الـ DF
            System.out.println("   Agent " + getLocalName() + " registered service '" + sd.getName() + "' in DF.");
        } catch (FIPAException fe) {
            System.err.println("   Error registering agent in DF: " + fe.getMessage());
            fe.printStackTrace();
        }


        // --- السلوك الرئيسي: استقبال رسائل العروض (PROPOSE) ---
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // استقبل فقط رسائل PROPOSE المتعلقة بتفاوض المنازل
                MessageTemplate mt = MessageTemplate.and(
                        MessageTemplate.MatchPerformative(ACLMessage.PROPOSE),
                        MessageTemplate.MatchOntology("house-negotiation")
                );
                ACLMessage msg = myAgent.receive(mt);

                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + ": Received PROPOSE from " + msg.getSender().getLocalName());
                    String content = msg.getContent();
                    System.out.println("   Content: " + content);

                    // **TODO:** تحليل محتوى العرض (السعر، المدة، معرف المنزل)
                    // double proposedPrice = parsePrice(content);
                    // int proposedDuration = parseDuration(content);
                    // int houseId = parseHouseId(content); // تحتاج لطريقة لتضمين معرف المنزل في الرسالة

                    // **TODO:** تقييم العرض
                    // - التحقق من أن المنزل يخص هذا المالك (ownerId)
                    // - التحقق من قواعد المنزل (السعر الأدنى، المدة المسموحة) - قد يتطلب استعلام قاعدة بيانات
                    // boolean offerValid = checkOfferValidity(houseId, proposedPrice, proposedDuration);

                    // **TODO:** (إذا كان التقييم يتطلب قرار المالك)
                    // - إرسال إشعار لواجهة المستخدم الخاصة بالمالك (OwnerDashboardFrame / OwnerReviewFrame)
                    // - الانتظار للحصول على قرار المالك (ACCEPT/REJECT)

                    // **TODO:** (بعد الحصول على القرار) إرسال رد لوكيل الزبون
                    // ACLMessage reply = msg.createReply();
                    // if (decisionIsAccept) {
                    //     reply.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                    //     reply.setContent("Offer for house " + houseId + " accepted.");
                    //     // **TODO:** تحديث قاعدة البيانات (رفض العروض الأخرى، جعل المنزل غير متاح)
                    // } else {
                    //     reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    //     reply.setContent("Offer for house " + houseId + " rejected. Reason: ...");
                    // }
                    // myAgent.send(reply);
                    System.out.println("   **PLACEHOLDER:** Offer evaluation and response logic needed here.");

                } else {
                    // لا توجد رسائل مطابقة، انتظر
                    block();
                }
            }
        });

        // --- فتح واجهة المستخدم الخاصة بالمالك ---
        // ** هام: يجب تشغيل هذا الكود في EDT الخاص بـ Swing **
        /*
        SwingUtilities.invokeLater(() -> {
            System.out.println("OwnerAgent " + getLocalName() + " is opening owner GUI...");
            // OwnerDashboardFrame ownerGUI = new OwnerDashboardFrame(dbConnection, ownerId);
            // أو واجهة جديدة تتفاعل مع هذا الوكيل
            // OwnerAgentGUI ownerGUI = new OwnerAgentGUI(this, ownerId, dbConnection);
            // ownerGUI.setVisible(true);
        });
        */
         System.out.println("   OwnerAgent " + getLocalName() + " setup complete. Waiting for PROPOSE messages.");
    }

    @Override
    protected void takeDown() {
        // إلغاء تسجيل الخدمة من الـ DF
        try {
            DFService.deregister(this);
            System.out.println("   Agent " + getLocalName() + " deregistered from DF.");
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
        System.out.println("💤 Owner agent " + getAID().getName() + " terminating.");
    }

    // --- طرق مساعدة (أمثلة) ---
    // private double parsePrice(String content) { /* ... */ return 0.0; }
    // private int parseDuration(String content) { /* ... */ return 0; }
    // private int parseHouseId(String content) { /* ... */ return 0; }
    // private boolean checkOfferValidity(int houseId, double price, int duration) { /* ... */ return false; }

}