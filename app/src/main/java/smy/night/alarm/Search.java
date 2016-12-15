package smy.night.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
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

public class Search extends Activity implements AdapterView.OnItemSelectedListener{
    String cd, nm, ln;
    String[] item1 = {"평일", "토요일", "휴일/일요일"};//1, 2, 3
    String[] item2 = {"상행, 내선", "하행, 외선"};//1, 2
    String week, inout;
    Button search;
    String infoUrl;
    String currentData1;
    ListView listview;
    ListViewAdapter1 adapter;
    Intent intentT;
    String timeLast, direc;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.search);
        //지하철역 코드를 받아옴
        Intent intent = getIntent();
        cd=intent.getStringExtra("code");
        nm = intent.getStringExtra("name");
        ln = intent.getStringExtra("line");
        TextView tv = (TextView)findViewById(R.id.tv);
        tv.setText(nm+" "+ln);
        //스피너
        Spinner s1 = (Spinner)findViewById(R.id.sp1);
        Spinner s2 = (Spinner)findViewById(R.id.sp2);
        search = (Button)findViewById(R.id.search);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, item1);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, item2);
        adapter1.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s1.setPrompt("요일");
        s2.setPrompt("방향");
        s1.setAdapter(adapter1);
        s2.setAdapter(adapter2);
        s1.setOnItemSelectedListener(this);
        s2.setOnItemSelectedListener(this);
        //리스트뷰
        adapter = new ListViewAdapter1();
        listview = (ListView) findViewById(R.id.lv1);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                ListItem item = (ListItem)adapterView.getItemAtPosition(i);
                timeLast = item.getLeftTm();
                direc = item.getLastNm();
                AlertDialog dialog = createDialogBox();
                dialog.show();

            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoUrl = getResources().getString(R.string.infoUrl) + cd + week + inout;
                StrictMode.enableDefaults();
                currentData1 = getXml(infoUrl);
                parseWithDOM(0);
            }
        });

    }
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        if(adapterView.getId()==R.id.sp1) {
            week = item1[i];
            if(week.equals("평일")) week="/1";
            else if(week.equals("토요일")) week="/2";
            else if(week.equals("휴일/일요일")) week="/3";
        }else if(adapterView.getId() == R.id.sp2){
            inout = item2[i];
            if(inout.equals("상행, 내선")) inout="/1";
            else if(inout.equals("하행, 외선")) inout="/2";
        }

    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    private String getXml(String infoUrl){
        StringBuffer sb = new StringBuffer();
        URL u;
        HttpURLConnection uc;

        InputStream is = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try{
            u = new URL(infoUrl);
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
            showMsg(e.toString());
            return "";
        }finally{
            try{
                if( br != null) br.close();
                if( isr != null) isr.close();
                if( is != null) is.close();
            }catch(IOException ie){}
        }
    }
    private void parseWithDOM(int currentUrl){
        DocumentBuilderFactory dbf = null;
        DocumentBuilder db = null;
        try{
            dbf = DocumentBuilderFactory.newInstance();
            dbf.setIgnoringElementContentWhitespace(true); // 공백무시
            dbf.setIgnoringComments(true); // 주석무시
            db = dbf.newDocumentBuilder();

            if(currentUrl == 0) {
                adapter.remove();
                parseTime(db);
            }else{
                //parseSub(db);
            }
        }catch(Exception e){
            showMsg("parseWithDOM: " + e.toString());
        }
    } // end parseWithDOM()
    private void parseTime(DocumentBuilder db){
        StringBuffer sb = new StringBuffer();

        String name = "";
        String time ="";
        try{
            byte bs[] = currentData1.getBytes("utf-8");
            InputStream is = new ByteArrayInputStream(bs);
            Document d = db.parse(is);

            Element de = d.getDocumentElement();

            NodeList nlist = de.getElementsByTagName("row");
            Node nChild = null;
            String elementName = null;
            Node nText = null;
            for(int i=0; i<nlist.getLength(); i++){
                nChild = nlist.item(i);
                NodeList stationList = nChild.getChildNodes();
                Node nChildStation = null;
                for(int j =0; j<stationList.getLength(); j++){
                    nChildStation = stationList.item(j);
                    if(nChildStation.getNodeType() == Node.ELEMENT_NODE){
                        elementName = nChildStation.getNodeName();
                        nText = nChildStation.getFirstChild();
                        String value =  nText.getNodeValue();
                        sb.append(elementName + ": " + value +"\n");
                        // 여기에 태그별로 컬렉션 이용해서 저장
                        if(elementName.equals("SUBWAYENAME")){
                            name = value;
                        }
                        if(elementName.equals("LEFTTIME")) {
                            time = value;
                        }
                    }
                }
                adapter.addItem(name, time);
                adapter.notifyDataSetChanged();
            }
        }catch(Exception e){
            showMsg("parseStation: " +e.toString() );
        }
    } // end parseBus()
    private void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private AlertDialog createDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(ln+" "+nm+"\n" +
                "막차시간 "+timeLast);
        builder.setMessage("알람을 설정하시겠습니까?");
        builder.setCancelable(false);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                intentT = new Intent(Search.this, Alarm.class);
                intentT.putExtra("time", timeLast);
                intentT.putExtra("name", nm);
                intentT.putExtra("line", ln);
                intentT.putExtra("dir", direc);
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