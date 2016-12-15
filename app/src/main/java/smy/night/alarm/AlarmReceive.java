package smy.night.alarm;
//알람을 받아서 새로운 액티비티를 실행시킴
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AlarmReceive extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intentR) {

        try {
            intentR = new Intent(context, AlarmActivity.class);
            PendingIntent pi = PendingIntent.getActivity(context, 0, intentR,
                    PendingIntent.FLAG_ONE_SHOT);

            pi.send();

        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }
}
