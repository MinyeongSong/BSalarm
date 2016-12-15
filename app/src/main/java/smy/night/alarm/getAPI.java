package smy.night.alarm;

    import android.os.StrictMode;

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

/**
 * Created by ssh67 on 2016-08-09.
 */
public class getAPI {
    private String strUrl;
    private String currentData;
    private SubActivity subA;

    public getAPI(String url, SubActivity sub){
        this.strUrl = url;
        this.subA = sub;
    }

    public Map<String, List> getData(String id){
        StrictMode.enableDefaults();
        currentData = getXml(this.strUrl + "&busRouteId=" + id);
        parsePull(currentData);
        return infoMap;
    }

    public String getXml(String strUrl){
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
            return String.valueOf(sb);

        }catch(Exception e){
            subA.log("URL에러: "+  e.toString());
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
    Map<String, List> infoMap;
    List<String> infoList;
    public void parsePull(String data) {
        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            XmlPullParser parser = factory.newPullParser();
            parser.setInput(new StringReader(data));

            String beginTm = "";
            String lastTm = "";
            String direction = "";
            String arsId = "";
            String tagName = "";
            int kindItem = -1;
            int eventType = parser.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {
                switch (eventType) {
                    case XmlPullParser.START_DOCUMENT:
                        list = new Vector<Map>();
                        infoMap = new Hashtable<String, List>();
                        break;
                    case XmlPullParser.END_DOCUMENT:
                        break;
                    case XmlPullParser.START_TAG:
                        tagName = parser.getName();
                        if (tagName != null) tagName = tagName.trim();

                        if (tagName.equals("msgBody")) {
                        } else if (tagName.equals("itemList")) {
                            map = new Hashtable<String, String>();
                            infoList = new Vector<String>();

                        } else if (tagName.equals("arsId")) {
                            kindItem = 1;
                        } else if (tagName.equals("direction")) {
                            kindItem = 2;
                        } else if (tagName.equals("lastTm")) {
                            kindItem = 3;
                        } else if (tagName.equals("beginTm")){
                            kindItem = 4;
                        }
                        break;
                    case XmlPullParser.END_TAG:
                        lastTm = "";
                        direction = "";
                        arsId = "";
                        tagName = "";
                        beginTm = "";
                        kindItem = -1;
                        tagName = parser.getName();
                        if (tagName != null) tagName = tagName.trim();

                        if (tagName.equals("itemList")) {
                            list.add(map); // 리스트의 1개의 인덱스마다 모든 것들이 저장됨
                            String id = map.get("arsId");
                            if(id == null) id="0";
                            infoMap.put(id, infoList);
                        } else {
                        }
                        break;
                    case XmlPullParser.TEXT:
                        if (kindItem == 1) {
                            arsId = parser.getText();
                            if (arsId != null) arsId = arsId.trim();
                            if (arsId.length() != 0) {
                                map.put("arsId", arsId);
                            }
                        } else if (kindItem == 2) {
                            direction = parser.getText();
                            if (direction != null) direction = direction.trim();
                            if (direction.length() != 0) {
                                infoList.add(0, direction);
                            }
                        } else if (kindItem == 3) {
                            lastTm = parser.getText();
                            if (lastTm != null) lastTm = lastTm.trim();
                            if (lastTm.length() != 0) {
                                infoList.add(1, lastTm);
                            }
                        } else if(kindItem == 4){
                            beginTm = parser.getText();
                            if(beginTm != null) beginTm = beginTm.trim();
                            if(beginTm.length() != 0){
                                infoList.add(beginTm);
                            }
                        } else {

                        }
                        break;
                }
                eventType = parser.next();
            }
        } catch (Exception e) {
            subA.log("getAPI-parse: "+  e.toString());
        }
    }
}

