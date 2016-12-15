package smy.night.alarm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class BookMarkAdapter extends BaseAdapter {

    private ArrayList<BookMarkItem> itemList = new ArrayList<BookMarkItem>() ;

    @Override
    public int getCount() {return itemList.size();}

    @Override
    public Object getItem(int position) {
        return itemList.get(position) ;
    }

    // 지정한 위치(position)에 있는 데이터와 관계된 아이템(row)의 ID를 리턴. : 필수 구현
    @Override
    public long getItemId(int position) {
        return position ;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final int pos = position;
        final Context context = parent.getContext();

        // "listview_item" Layout을 inflate하여 convertView 참조 획득.
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.bookmark_item, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView stationNameTextView = (TextView) convertView.findViewById(R.id.bookmark_tv1) ;
        TextView arsIdTextView = (TextView) convertView.findViewById(R.id.bookmark_tv2) ;
        TextView lineTextView = (TextView) convertView.findViewById(R.id.bookmark_tv3) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        BookMarkItem listViewItem = itemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영

        stationNameTextView.setText(listViewItem.getName());
        arsIdTextView.setText(listViewItem.getArsId());
        lineTextView.setText(listViewItem.getLine());

        return convertView;
    }
    public void addItem(String stationName, String arsId, String line) {
        BookMarkItem item = new BookMarkItem();

        item.setName(stationName);
        item.setArsId(arsId);
        item.setLine(line);

        itemList.add(item);
    }
}

