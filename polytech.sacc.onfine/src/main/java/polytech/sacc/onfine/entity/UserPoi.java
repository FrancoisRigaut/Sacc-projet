package polytech.sacc.onfine.entity;

public class UserPoi {
    private String sha1;
    private String poiDate;

    public UserPoi(){

    }

    public UserPoi(String sha1, String poiDate) {
        this.sha1 = sha1;
        this.poiDate = poiDate;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getPoiDate() {
        return poiDate;
    }

    public void setPoiDate(String poiDate) {
        this.poiDate = poiDate;
    }

    @Override
    public String toString() {
        return "UserPoi{" +
                "sha1='" + sha1 + '\'' +
                ", poiDate='" + poiDate + '\'' +
                '}';
    }
}
