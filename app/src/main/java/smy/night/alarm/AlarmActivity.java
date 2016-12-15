package smy.night.alarm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;

public class AlarmActivity extends Activity {
    Vibrator vibe;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.set_alarm);

        AlertDialog dialog = createDialogBox();
        dialog.show();
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        long[] pattern = {0,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500,2000,500};
        vibe.vibrate(pattern,-1);

    }
    private AlertDialog createDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("막차알람이 울립니다.");
        builder.setMessage("종료하시겠습니까?");
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setCancelable(false);

        builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                NotificationManager nm = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
                nm.cancel(123);
            vibe.cancel();
            finish();
            }
        });
        AlertDialog dialog = builder.create();
        return dialog;
    }
}