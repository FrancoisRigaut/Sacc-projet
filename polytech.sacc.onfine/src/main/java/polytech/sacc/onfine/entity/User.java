package polytech.sacc.onfine.entity;

public class User {
    private String sha1;

    public User(){

    }

    public User(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    @Override
    public String toString() {
        return "User{" +
                "sha1='" + sha1 + '\'' +
                '}';
    }
}
