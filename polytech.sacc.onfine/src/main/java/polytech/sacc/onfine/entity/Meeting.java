package polytech.sacc.onfine.entity;

public class Meeting {
    private String sha1;
    private String sha1Met;
    private Gps gps;
    private String timestamp;

    public Meeting(String sha1, String sha1Met, Gps gps, String timestamp) {
        this.sha1 = sha1;
        this.sha1Met = sha1Met;
        this.gps = gps;
        this.timestamp = timestamp;
    }

    public Meeting(){

    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSha1Met() {
        return sha1Met;
    }

    public void setSha1Met(String sha1Met) {
        this.sha1Met = sha1Met;
    }

    public Gps getGps() {
        return gps;
    }

    public void setGps(Gps gps) {
        this.gps = gps;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Meeting{" +
                "sha1='" + sha1 + '\'' +
                ", sha1Met='" + sha1Met + '\'' +
                ", gps=" + gps +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
