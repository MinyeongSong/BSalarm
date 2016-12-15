package smy.night.alarm;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListViewAdapter1 extends BaseAdapter{
    int i;
    private ArrayList<ListItem> listViewItem1List = new ArrayList<ListItem>();
    public ListViewAdapter1(){
    }
    @Override
    public int getCount() {
        return listViewItem1List.size();
    }

    @Override
    public Object getItem(int position) {
        return listViewItem1List.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        final int pos= position;
        final Context context = parent.getContext();
        if(view == null){
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inflater.inflate(R.layout.listview1, parent, false);
        }
        //ImageView iconImageView = (ImageView) view.findViewById(R.id.iv1) ;
        TextView lastNmTextView = (TextView) view.findViewById(R.id.text1) ;
        TextView leftTmTextView = (TextView) view.findViewById(R.id.text2) ;
        ListItem listViewItem = listViewItem1List.get(position);

        //iconImageView.setImageDrawable(listViewItem1.getTime());
        lastNmTextView.setText(listViewItem.getLastNm());
        leftTmTextView.setText(listViewItem.getLeftTm());

        return view;
    }
    public void addItem(String station, String time){
        ListItem item = new ListItem();
        item.setLastNm(station+"행");
        item.setLeftTm(time);
        listViewItem1List.add(item);
    }
    public void remove(){
        listViewItem1List.clear();
    }
}
