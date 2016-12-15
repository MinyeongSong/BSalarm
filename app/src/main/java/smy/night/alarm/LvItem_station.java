package smy.night.alarm;


public class LvItem_station {
    private String  stationName ;
    private String arsId ;
    private int distance;
    private Double gpsX;
    private Double gpsY;

    public void setStationName(String stationName) {
        this.stationName = stationName;
    }
    public void setArsId(String arsId) {
        this.arsId = arsId;
    }
    public void setDistance(float distance) {
        int v = (int)distance;
        this.distance = v;
    }
    public void setGpsX(Double gpsX) {
        this.gpsX = gpsX;
    }
    public void setGpsY(Double gpsY) {
        this.gpsY = gpsY;
    }

    public String getStationName() {
        return this.stationName;
    }
    public String getArsId() {
        return this.arsId;
    }
    public int getDistance() {
        return this.distance;
    }
    public Double getGpsX() {
        return gpsX;
    }
    public Double getGpsY() {
        return gpsY;
    }
}