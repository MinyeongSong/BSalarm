package smy.night.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;


public class BookMarkActivity extends Activity {
    SQLiteDatabase db;
    MySQLiteOpenHelper helper;

    String name;
    String arsId;
    String lineName;
    int distance;
    Double stX;
    Double stY;

    String tempArsId;

    BookMarkAdapter adapter;
    ListView listView;
    String action;
    private BackPressCloseHandler backPressCloseHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bookmark_listview);
        helper = new MySQLiteOpenHelper(BookMarkActivity.this, "trans.db", null, 2);
        adapter = new BookMarkAdapter();
        listView = (ListView)findViewById(R.id.bookmark_listview);
        listView.setAdapter(adapter);
        backPressCloseHandler = new BackPressCloseHandler(this);


        getData(); // 즐겨찾기 버튼을 클릭했을 때 해당 리스트뷰의 데이터가 넘어옴
        if(action.equals("bookmark")){
            select();
        }else if(action.equals("star")){
            insert(name, arsId, "");
            finish();
        }else if(action.equals("sub_star")){
            insert(name, arsId, lineName);
            finish();
        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView parent, View v, int position, long id) {
                // get item
                BookMarkItem item = (BookMarkItem) parent.getItemAtPosition(position);

                String arsId = item.getArsId();
                String stationNm = item.getName();
                String line = item.getLine();

                if(line.equals("")){
                    Intent i = new Intent(BookMarkActivity.this, SubActivity.class);
                    i.setAction("nomap");
                    i.putExtra("arsId", arsId);
                    i.putExtra("stationNm", stationNm);
                    i.putExtra("stX", 0.0);
                    i.putExtra("stY", 0.0);
                    i.putExtra("gpsX", 0.0);
                    i.putExtra("gpsY", 0.0);
                    startActivity(i);
                }else{
                    Intent i = new Intent(BookMarkActivity.this, Search.class);
                    i.putExtra("code", arsId);
                    i.putExtra("name", stationNm);
                    i.putExtra("line", line);
                    startActivity(i);
                }
                // TODO : use item data.
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView parent, View view, int position, long id) {
                BookMarkItem item = (BookMarkItem) parent.getItemAtPosition(position);
                String arsId = item.getArsId();
                alertbox("북마크 삭제", "해당 북마크를 삭제하시겠습니까?", arsId);
                return true;
            }
        });
    }

    protected  void onResume(){
        super.onResume();
        adapter.notifyDataSetChanged();
    }

    private void getData(){
        Intent getIn = getIntent();
        action = getIn.getAction();
        if(action.equals("star")){
            this.arsId = getIn.getStringExtra("arsId");
            this.name = getIn.getStringExtra("name");
        }else if(action.equals("sub_star")){
            this.arsId = getIn.getStringExtra("stCode");
            this.name = getIn.getStringExtra("stName");
            this.lineName = getIn.getStringExtra("lineName");
        }

    }

    public void insert(String name, String arsId, String line){
        db = helper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put("name", name);
        values.put("arsId", arsId);
        values.put("line", line);
        try{
            db.insertOrThrow("bookmark", null, values);
            Toast.makeText(BookMarkActivity.this, "즐겨찾기에 추가되었습니다.", Toast.LENGTH_SHORT).show();
        }catch (SQLiteConstraintException sq){
            Toast.makeText(BookMarkActivity.this, "추가된 즐겨찾기입니다.", Toast.LENGTH_SHORT).show();
        }


        db.close();
    }
    public void delete(String arsId){
        db = helper.getWritableDatabase();
        db.delete("bookmark", "arsId=?", new String[]{arsId});
    }
    public void select(){
        db = helper.getReadableDatabase();
        Cursor cursor = db.query("bookmark", null, null, null, null, null, null);

        while(cursor.moveToNext()){
            String name = cursor.getString(cursor.getColumnIndex("name"));
            String arsId = cursor.getString(cursor.getColumnIndex("arsId"));
            String line = cursor.getString(cursor.getColumnIndex("line"));

            // 어댑터로 새로운 리스트뷰 만들어서 추가
            adapter.addItem(name, arsId, line);
            adapter.notifyDataSetChanged();

        }
        cursor.close();
        db.close();
    }

    protected void alertbox(String title, String mymessage, String arsId){
        this.tempArsId = arsId;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title)
                .setMessage(mymessage)
                .setCancelable(false)
                .setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    //  폰 위치 설정 페이지로 넘어감
                    public void onClick(DialogInterface dialog, int id) {
                        delete(tempArsId);
                        dialog.cancel();
                        Intent inten = new Intent(BookMarkActivity.this, Main.class);
                        startActivity(inten);
                        Toast.makeText(BookMarkActivity.this, "삭제가 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();

    }
}