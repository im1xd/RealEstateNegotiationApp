package tpp;

public class User {
    protected int id;
    protected String username;
    protected String password;
    protected String role;

    public User(int id, String username, String password, String role) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = (role != null && !role.trim().isEmpty()) ? role.trim().toLowerCase() : "client";
    }

    public User(String username, String password, String role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters و Setters
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }

    public void setId(int id) { this.id = id; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setRole(String role) { this.role = role; }

private String fullName, phone, email;

public void setFullName(String fullName) { this.fullName = fullName; }
public void setPhone(String phone) { this.phone = phone; }
public void setEmail(String email) { this.email = email; }

public String getFullName() { return fullName; }
public String getPhone() { return phone; }
public String getEmail() { return email; }
}