package smy.night.alarm;
    import android.app.Activity;
        import android.app.AlarmManager;
        import android.app.AlertDialog;
        import android.app.PendingIntent;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
        import android.util.Log;
        import android.view.View;
        import android.view.ViewGroup;
        import android.widget.AdapterView;
        import android.widget.Button;
        import android.widget.ListView;
        import android.widget.RelativeLayout;
        import android.widget.TextView;
        import android.widget.Toast;

        import net.daum.mf.map.api.CameraPosition;
        import net.daum.mf.map.api.CameraUpdateFactory;
        import net.daum.mf.map.api.CancelableCallback;
        import net.daum.mf.map.api.MapPOIItem;
        import net.daum.mf.map.api.MapPoint;
        import net.daum.mf.map.api.MapView;

        import org.xmlpull.v1.XmlPullParser;
        import org.xmlpull.v1.XmlPullParserFactory;

        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.io.StringReader;
        import java.net.HttpURLConnection;
        import java.net.URL;
        import java.util.Hashtable;
        import java.util.List;
        import java.util.Map;
        import java.util.Vector;

public class SubActivity extends Activity implements View.OnClickListener, MapView.MapViewEventListener{
    private static final String TAG = "SubActivity";
    private String strUrl_brs;
    private String strUrl_sbr;
    private String mapKey;
    private String arsId;
    private String stationNm;
    private String currentData;
    private Double stX;
    private Double stY;
    private Double myX;
    private Double myY;

    private TextView tv;
    private Button btn_map, btn_po;

    ListView listview ;
    LvAdapter_route adapter;

    ViewGroup mapViewContainer;
    MapView mapview;
    private CameraPosition camera_position;
    MapPOIItem stMarker;
    MapPOIItem myMarker;

    Intent getIn;
    Intent intentT;
    getAPI getApi;
    Map<String, List> infoMap;

    AlarmManager alarmMgr;
    RelativeLayout rl;

    String tempBusName;
    String tempStName;
    String tempDirection;
    String timeLast, busName, direction;

