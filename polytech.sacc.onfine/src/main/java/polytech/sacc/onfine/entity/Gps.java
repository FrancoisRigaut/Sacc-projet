package polytech.sacc.onfine.entity;

public class Gps {
    private float latitude;
    private float longitude;

    public Gps(float latitude, float longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Gps(){

    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    @Override
    public String toString() {
        return "Gps{" +
                "latitude=" + latitude +
                ", longitude=" + longitude +
                '}';
    }
}
