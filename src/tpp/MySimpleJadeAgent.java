package tpp; // تأكد من أن الحزمة صحيحة

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class MySimpleJadeAgent extends Agent {

    @Override
    protected void setup() {
        // هذه الطريقة تُستدعى مرة واحدة عند بدء تشغيل الوكيل
        System.out.println("👋 Hello! Agent " + getAID().getName() + " is ready.");
        // يمكنك إضافة رسالة هنا لتأكيد بدء الوكيل

        // إضافة سلوك بسيط: طباعة أي رسالة يستقبلها
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = myAgent.receive(); // محاولة استقبال رسالة
                if (msg != null) {
                    // تمت استقبال رسالة
                    System.out.println("📬 Agent " + myAgent.getLocalName() + " received message: ");
                    System.out.println("   Sender: " + msg.getSender().getName());
                    System.out.println("   Performative: " + ACLMessage.getPerformative(msg.getPerformative()));
                    System.out.println("   Content: " + msg.getContent());

                    // (اختياري) إرسال رد بسيط
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent("Received your request!");
                        myAgent.send(reply);
                        System.out.println("   Sent reply: INFORM");
                    }

                } else {
                    // لا توجد رسائل، انتظر قليلاً
                    block();
                }
            }
        });
    }

    @Override
    protected void takeDown() {
        // هذه الطريقة تُستدعى عند إيقاف الوكيل
        System.out.println("💤 Agent " + getAID().getName() + " is shutting down.");
        // يمكنك إضافة رسالة هنا لتأكيد إيقاف الوكيل
    }
}