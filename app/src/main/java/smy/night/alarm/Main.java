package smy.night.alarm;

import android.app.Activity;
import android.app.ActivityGroup;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TabHost;

public class Main extends ActivityGroup implements TabHost.OnTabChangeListener {
    Button subway, bus;
    Intent select_bus, select_sub, select_idx;
    private BackPressCloseHandler backPressCloseHandler;
    BookMarkAdapter adapter;
    private static final String TAG = "Main";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        adapter = new BookMarkAdapter();
        backPressCloseHandler = new BackPressCloseHandler(this);

        select_bus = new Intent(getApplicationContext(), MainActivity.class);
        select_sub = new Intent(getApplicationContext(), MainActivityS.class);
        select_idx = new Intent(getApplicationContext(), BookMarkActivity.class);
        select_idx.setAction("bookmark");

        TabHost tabHost = (TabHost) findViewById(R.id.tab_host);
        tabHost.setup(getLocalActivityManager());

        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("즐겨찾기")
                .setContent(select_idx.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)));
        tabHost.setOnTabChangedListener(this);
        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("지하철")
                .setContent(select_sub));
        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("버스")
                .setContent(select_bus));
        tabHost.setCurrentTab(0);
    }
    @Override
    public void onBackPressed(){
        backPressCloseHandler.onBackPressed();

    }

    @Override
    public void onTabChanged(String s) {

    }
}
