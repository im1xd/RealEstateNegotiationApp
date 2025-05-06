package tpp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

import jade.core.Agent;

public class ClientConsoleApp extends Agent {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        try {
            // الاتصال بقاعدة البيانات
            Connection connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/house_db", "root", "");

            // تسجيل دخول أو تسجيل حساب جديد
            UserService userService = new UserService(connection);

            System.out.println("==== مرحبًا بالزبون ====");
            System.out.println("1. تسجيل حساب جديد");
            System.out.println("2. تسجيل الدخول");
            System.out.print("اختيارك: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // لتفادي مشاكل nextLine

            int clientId = -1;

            if (choice == 1) {
                System.out.print("أدخل اسم المستخدم: ");
                String username = scanner.nextLine();
                System.out.print("أدخل كلمة السر: ");
                String password = scanner.nextLine();

                User newUser = new User(0, username, password, "client");
                userService.registerUser(newUser);
                System.out.println("✅ تم تسجيل الحساب بنجاح. الرجاء تسجيل الدخول.");
            }

            System.out.print("اسم المستخدم: ");
            String username = scanner.nextLine();
            System.out.print("كلمة السر: ");
            String password = scanner.nextLine();

            User user = userService.login(username, password);

            if (user != null && user.getRole().equals("client")) {
                clientId = user.getId();
                System.out.println("✅ تم تسجيل الدخول بنجاح كزبون!");

                // استعراض المنازل
                HouseService houseService = new HouseService(connection);
                List<House> houses = houseService.getAllAvailableHouses();

                System.out.println("==== المنازل المتوفرة للكراء ====");
                for (House h : houses) {
                    System.out.println("ID المنزل: " + h.getId());
                    System.out.println("الموقع: " + h.getLocation());
                    System.out.println("السعر المطلوب: " + h.getPrice());
                    System.out.println("مدة الكراء المسموحة: من " + h.getMinDuration() + " إلى " + h.getMaxDuration());
                    System.out.println("---------------------------------");
                }

                System.out.print("أدخل ID المنزل الذي تريد التفاوض عليه: ");
                int selectedHouseId = scanner.nextInt();

                System.out.print("أدخل السعر الذي ترغب به: ");
                double offeredPrice = scanner.nextDouble();

                System.out.print("أدخل المدة التي ترغب بالكراء بها: ");
                int offeredDuration = scanner.nextInt();

             // بدء التفاوض
                NegotiationService negotiationService = new NegotiationService(connection);
                Negotiation newNegotiation = new Negotiation(
                    clientId,               // client_id
                    selectedHouseId,        // house_id
                    offeredPrice,           // proposed_price
                    offeredDuration         // proposed_duration
                );
                negotiationService.startNegotiation(newNegotiation);
            } else {
                System.out.println("❌ اسم المستخدم أو كلمة المرور غير صحيحين أو لست زبون.");
            }

        } catch (SQLException e) {
            System.out.println("❌ خطأ في الاتصال بقاعدة البيانات: " + e.getMessage());
        }
    }
}
