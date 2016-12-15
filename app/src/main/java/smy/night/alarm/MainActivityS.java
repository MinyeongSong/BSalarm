package smy.night.alarm;


import android.app.Activity;
import android.content.Intent;
import android.os.StrictMode;
        import android.os.Bundle;
    import android.util.Log;
    import android.view.KeyEvent;
    import android.view.View;
    import android.view.inputmethod.EditorInfo;
    import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
        import android.widget.EditText;
        import android.widget.ListView;
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
    import java.io.UnsupportedEncodingException;
    import java.net.HttpURLConnection;
        import java.net.URL;
    import java.net.URLEncoder;

    import javax.xml.parsers.DocumentBuilder;
        import javax.xml.parsers.DocumentBuilderFactory;

public class MainActivityS extends Activity implements ListViewAdapter.ListBtnClickListener{
    private Button btn;
    private AutoCompleteTextView et;
    private String strUrl;
    private String currentData;
    ListView listview;
    ListViewAdapter adapter;
    Intent intent;
    String[] stationList;
    SubwayList subwayList;
    private BackPressCloseHandler backPressCloseHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mains);
        stationList = subwayList.station;
        backPressCloseHandler = new BackPressCloseHandler(this);
        et = (AutoCompleteTextView) findViewById(R.id.et);
        et.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_dropdown_item_1line, stationList));
        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch (actionId) {
                    case EditorInfo.IME_ACTION_SEARCH:
                        String getEdit = et.getText().toString();
                        try {
                            getEdit = URLEncoder.encode(getEdit, "UTF-8");
                        } catch (UnsupportedEncodingException e) {}
                        strUrl = getResources().getString(R.string.stationUrl) + getEdit;
                        StrictMode.enableDefaults();//쓰레드 없이 네트워크 사용가능
                        currentData = getXml(strUrl);
                        parseWithDOM(0);
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
        Button idxg = (Button)findViewById(R.id.idxg);
        adapter = new ListViewAdapter(this);
        listview = (ListView) findViewById(R.id.lv);
        listview.setAdapter(adapter);
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView adapterView, View view, int i, long l) {
                ListItem item = (ListItem)adapterView.getItemAtPosition(i);

                String code = item.getStationCd();
                String name = item.getStationNm();
                String line = item.getLineNm();
                intent = new Intent(MainActivityS.this, Search.class);
                intent.putExtra("code", code);
                intent.putExtra("name", name);
                intent.putExtra("line", line);
                startActivity(intent);
            }
        });

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
            showMsg("네트워크에 연결해주세요");
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
                parseSubway(db);
            }else{
                //parseSub(db);
            }
        }catch(Exception e){
        }
    } // end parseWithDOM()
    private void parseSubway(DocumentBuilder db){
        StringBuffer sb = new StringBuffer();

        String name = "";
        String line = "";
        String code ="";
        try{
            byte bs[] = currentData.getBytes("utf-8");
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
                        //sb.append(elementName + ": " + value +"\n");
                        // 여기에 태그별로 컬렉션 이용해서 저장
                        if(elementName.equals("STATION_CD")){
                            code = value;
                        }
                        if(elementName.equals("STATION_NM")) {
                            name = value;
                        }
                        if(elementName.equals("LINE_NUM")) {
                            line = value;
                        }
                    }
                }
                adapter.addItem(code, name, line);
                adapter.notifyDataSetChanged();
            }
            if(code.equals("")){
                showMsg("정확한 역명을 입력하세요 ex.양재");
            }
        }catch(Exception e){
        }
    } // end parseBus()
    private void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


    @Override
    public void onListBtnClick(String stCode, String stName, String lineName) {
        Intent in = new Intent(this, BookMarkActivity.class);
        in.setAction("sub_star");
        in.putExtra("stCode", stCode);
        in.putExtra("stName", stName);
        in.putExtra("lineName", lineName);
        startActivity(in);
    }
    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();

    }
}


