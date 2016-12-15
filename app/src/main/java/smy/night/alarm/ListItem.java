package smy.night.alarm;
//역이름 to 역코드
//역코드 to 막차시간

public class ListItem {
    private String  staionNm ;
    private String stationCd ;
    private String lineNm;

    private String lastNm;
    private String leftTm;

    public void setStaionNm(String staionNm) {
        this.staionNm =  staionNm ;
    }
    public void setStationCd(String stationCd) {
        this.stationCd = stationCd ;
    }
    public void setLineNm(String lineNm){
        this.lineNm = lineNm;
    }
    public String getStationNm() {
        return this.staionNm ;
    }
    public String getStationCd() {
        return this.stationCd ;
    }
    public String getLineNm(){
        return this.lineNm;
    }

    public void setLastNm(String lastNm){
        this.lastNm = lastNm;
    }
    public void setLeftTm(String leftTm){
        this.leftTm = leftTm;
    }
    public String getLastNm(){
        return this.lastNm;
    }
    public String getLeftTm(){
        return this.leftTm;
    }
}