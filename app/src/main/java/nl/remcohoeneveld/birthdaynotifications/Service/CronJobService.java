package nl.remcohoeneveld.birthdaynotifications.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import nl.remcohoeneveld.birthdaynotifications.BroadcastReceiver.BirthdayReceiver;

@SuppressLint("Registered")
public class CronJobService extends Service {


    private boolean isRunning = false;
    private static final String TAG = "CronJobService";
    // add the birthdayreceiver to the service to send the notifications in the background
    BirthdayReceiver birthdayReceiver = new BirthdayReceiver();
    public void onCreate()
    {
        super.onCreate();
        isRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        // set the birthdayreceiver on the start commando
        birthdayReceiver.setBirthdayReceiver(this);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        birthdayReceiver.cancelBirthdayReceiver(this);
        isRunning = false;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


}
