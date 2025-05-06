package tpp; // تأكد من الحزمة الصحيحة

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController; // For potential interaction with other agents/GUI
import jade.gui.GuiAgent; // Alternative base class if complex GUI interaction needed later
import jade.gui.GuiEvent; // Companion to GuiAgent

import javax.swing.*; // For SwingUtilities
import java.sql.Connection; // To receive connection if passed as argument
import java.util.Map; // To receive data from GUI via O2A

public class ClientAgentJADE extends Agent { // Consider GuiAgent if tight GUI coupling needed

    private int clientId;
    private Connection dbConnection; // Store connection if passed
    // private transient NegotiationFrame negotiationFrame; // Reference to GUI (handle carefully for serialization)

    @Override
    protected void setup() {
        System.out.println("👋 Hello! Client agent " + getAID().getLocalName() + " starting...");

        // --- استقبال الوسائط (Arguments) ---
        Object[] args = getArguments();
        if (args != null && args.length > 0) {
            if (args[0] instanceof Integer) {
                this.clientId = (Integer) args[0];
                System.out.println("   Agent initialized for Client ID: " + this.clientId);
            }
            if (args.length > 1 && args[1] instanceof Connection) {
                this.dbConnection = (Connection) args[1];
                System.out.println("   Agent received database connection.");
            }
            // You might pass GUI references here too, but be cautious
        } else {
            System.out.println("   Warning: No arguments passed to ClientAgentJADE.");
            // Maybe terminate if arguments are essential?
            // doDelete();
            // return;
        }

        // --- تفعيل استقبال الكائنات من الواجهة (O2A) ---
        // ضروري لاستخدام agentController.putO2AObject() من الواجهة
        setEnabledO2ACommunication(true, 0); // Enable O2A, 0 means infinite queue
        System.out.println("   Agent " + getLocalName() + " enabled O2A communication.");

        // --- السلوك الرئيسي: استقبال طلبات الواجهة أو رسائل الرد ---
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // 1. التحقق من وجود كائنات من الواجهة (O2A)
                Object o2aObject = myAgent.getO2AObject();
                if (o2aObject != null) {
                    System.out.println(myAgent.getLocalName() + ": Received O2A object from GUI.");
                    if (o2aObject instanceof Map) {
                        handleGuiRequest((Map<String, Object>) o2aObject);
                    } else {
                        System.out.println("   Received unexpected O2A object type: " + o2aObject.getClass().getName());
                    }
                }

                // 2. التحقق من رسائل ACL (مثل ردود التفاوض من وكيل المنزل)
                // MessageTemplate لتصفية الرسائل التي تهمنا فقط (مثال: ردود التفاوض)
                MessageTemplate mt = MessageTemplate.MatchOntology("house-negotiation");
                ACLMessage msg = myAgent.receive(mt);
                if (msg != null) {
                    System.out.println(myAgent.getLocalName() + ": Received ACL message from " + msg.getSender().getLocalName());
                    System.out.println("   Performative: " + ACLMessage.getPerformative(msg.getPerformative()));
                    System.out.println("   Content: " + msg.getContent());
                    // **TODO:** تحليل الرد (ACCEPT/REJECT) وإبلاغ واجهة المستخدم
                    // handleNegotiationResponse(msg);
                } else {
                    // لا توجد رسائل أو كائنات، انتظر
                    block();
                }
            }
        });

        // --- فتح واجهة المستخدم الخاصة بالعميل ---
        // ** هام: يجب تشغيل هذا الكود في Event Dispatch Thread (EDT) الخاص بـ Swing **
        // ** ويفضل أن يتم ذلك بعد اكتمال إعداد الوكيل الأساسي **
        // ** TODO: قم بإلغاء التعليق وتعديل هذا الجزء لفتح الواجهة المناسبة **
        /*
        SwingUtilities.invokeLater(() -> {
            System.out.println("ClientAgent " + getLocalName() + " is opening client GUI...");
            // تحتاج إلى طريقة للحصول على AgentController الخاص بهذا الوكيل لتمريره للواجهة
            // قد تحتاج لتمرير this (الوكيل نفسه) إذا كانت الواجهة ستستخدم putO2AObject للتواصل معه
            // HouseFrame clientGUI = new HouseFrame(dbConnection); // الإصدار القديم
            // أو واجهة جديدة تتفاعل مع الوكيل
            // ClientAgentGUI clientGUI = new ClientAgentGUI(this, dbConnection); // تمرير الوكيل
            // clientGUI.setVisible(true);
        });
        */
        System.out.println("   ClientAgent " + getLocalName() + " setup complete. Waiting for GUI requests or ACL messages.");
    }

    // --- معالجة الطلبات من الواجهة ---
    private void handleGuiRequest(Map<String, Object> requestData) {
        String action = (String) requestData.get("action");
        System.out.println("   Handling GUI Action: " + action);

        if ("SUBMIT_OFFER".equals(action)) {
            Integer houseId = (Integer) requestData.get("houseId");
            Double price = (Double) requestData.get("proposedPrice");
            Integer duration = (Integer) requestData.get("proposedDuration");

            if (houseId != null && price != null && duration != null) {
                System.out.println("   Received offer submission request for House ID: " + houseId);
                System.out.println("   Proposed Price: " + price + ", Proposed Duration: " + duration);

                // **TODO:** هنا يجب أن يبدأ سلوك التفاوض الفعلي
                // 1. تحديد AID وكيل المنزل (تحتاج لطريقة لمعرفته، ربما عبر DF أو اسم ثابت)
                // AID houseAgentAID = findHouseAgentAID(houseId);
                // 2. إنشاء رسالة PROPOSE
                // ACLMessage proposeMsg = new ACLMessage(ACLMessage.PROPOSE);
                // proposeMsg.addReceiver(houseAgentAID);
                // proposeMsg.setOntology("house-negotiation");
                // proposeMsg.setContent("price=" + price + ";duration=" + duration);
                // proposeMsg.setConversationId("neg-" + System.currentTimeMillis());
                // 3. إرسال الرسالة
                // send(proposeMsg);
                System.out.println("   **PLACEHOLDER:** Negotiation behaviour should start here to send PROPOSE to house agent.");

            } else {
                System.out.println("   Error: Missing data in SUBMIT_OFFER request.");
            }
        } else {
            System.out.println("   Unknown GUI action received: " + action);
        }
    }

    // --- معالجة ردود التفاوض (ACL) ---
    private void handleNegotiationResponse(ACLMessage msg) {
        // **TODO:** تحليل الرد (ACCEPT/REJECT) وتحديث واجهة المستخدم
        // استخدم SwingUtilities.invokeLater لتحديث أي مكونات Swing
        // مثال:
        // String responseContent = msg.getContent();
        // int performative = msg.getPerformative();
        // SwingUtilities.invokeLater(() -> {
        //    if (negotiationFrame != null) { // تأكد أن لديك مرجع للواجهة
        //        if (performative == ACLMessage.ACCEPT_PROPOSAL) {
        //            negotiationFrame.resultArea.setText("✅ تم قبول العرض من المالك!");
        //        } else if (performative == ACLMessage.REJECT_PROPOSAL) {
        //            negotiationFrame.resultArea.setText("❌ تم رفض العرض من المالك. السبب: " + responseContent);
        //        }
        //    }
        // });
    }


    @Override
    protected void takeDown() {
        // تنظيف الموارد عند إيقاف الوكيل
        System.out.println("💤 Client agent " + getAID().getName() + " terminating.");
        // إغلاق الواجهة إذا كانت مرتبطة بالوكيل؟
        // إغلاق اتصال قاعدة البيانات إذا كان الوكيل يديره؟
        setEnabledO2ACommunication(false, 0); // تعطيل O2A
    }

    // --- (اختياري) طريقة مساعدة للبحث عن وكيل المنزل عبر DF ---
    // private AID findHouseAgentAID(int houseId) {
    //     AID houseAgent = null;
    //     // ... كود للبحث في الـ Directory Facilitator (DF) ...
    //     // تحتاج إلى أن يقوم وكيل المنزل بتسجيل نفسه في الـ DF أولاً
    //     return houseAgent;
    // }
}