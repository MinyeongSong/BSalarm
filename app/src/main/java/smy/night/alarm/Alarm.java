package smy.night.alarm;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class Alarm extends Activity implements AdapterView.OnItemSelectedListener, View.OnClickListener{
    String time, line, name, dir;
    String nowT;
    String[] setTime = {"10분 전", "20분 전", "30분 전", "40분 전", "50분 전", "60분 전", "직접입력"};
    String alarmT;
    Button btnA, cancel;
    Intent intentA, intent;
    PendingIntent pending;
    EditText min;
    AlarmManager AM;
    int hourL, minL, hourT;
    TextView alarmtxt, tmin;
    private int j;
    Spinner st;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm);
        btnA = (Button)findViewById(R.id.btnA);
        btnA.setOnClickListener(this);
        cancel = (Button)findViewById(R.id.cancel);
        cancel.setOnClickListener(this);
        min = (EditText)findViewById(R.id.min);
        //min.setOnClickListener(this);
        tmin = (TextView)findViewById(R.id.tmin);
        alarmtxt = (TextView)findViewById(R.id.alarmtxt);


        //막차시간 받아옴
        Intent intentT = getIntent();
        if(intentT.getAction() == "set"){
            btnA.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
        }else if(intentT.getAction() == "noti"){
            btnA.setVisibility(View.GONE);
            cancel.setVisibility(View.VISIBLE);
        }
        time=intentT.getStringExtra("time");
        line = intentT.getStringExtra("line");
        name = intentT.getStringExtra("name");
        dir = intentT.getStringExtra("dir");
        alarmtxt.setText(name+"\n"+line+"\n방향 "+dir+"\n막차시간 "+time);
        //스피너 설정
        st = (Spinner)findViewById(R.id.spt);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, android.R.layout.simple_spinner_dropdown_item, setTime);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        st.setPrompt("알람시간 설정");
        st.setAdapter(adapter);
        st.setOnItemSelectedListener(this);
        AM = (AlarmManager) getSystemService(ALARM_SERVICE);
        intentA = new Intent(getApplicationContext(), AlarmReceive.class);
        pending = PendingIntent.getBroadcast(Alarm.this, 0, intentA, 0);

    }
    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnA:
                int sec1 = second();
                if(sec1<0) {
                    showMsg("막차운행이 종료되었습니다.");
                    finish();
                }else {
                    if((sec1 / 3600)==0){
                        showMsg((sec1 % 3600 / 60) + "분 " + (sec1 % 3600 % 60) + "초 후 알람이 울립니다.");
                    }else{
                        showMsg((sec1 / 3600) + "시간 " + (sec1 % 3600 / 60) + "분 " + (sec1 % 3600 % 60) + "초 후 알람이 울립니다.");
                    }

                    //알람매니져
                    Date t = new Date();
                    t.setTime(System.currentTimeMillis() + sec1 * 1000);
                    AM.set(AlarmManager.RTC_WAKEUP, t.getTime(), pending);
                    //알림창 띄우기
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    Intent intent = new Intent(Alarm.this, Alarm.class);
                    intent.setAction("noti");
                    intent.putExtra("name",name);
                    intent.putExtra("line", line);
                    intent.putExtra("dir", dir);
                    intent.putExtra("time", time);
                    PendingIntent pendingIntent = PendingIntent.getActivity(Alarm.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                    Notification.Builder builder = new Notification.Builder(Alarm.this);
                    builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.alarm));
                    builder.setSmallIcon(R.drawable.alarmset);
                    builder.setTicker("막차알람");
                    builder.setContentTitle("막차알람이 설정되었습니다.");
                    builder.setContentText("막차시간 : " + hourL + "시 " + minL + "분\n알람을 취소하려면 클릭하세요.");
                    builder.setWhen(System.currentTimeMillis());
                    builder.setContentIntent(pendingIntent);
                    builder.setOngoing(true);
                    notificationManager.notify(123, builder.build());
                    btnA.setVisibility(View.GONE);
                    cancel.setVisibility(View.VISIBLE);
                    break;
                }
            case R.id.cancel:
                AM.cancel(pending);
                showMsg("알람이 취소되었습니다.");
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(123);
                btnA.setVisibility(View.VISIBLE);
                cancel.setVisibility(View.GONE);
                finish();
                break;

        }
    }
    private void showMsg(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        alarmT = setTime[i];
        if(alarmT.equals("10분 전")) alarmT="10";
        else if(alarmT.equals("20분 전")) alarmT = "20";
        else if(alarmT.equals("30분 전")) alarmT = "30";
        else if(alarmT.equals("40분 전")) alarmT = "40";
        else if(alarmT.equals("50분 전")) alarmT = "50";
        else if(alarmT.equals("60분 전")) alarmT = "60";
        else if(alarmT.equals("직접입력")) {
            min.setVisibility(View.VISIBLE);
            tmin.setVisibility(View.VISIBLE);
            st.setVisibility(View.INVISIBLE);
            j=1227;
        }
    }
    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
    //현재시간 받아오기
    public static String GetCurrentTime(){
        String localT;
        Calendar calendar = Calendar.getInstance();
        localT = String.format("%02d:%02d:%02d", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND));
        return localT;
    }
    public int second(){
        //막차시간
        String b[]= new String[3];
        b=time.split(":");
        int hour = Integer.parseInt(b[0]);
        if(hour<=3) hourL = hour + 24;// 00시 24로 변환
        else hourL=hour;
        int hourL1 = hourL*60*60;
        minL = Integer.parseInt(b[1]);
        int minL1 = minL*60;
        int secL1 = Integer.parseInt(b[2]);
        int secL = hourL1 + minL1 + secL1;//막차시간 초
        //현재시간
        nowT = GetCurrentTime().toString();
        String a[] = new String[3];
        a = nowT.split(":");
        int hourN = Integer.parseInt(a[0]);
        if(hourN<=3) hourT = hourN + 24;// 00시 24로 변환
        else hourT=hourN;
        int hourT1 = hourT*60*60;
        int minT = Integer.parseInt(a[1]);
        int minT1 = minT*60;
        int secT1 = Integer.parseInt(a[2]);

        if(j==1227){
            try{
                alarmT = min.getText().toString();
            }catch (NumberFormatException ne){showMsg("다시 입력해주세요");}
        }

        int secT = hourT1 + minT1 + secT1;//현재시간 초
        int minLT = secL-secT;//현재시간와 막차시간 차이 초
        int minA = Integer.parseInt(alarmT)*60;
        int sec = minLT - minA;//알람시간까지 남은 초
        return sec;
    }
}
