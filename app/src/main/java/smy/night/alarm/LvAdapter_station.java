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

        import java.util.ArrayList;

public class LvAdapter_station extends BaseAdapter implements View.OnClickListener{
    // Adapter에 추가된 데이터를 저장하기 위한 ArrayList
    private ArrayList<LvItem_station> listViewItemList = new ArrayList<LvItem_station>() ;

    @Override
    public void onClick(View view) {
        if (this.listBtnClickListener != null) {
            this.listBtnClickListener.onListBtnClick(
                    (int)view.getTag(R.id.position),
                    (String)view.getTag(R.id.arsId),
                    (String)view.getTag(R.id.stName),
                    (int)view.getTag(R.id.distance),
                    (Double)view.getTag(R.id.gpsX),
                    (Double)view.getTag(R.id.gpsY)) ;
        }
    }

    public interface ListBtnClickListener {
        void onListBtnClick(int position, String arsId, String name, int distance, Double gpsX, Double gpsY) ;
    }
    private ListBtnClickListener listBtnClickListener ;

    // ListViewAdapter의 생성자
    public LvAdapter_station(ListBtnClickListener clickListener) {
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
            convertView = inflater.inflate(R.layout.listview_station, parent, false);
        }

        // 화면에 표시될 View(Layout이 inflate된)으로부터 위젯에 대한 참조 획득
        TextView stationNameTextView = (TextView) convertView.findViewById(R.id.textView1) ;
        TextView arsIdTextView = (TextView) convertView.findViewById(R.id.textView3) ;
        TextView distanceTextView = (TextView) convertView.findViewById(R.id.textView2) ;

        // Data Set(listViewItemList)에서 position에 위치한 데이터 참조 획득
        LvItem_station listViewItem = listViewItemList.get(position);

        // 아이템 내 각 위젯에 데이터 반영

        stationNameTextView.setText(listViewItem.getStationName());
        arsIdTextView.setText(listViewItem.getArsId());
        distanceTextView.setText(listViewItem.getDistance() + "m");

        Button idxb = (Button) convertView.findViewById(R.id.idxb);
        idxb.setTag(R.id.position, position);
        idxb.setTag(R.id.arsId, listViewItem.getArsId());
        idxb.setTag(R.id.stName, listViewItem.getStationName());
        idxb.setTag(R.id.distance, listViewItem.getDistance());
        idxb.setTag(R.id.gpsX, listViewItem.getGpsX());
        idxb.setTag(R.id.gpsY, listViewItem.getGpsY());
        idxb.setOnClickListener(this);

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
    public void addItem(String stationName, String arsId, float distance, Double x, Double y) {
        LvItem_station item = new LvItem_station();

        item.setStationName(stationName);
        item.setArsId(arsId);
        item.setDistance(distance);
        item.setGpsX(x);
        item.setGpsY(y);

        listViewItemList.add(item);
    }
    public void remove(){
        listViewItemList.clear();
    }
}
