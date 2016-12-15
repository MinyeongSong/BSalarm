package smy.night.alarm;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

    public MySQLiteOpenHelper(Context context, String name, CursorFactory factory, int version){
        super(context, name, factory, version);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "create table bookmark(" +
                "arsId text primary key, " +
                "name text, " +
                "line text);";
        db.execSQL(sql);
        // sql문 실행
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // DB버전이 업그레이드 되었을 때 실행되는 메소드
        String sql = "drop table if exists bookmark";
        db.execSQL(sql);

        onCreate(db);
    }
}
