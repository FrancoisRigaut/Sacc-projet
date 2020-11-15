package polytech.sacc.onfine.entity;

public class UserPoi {
    private String sha1;
    private String timestamp;

    public UserPoi(){

    }

    public UserPoi(String sha1, String poiDate) {
        this.sha1 = sha1;
        this.timestamp = timestamp;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String poiDate) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "UserPoi{" +
                "sha1='" + sha1 + '\'' +
                ", poiDate='" + timestamp + '\'' +
                '}';
    }
}
