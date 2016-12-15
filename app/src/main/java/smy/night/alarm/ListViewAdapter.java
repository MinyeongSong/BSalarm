package smy.night.alarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ListViewAdapter extends BaseAdapter implements View.OnClickListener{
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<ListItem> listViewItemList = new ArrayList<ListItem>() ;

    @Override
    public void onClick(View view) {
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick(
                    (String)view.getTag(R.id.subCode),
                    (String)view.getTag(R.id.subStName),
                    (String)view.getTag(R.id.subLine)) ;
        }
    }

    public interface ListBtnClickListener {
        void onListBtnClick(String stCode, String stName, String lineName);
    }
    private ListBtnClickListener listBtnClickListener ;
    // ListViewAdapter의 생성자
    public ListViewAdapter(ListBtnClickListener clickListener) {
        this.listBtnClickListener = clickListener;
    }


    // Adapter에 사용되는 데이터의 개수를 리턴. : 필수 구현
    @Override
    public int getCount() {
        return listViewItemList.size() ;
    }

    // position에 위치한 데이터를 화면에 출력하는데 사용될 View를 리턴. : 필수 구현
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.listview, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        //ImageView iconImageView = (ImageView) convertView.findViewById(R.id.imageView1) ;
        TextView staionNmTextView = (TextView) convertView.findViewById(R.id.text2) ;
        TextView lineNmTextView = (TextView) convertView.findViewById(R.id.text1) ;
        //TextView stationCdTextView = (TextView) convertView.findViewById(R.id.textView3) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        ListItem listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영
        //iconImageView.setImageDrawable(listViewItem.getIcon());
        staionNmTextView.setText(listViewItem.getStationNm()+" / "+listViewItem.getStationCd());
        //stationCdTextView.setText(listViewItem.getStationCd());
        lineNmTextView.setText(listViewItem.getLineNm());

        Button idxg = (Button) convertView.findViewById(R.id.idxg);
        idxg.setTag(R.id.position, position);
        idxg.setTag(R.id.subCode, listViewItem.getStationCd());
        idxg.setTag(R.id.subLine, listViewItem.getLineNm());
        idxg.setTag(R.id.subStName, listViewItem.getStationNm());
        idxg.setOnClickListener(this);

        return convertView;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    // 지정한 위치(position)에 있는 데이터 리턴 : 필수 구현
    @Override
    public Object getItem(int position) {
        return listViewItemList.get(position) ;
    }

    // 아이템 데이터 추가를 위한 함수. 개발자가 원하는대로 작성 가능.
    public void addItem(String code, String name, String line) {
        ListItem item = new ListItem();

        item.setStationCd(code);
        item.setStaionNm(name);
        if(line.equals("K")){
            item.setLineNm("경의중앙선");
        }else if(line.equals("G")){
            item.setLineNm("경춘선");
        }
        else if(line.equals("S")){
            item.setLineNm("신분당선");
        }
        else if(line.equals("A")){
            item.setLineNm("공항철도");
        }
        else if(line.equals("B")){
            item.setLineNm("분당선");
        }
        else if(line.equals("I")){
            item.setLineNm("인천1호선");
        }
        else if(line.equals("SU")){
            item.setLineNm("수인선");
        }
        else if(line.equals("E")){
            item.setLineNm("에버라인");
        }else if(line.equals("U")){
            item.setLineNm("의정부선");
        }else{
            item.setLineNm(line+"호선");
        }


        listViewItemList.add(item);
    }
    public void remove(){
        listViewItemList.clear();
    }
    }