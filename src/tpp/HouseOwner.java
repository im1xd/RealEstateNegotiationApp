package tpp;

public class HouseOwner extends User {
    public HouseOwner(int id, String username, String password) {
        super(id, username, password, "owner");
    }

    public HouseOwner(String username, String password) {
        super(username, password, "owner");
    }

    // يمكنك إضافة خصائص إضافية لاحقًا لو أردت
}