    Intent i;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);
        Log.i(TAG, "onCreate()");

        strUrl_brs = getResources().getString(R.string.url_route_by_station);
        strUrl_sbr = getResources().getString(R.string.url_station_by_route);
        getApi = new getAPI(strUrl_sbr, this);

        rl = (RelativeLayout)findViewById(R.id.listview_layout);
        tv = (TextView)findViewById(R.id.textView_sub);
        btn_map = (Button)findViewById(R.id.btn_route);
        btn_map.setOnClickListener(this);
        btn_po = (Button)findViewById(R.id.btn_my);

        getIn = getIntent();
        getData(); // arsId(정류소고유번호 얻어오기)
        if(getIn.getAction().equals("nomap")){
            btn_map.setVisibility(View.INVISIBLE);
        }
        adapter = new LvAdapter_route() ;
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.lv_route);
        listview.setAdapter(adapter);

        mapKey = getResources().getString(R.string.map_key);
        mapViewContainer= (ViewGroup) findViewById(R.id.map_view);
        createMap();
        mapViewContainer.addView(mapview);

        camera_position = new CameraPosition(MapPoint.mapPointWithGeoCoord(myY, myX), 1);

        alarmMgr = (AlarmManager) getSystemService(ALARM_SERVICE);

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                LvItem_route item = (LvItem_route) parent.getItemAtPosition(position);
                timeLast = item.getLastBusTm();
                busName = item.getBusRouteNm();
                direction = item.getStEnd();
                AlertDialog dialog = createDialogBox();
                dialog.show();
            }
        });

        new Thread() {
            public void run() {
                currentData = getXml(strUrl_brs + "&arsId=" + arsId ); // xml 정보를 얻어와서 String 값으로 입력
                parsePull(currentData);
            }
        }.start();
    }
    @Override
    public void onBackPressed(){
        if(mapViewContainer.getVisibility() == View.VISIBLE) {
            btn_map.setVisibility(View.VISIBLE);
            btn_po.setVisibility(View.INVISIBLE);
            mapViewContainer.setVisibility(mapViewContainer.GONE);
            rl.setVisibility(rl.VISIBLE);
        }else{
            super.onBackPressed();
            mapViewContainer.setVisibility(mapViewContainer.VISIBLE);
            rl.setVisibility(rl.GONE);
        }

    }

    public void onGoToMyPosition(View view) {
        mapview.animateCamera(CameraUpdateFactory.newCameraPosition(camera_position), 1000, new CancelableCallback() {
            @Override
            public void onFinish() {
                mapview.selectPOIItem(myMarker, true);
            }

            @Override
            public void onCancel() {
                Toast.makeText(getBaseContext(), "cancel", Toast.LENGTH_SHORT).show();
            }

        });
    }


    void createMap(){
        mapview = new MapView(SubActivity.this);
        mapview.setDaumMapApiKey(mapKey);
        mapview.setMapCenterPointAndZoomLevel(MapPoint.mapPointWithGeoCoord(stY, stX), 0, true);
        mapview.setMapViewEventListener(this);

    }

    void setMarker_st(){
        stMarker = new MapPOIItem();
        stMarker.setItemName(stationNm);
        stMarker.setTag(0);
        stMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(stY, stX));
        stMarker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        stMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapview.addPOIItem(stMarker);
        mapview.selectPOIItem(stMarker, true);
    }

    void setMarker_my(){
        myMarker = new MapPOIItem();
        myMarker.setItemName("현위치");
        myMarker.setTag(0);
        myMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(myY, myX));
        myMarker.setMarkerType(MapPOIItem.MarkerType.YellowPin); // 기본으로 제공하는 BluePin 마커 모양.
        myMarker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapview.addPOIItem(myMarker);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_route:
                btn_map.setVisibility(View.INVISIBLE);
                btn_po.setVisibility(View.VISIBLE);
                mapViewContainer.setVisibility(mapViewContainer.VISIBLE);
                rl.setVisibility(rl.GONE);
                break;
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mapViewContainer != null) mapViewContainer.removeAllViews();
        if(mapview != null) mapview.removeAllPOIItems();
    }

    void getData(){ // main에서 얻어온 정류소 번호
        this.arsId = getIn.getStringExtra("arsId");
        this.stationNm = getIn.getStringExtra("stationNm");
        this.stX = getIn.getDoubleExtra("stX", 0.0);
        this.stY = getIn.getDoubleExtra("stY", 0.0);
        this.myX = getIn.getDoubleExtra("gpsX", 0.0);
        this.myY = getIn.getDoubleExtra("gpsY", 0.0);
        tv.setText(stationNm);
    }
    private String getXml(String strUrl){
        StringBuffer sb = new StringBuffer();
        URL u;
        HttpURLConnection uc;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
            u = new URL(strUrl);
            uc = (HttpURLConnection) u.openConnection();
            if(uc != null){
                uc.setConnectTimeout(5000);
                uc.setUseCaches(false);
                if(uc.getResponseCode() == HttpURLConnection.HTTP_OK){
                    is = uc.getInputStream();
                    isr = new InputStreamReader(is, "utf-8");
                    br = new BufferedReader(isr);
                    String line= "";
                    while((line = br.readLine()) != null){
                        sb.append(line + "\n");
                    }
                }
                uc.disconnect();
            }
            return sb.toString();
        }catch(Exception e){
            return "";
        }finally{
            try{
                if( br != null) br.close();
                if( isr != null) isr.close();
                if( is != null) is.close();
            }catch(IOException ie){}
        }
    } // end getXml()

    List<Map> list;
    Map<String, String> map;
    Map<String, String> imap;
    private void parsePull(String data){
        try{
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(data));

            String busRouteId = "";
            String busRouteNm = "";
            String term = "";
            String tagName = "";
            int kindItem = -1;
            int eventType =  parser.getEventType();
            while(eventType != XmlPullParser.END_DOCUMENT){
                switch (eventType){

                    case XmlPullParser.START_DOCUMENT:
                        list = new Vector<Map>();
                        imap = new Hashtable<String, String>();
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if(tagName != null) tagName = tagName.trim();

                        if(tagName.equals("msgBody")){
                        }else if(tagName.equals("itemList")){
                            map = new Hashtable<String, String>();
                        }else if(tagName.equals("busRouteId")){
                            kindItem = 1;
                        }else if(tagName.equals("busRouteNm")){
                            kindItem = 2;
                        }else if(tagName.equals("term")){
                            kindItem = 3;
                        }else{
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        busRouteId = "";
                        busRouteNm = "";
                        term = "";
                        tagName = "";
                        kindItem = -1;
                        tagName = parser.getName();
                        if(tagName != null) tagName = tagName.trim();

                        if(tagName.equals("itemList")){
                            list.add(map);
                            String name = map.get("busRouteNm");
                            String t = map.get("term");
                            String id = map.get("busRouteId"); // 이걸 받아와서 at로 보내고 여기서 at가 수행되도록 하면 되나?

                            infoMap = getApi.getData(id);
                            String direc = (String)infoMap.get(arsId).get(0);
                            String last = (String)infoMap.get(arsId).get(1);
                            String begin = (String)infoMap.get(arsId).get(2);

                            imap.put(name, last);

                            adapter.addItem(name, direc, last, begin, t );
                        }else{}
                        break;
                    case XmlPullParser.TEXT:
                        if(kindItem == 1){
                            busRouteId = parser.getText();
                            if(busRouteId != null) busRouteId = busRouteId.trim();
                            if(busRouteId.length() != 0) map.put("busRouteId", busRouteId);
                        }else if(kindItem == 2){
                            busRouteNm = parser.getText();
                            if(busRouteNm != null) busRouteNm = busRouteNm.trim();
                            if(busRouteNm.length() != 0) map.put("busRouteNm", busRouteNm);
                        }else if(kindItem == 3){
                            term = parser.getText();
                            if(term != null) term = term.trim();
                            if(term.length() != 0) map.put("term", term);
                        }else{
                        }
                        break;
                }
                eventType = parser.next();
            }
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try{
                        adapter.notifyDataSetChanged();
                    }catch (Exception he) {}
                }
            }, 100);
        }catch(Exception e){
            Log.i("parsePull 에러(subA)", ""+e);
        }
    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
        }
    };

    @Override
    public void onMapViewInitialized(MapView mapView) {
        setMarker_st();
        setMarker_my();
    }
    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {}
    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {}
    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {}

    public void log(String msg){
        Log.i("에러", msg);
    }

    private AlertDialog createDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(busName+" "+direction+" 방면\n" +"막차시간 "+timeLast);
        builder.setMessage("알람을 설정하시겠습니까?");
        builder.setCancelable(false);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                intentT = new Intent(SubActivity.this, Alarm.class);
                timeLast = timeLast+":00";
                intentT.putExtra("time", timeLast);
                intentT.putExtra("name",stationNm);
                intentT.putExtra("line",busName);
                intentT.putExtra("dir", direction);
                intentT.setAction("set");
                startActivity(intentT);
            }
        });
        builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.cancel();
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }

}
