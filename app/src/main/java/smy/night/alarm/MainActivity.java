package smy.night.alarm;

    import android.Manifest;
        import android.app.Activity;
        import android.app.AlertDialog;
        import android.content.Context;
        import android.content.DialogInterface;
        import android.content.Intent;
        import android.content.pm.PackageManager;
        import android.location.Location;
        import android.location.LocationListener;
        import android.location.LocationManager;
        import android.os.Bundle;
        import android.os.Handler;
        import android.os.Message;
    import android.os.StrictMode;
    import android.provider.Settings;
        import android.support.v4.app.ActivityCompat;
        import android.util.Log;
    import android.view.KeyEvent;
    import android.view.View;
    import android.view.inputmethod.EditorInfo;
    import android.widget.AdapterView;
        import android.widget.Button;
    import android.widget.EditText;
    import android.widget.ListView;
        import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

        import org.w3c.dom.Document;
        import org.w3c.dom.Element;
        import org.w3c.dom.Node;
        import org.w3c.dom.NodeList;

        import java.io.BufferedReader;
        import java.io.ByteArrayInputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.io.InputStreamReader;
        import java.net.HttpURLConnection;
        import java.net.URL;

        import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivity extends Activity implements LvAdapter_station.ListBtnClickListener{
    private Intent i;
    private ProgressBar pb = null;

    private String currentData;
    private int currentUrl;
    private double gpsX = 127.027374; // 위도
    private double gpsY = 37.497950; // 경도
    private final static int RADIUS = 500;
    private String strUrl;

    LocationManager mLocMgr;
    private boolean isGPSEnabled;
    private boolean isNetworkEnabled;
    Location lastKnown_net;
    Location lastKnown_gps;

    ListView listview;
    LvAdapter_station adapter;

    private LocationListener locationListener = null;
    private BackPressCloseHandler backPressCloseHandler;
    private EditText busid;
    Button btn_gps;
    int j;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        backPressCloseHandler = new BackPressCloseHandler(this);
        setContentView(R.layout.activity_main);
        //parseM(0);
        pb = (ProgressBar) findViewById(R.id.progressBar1);
        btn_gps = (Button)findViewById(R.id.btn_gps);
        strUrl = getResources().getString(R.string.url_station_gps);

        // Adapter 생성
        adapter = new LvAdapter_station(this);
        // 리스트뷰 참조 및 Adapter달기
        listview = (ListView) findViewById(R.id.lv);
        listview.setAdapter(adapter);

        busid = (EditText) findViewById(R.id.busid);
        busid.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        j=1227;
                        String getEdit = busid.getText().toString();
                        String strUrl_num = getResources().getString(R.string.url_station_by_id)+"&arsId="+ getEdit;
                        StrictMode.enableDefaults();//쓰레드 없이 네트워크 사용가능
                        currentData = getXml(strUrl_num);
                        parseM(0);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        btn_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //StrictMode.enableDefaults();
                j=0;
                adapter.remove();
                busid.setText(null);
                pb.setVisibility(View.VISIBLE);
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                // 마시멜로 이상 버전에서는 이게 들어가야됨, 사용자 위치는 사용해도 되냐고 요청을 반드시 해야함
                getGPS();
            }
        });


        //아이템을 클릭했을 때
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                LvItem_station item = (LvItem_station) parent.getItemAtPosition(position);

                String arsId = item.getArsId();
                String stationNm = item.getStationName();
                Double x = item.getGpsX();
                Double y = item.getGpsY();

                i = new Intent(MainActivity.this, SubActivity.class);
                if (j==1227) i.setAction("nomap");
                else i.setAction("1");
                i.putExtra("arsId", arsId);
                i.putExtra("stationNm", stationNm);
                i.putExtra("stX", x);
                i.putExtra("stY", y);
                i.putExtra("gpsX", gpsX);
                i.putExtra("gpsY", gpsY);
                startActivity(i);

                // TODO : use item data.
            }
        });

    } // end onCreate();


    @Override
    public void onStart() {super.onStart();}

    @Override
    public void onResume() {
        super.onResume();
    }
    @Override
    public void onPause(){
        super.onPause();

    }

    @Override
    public void onRestart() {
        super.onRestart();
        if(lastKnown_gps != null && lastKnown_net != null){
            if (lastKnown_gps.getAccuracy() >= lastKnown_net.getAccuracy()) {
                gpsY = lastKnown_gps.getLatitude();
                gpsX = lastKnown_gps.getLongitude();
            } else {
                gpsY = lastKnown_net.getLatitude();
                gpsX = lastKnown_net.getLongitude();
            }
        }else{
            getGPS();
        }
    }
    @Override
    public void onStop() { // 다른 액티비티가 실행됬을 때
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if(locationListener != null) {
            mLocMgr.removeUpdates(locationListener);
            Log.i("check", "removeUpdates()");
        }
    }

    void getGPS(){
        mLocMgr = (LocationManager) getSystemService(Context.LOCATION_SERVICE); // 위치 얻어오기위한 코드
        isGPSEnabled = mLocMgr.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetworkEnabled = mLocMgr.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if(isNetworkEnabled && isGPSEnabled) {
            //pb.setVisibility(View.VISIBLE);
            locationListener = new LocListener();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocMgr.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 30000, 10, locationListener);
            mLocMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 30000, 10, locationListener);

            lastKnown_net = mLocMgr.getLastKnownLocation(mLocMgr.NETWORK_PROVIDER);
            lastKnown_gps = mLocMgr.getLastKnownLocation(mLocMgr.GPS_PROVIDER);
        }else{
            alertbox("GPS 상태", "위치서비스가 비활성화 되어있습니다.");
        }
    }


    @Override
    public void onListBtnClick(int position, String arsId, String name, int distance, Double gpsX, Double gpsY) {
        Intent intent = new Intent(this, BookMarkActivity.class);
        intent.setAction("star");
        intent.putExtra("arsId", arsId);
        intent.putExtra("name", name);
        intent.putExtra("distance", distance);
        intent.putExtra("gpsX", gpsX);
        intent.putExtra("gpsY", gpsY);
        startActivity(intent);
    }

    private class LocListener implements LocationListener{
        @Override
        public void onLocationChanged(Location location) {
            gpsY = location.getLatitude();
            gpsX = location.getLongitude();
            new Thread() {
                public void run() {
                    currentData = getXml(strUrl + "&tmX=" + gpsX + "&tmY=" + gpsY + "&radius=" + RADIUS); // xml 정보를 얻어와서 String 값으로 입력
                    currentUrl = 0;
                    parseM(currentUrl);
                }
            }.start();
            pb.setVisibility(View.GONE);
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            if(locationListener != null) {
                mLocMgr.removeUpdates(locationListener);
            }
        }
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        @Override
        public void onProviderEnabled(String provider) {
            showMsg("onProviderEnabled");
        }
        @Override
        public void onProviderDisabled(String provider) {
            showMsg("onProviderDisabled");
        }
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
            Log.i("getXML 에러", ""+e);
            return "";

        }finally{
            try{
                if( br != null) br.close();
                if( isr != null) isr.close();
                if( is != null) is.close();
            }catch(IOException ie){}
        }
    } // end getXml()

    private void parseM(int currentUrl){
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        try{
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true); // 공백무시
            dbf.setIgnoringComments(true); // 주석무시
            db = dbf.newDocumentBuilder();

            if(currentUrl == 0) {
                adapter.remove();
                parse(db);
            }else if(currentUrl ==1){
            }
        }catch(Exception e){
            Log.i("parseM에러 ","" + e);
        }
    } // end parseM()

    private void parse(DocumentBuilder db){ // pull 파싱으로
        String stationNm = "";
        String arsId = "";
        Double x = 0.0;
        Double y = 0.0;
        try{
            byte bs[] = currentData.getBytes("utf-8");
            InputStream is = new ByteArrayInputStream(bs);
            Document d = db.parse(is);

            Element de = d.getDocumentElement();

            NodeList nlist = de.getElementsByTagName("itemList");
            Node nChild = null;
            String elementName = null;
            Node nText = null;
            int k = 0;
            if(j==0) k=nlist.getLength();
            else if(j==1227) k=1;

            for(int i=0; i<k; i++){
                nChild = nlist.item(i);
                NodeList busList = nChild.getChildNodes();
                Node nChildBus = null;
                for(int j =0; j<busList.getLength(); j++){
                    nChildBus = busList.item(j);
                    if(nChildBus.getNodeType() == Node.ELEMENT_NODE){
                        elementName = nChildBus.getNodeName();
                        nText = nChildBus.getFirstChild();
                        String value =  nText.getNodeValue();
                        // 여기에 태그별로 컬렉션 이용해서 저장
                        if(elementName.equals("stationNm")){
                            stationNm = value;
                        }else if(elementName.equals("arsId")) {
                            arsId = value;
                        }else if(elementName.equals("gpsX")) {
                            x = Double.parseDouble(value);
                        }else if(elementName.equals("gpsY")) {
                            y = Double.parseDouble(value);
                        }else if(elementName.equals("stNm")){
                            stationNm = value;
                        }
                    }
                }
                float distance[] = new float[3];
                Location.distanceBetween(gpsX, gpsY, x, y, distance);
                if(!arsId.equals("0")) {
                    if(j==1227) distance[0]=0;
                    adapter.addItem(stationNm, arsId, distance[0], x, y);
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                adapter.notifyDataSetChanged();
                            }catch(Exception e){}
                        }
                    }, 100);
                }
            }
        }catch(Exception e){
            Log.i("parse 에러", ""+e.toString());
            showMsg("정확히 입력해주세요");
        }
    } // end parseBus()

    final Handler handler = new Handler(){
        public void handleMessage(Message msg){
            adapter.notifyDataSetChanged();
        }
    };

    protected void alertbox(String title, String mymessage){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(mymessage)
                .setCancelable(false)
                .setTitle(title)
                .setPositiveButton("GPS ON", new DialogInterface.OnClickListener() {
                    //  폰 위치 설정 페이지로 넘어감
                    public void onClick(DialogInterface dialog, int id) {
                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("CANCLE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();

    }
}
