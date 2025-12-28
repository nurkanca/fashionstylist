package tr.edu.kalyon.nur.university.controller.dto;

public class RegisterRequest {
    private String username;
    private String password;
    private String name;
    private String gender;

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getName() { return name; }
    public String getGender() { return gender; }

    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
    public void setName(String name) { this.name = name; }
    public void setGender(String gender) { this.gender = gender; }
}
