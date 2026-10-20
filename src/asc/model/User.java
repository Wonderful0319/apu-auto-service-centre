package asc.model;

/**
 * Abstract base class representing a generic user in the APU-ASC system.
 * Demonstrates Abstraction and Inheritance (OOP concepts).
 * Encapsulation: all fields are protected, accessed via public getters/setters.
 */
public abstract class User {
    protected String userId;
    protected String username;
    protected String password;
    protected String name;
    protected String email;
    protected String phone;
    protected String role;
    protected String gender; // "Male" or "Female"

    public User(String userId, String username, String password, String name,
                String email, String phone, String role, String gender) {
        this.userId   = userId;
        this.username = username;
        this.password = password;
        this.name     = name;
        this.email    = email;
        this.phone    = phone;
        this.role     = role;
        this.gender   = (gender != null) ? gender : "";
    }

    public String getUserId()   { return userId; }
    public void   setUserId(String userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void   setUsername(String username) { this.username = username; }
    public String getPassword() { return password; }
    public void   setPassword(String password) { this.password = password; }
    public String getName()     { return name; }
    public void   setName(String name) { this.name = name; }
    public String getEmail()    { return email; }
    public void   setEmail(String email) { this.email = email; }
    public String getPhone()    { return phone; }
    public void   setPhone(String phone) { this.phone = phone; }
    public String getRole()     { return role; }
    public String getGender()   { return gender; }
    public void   setGender(String gender) { this.gender = gender; }

    public abstract String getDisplayInfo();
    public abstract void openDashboard();

    public String toFileString() {
        return userId + "|" + username + "|" + password + "|"
                + name + "|" + email + "|" + phone + "|" + gender;
    }

    @Override
    public String toString() {
        return name + " (" + username + ")";
    }
}
