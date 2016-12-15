package smy.night.alarm;


public class LvItem_route {

    private String busRouteNm;
    private String stEnd;
    private String lastBusTm;
    private String beginBusTm;
    private String term;


    public void setBusRouteNm(String routeNm){
        this.busRouteNm = routeNm;
    }
    public void setStEnd(String end){
        this.stEnd = end;
    }
    public void setLastBusTm(String lastBus){
        this.lastBusTm = lastBus;
    }
    public void setBeginBusTm(String beginBus){
        this.lastBusTm = beginBus;
    }
    public void setTerm(String term){ this.term = term; }

    public String getTerm(){return this.term +"분 간격 배차";}
    public String getBusRouteNm(){
        return this.busRouteNm;
    }
    public String getStEnd(){
        return this.stEnd;
    }
    public String getLastBusTm(){
        return this.lastBusTm;
    }
    public String getBeginBusTm(){return this.beginBusTm;}

}
