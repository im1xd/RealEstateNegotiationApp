package tpp; // تأكد من أن اسم الحزمة صحيح لمشروعك

import javax.swing.*;
import java.sql.Connection;
import java.sql.SQLException;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentContainer;
import jade.wrapper.AgentController;
import jade.wrapper.StaleProxyException;

public class MainApp {

    private static AgentContainer mainContainer; // احتفظ بمرجع للحاوية الرئيسية إذا احتجت لإطلاق وكلاء لاحقًا

    public static void main(String[] args) {

        // --- 1. بدء منصة JADE ---
        try {
            // الحصول على JADE runtime instance
            Runtime rt = Runtime.instance();
            // إغلاق الـ JVM عند إيقاف الحاوية الرئيسية
            rt.setCloseVM(true); // مهم للإغلاق الكامل

            // إنشاء Profile افتراضي
            Profile profile = new ProfileImpl();
            // profile.setParameter(Profile.MAIN_HOST, "localhost"); // اختياري: تحديد المضيف
            // profile.setParameter(Profile.LOCAL_PORT, "1099");    // اختياري: تحديد المنفذ
            profile.setParameter(Profile.GUI, "true"); // تشغيل واجهة JADE الرسومية (RMA)

            // إنشاء الحاوية الرئيسية
            mainContainer = rt.createMainContainer(profile);
            System.out.println("✅ JADE Main Container started with GUI (RMA).");

            // --- إطلاق الوكيل البسيط تلقائياً ---
            try {
                System.out.println("Attempting to launch MySimpleJadeAgent...");
                // اختر اسماً مستعاراً فريداً للوكيل
                String agentNickname = "MyFirstAgent";
                // اسم الكلاس المؤهل بالكامل (تأكد من الحزمة الصحيحة)
                String agentClassName = "tpp.MySimpleJadeAgent";

                // قم بإنشاء الوكيل (مرر null كوسائط إذا لم تكن هناك حاجة إليها)
                AgentController simpleAgent = mainContainer.createNewAgent(agentNickname, agentClassName, null);
                // ابدأ تشغيل الوكيل
                simpleAgent.start();

                System.out.println("🚀 Agent " + agentNickname + " launched successfully!");

            } catch (StaleProxyException spe) {
                // هذا الخطأ يحدث إذا حاولت استخدام AgentController بعد إيقاف الوكيل أو الحاوية
                System.err.println("❌ Error launching agent " + "MySimpleJadeAgent" + ": " + spe.getMessage());
                spe.printStackTrace();
            } catch (Exception agentEx) {
                 // التعامل مع أي أخطاء أخرى قد تحدث أثناء إنشاء/بدء الوكيل
                System.err.println("❌ Unexpected error launching agent " + "MySimpleJadeAgent" + ": " + agentEx.getMessage());
                agentEx.printStackTrace();
            }
            // --- نهاية إطلاق الوكيل البسيط ---

        } catch (Exception e) { // التعامل مع أخطاء بدء JADE Platform نفسها
            System.err.println("❌ فشل في بدء منصة JADE: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "فشل في بدء بيئة الوكلاء JADE. قد لا تعمل بعض الميزات.", "خطأ JADE", JOptionPane.ERROR_MESSAGE);
            // يمكنك أن تقرر إيقاف التطبيق هنا أو المتابعة بدونه
            // System.exit(1); // إلغاء التعليق للخروج إذا كان JADE إلزاميًا
        }


        // --- 2. بدء واجهة المستخدم الرسومية Swing (بعد محاولة بدء JADE) ---
        SwingUtilities.invokeLater(() -> { // التأكد من تشغيل Swing في الـ EDT
            try {
                // 1. الاتصال بقاعدة البيانات
                System.out.println("Attempting database connection...");
                Database.connect(); // تأكد أن هذا يعمل بشكل صحيح
                Connection connection = Database.getConnection();
                System.out.println("Database connection successful.");

                // 2. بدء عملية تسجيل الدخول/التسجيل
                System.out.println("Launching Login/Register Frame...");
                // في المستقبل، قد تحتاج هذه الواجهة لتمرير mainContainer أو طريقة للوصول إليه
                LoginRegisterFrame loginFrame = new LoginRegisterFrame(connection);
                loginFrame.setVisible(true);
                System.out.println("Login/Register Frame is visible.");

            } catch (SQLException e) {
                JOptionPane.showMessageDialog(null,
                        "❌ فشل فادح في الاتصال بقاعدة البيانات:\n" + e.getMessage() +
                        "\nالرجاء التأكد من تشغيل خدمة MySQL وتكوين قاعدة البيانات.",
                        "خطأ اتصال قاعدة البيانات", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1); // الخروج إذا فشل الاتصال بقاعدة البيانات
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "❌ حدث خطأ غير متوقع عند بدء التطبيق:\n" + e.getMessage(),
                        "خطأ غير متوقع", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(1); // الخروج في حالة الأخطاء الحرجة الأخرى
            }
        });
    }

    /**
     * طريقة مساعدة (اختيارية) لإطلاق وكيل JADE جديد من أي مكان في الكود.
     * @param agentNickname الاسم المستعار للوكيل (يجب أن يكون فريدًا)
     * @param agentClassName اسم الكلاس الكامل للوكيل (مثال: "tpp.ClientAgentJADE")
     * @param agentArgs مصفوفة من الكائنات لتمريرها كوسائط للوكيل عند الإعداد
     * @return AgentController للتحكم في الوكيل، أو null في حالة الفشل.
     */
    public static AgentController launchAgent(String agentNickname, String agentClassName, Object[] agentArgs) {
        if (mainContainer == null) {
            System.err.println("JADE Main Container is not initialized! Cannot launch agent.");
            JOptionPane.showMessageDialog(null, "بيئة JADE غير جاهزة لإطلاق الوكيل: " + agentNickname, "خطأ JADE", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        try {
            AgentController agent = mainContainer.createNewAgent(agentNickname, agentClassName, agentArgs);
            agent.start();
            System.out.println("🚀 Launched agent (via helper): " + agentNickname + " (" + agentClassName + ")");
            // رسالة تأكيد إضافية
            // JOptionPane.showMessageDialog(null, "تم إطلاق الوكيل: " + agentNickname, "إطلاق وكيل", JOptionPane.INFORMATION_MESSAGE);
            return agent;
        } catch (StaleProxyException e) {
            System.err.println("❌ Error launching agent " + agentNickname + " (via helper): " + e.getMessage());
            JOptionPane.showMessageDialog(null, "خطأ أثناء إطلاق الوكيل: " + agentNickname + "\n" + e.getMessage(), "خطأ JADE", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
            return null;
        } catch (Exception e) {
             System.err.println("❌ Unexpected error launching agent " + agentNickname + " (via helper): " + e.getMessage());
             JOptionPane.showMessageDialog(null, "خطأ غير متوقع أثناء إطلاق الوكيل: " + agentNickname + "\n" + e.getMessage(), "خطأ JADE", JOptionPane.ERROR_MESSAGE);
             e.printStackTrace();
             return null;
        }
    }
}