package polytech.sacc.onfine.entity;

public class Admin {
    private String email;

    public Admin(String email) {
        this.email = email;
    }

    public Admin(){

    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "Admin{" +
                "email='" + email + '\'' +
                '}';
    }
}
