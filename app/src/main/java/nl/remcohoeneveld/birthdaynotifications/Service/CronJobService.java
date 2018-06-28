package nl.remcohoeneveld.birthdaynotifications.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import nl.remcohoeneveld.birthdaynotifications.BroadcastReceiver.BirthdayReceiver;

@SuppressLint("Registered")
public class CronJobService extends Service {

    // add the birthdayreceiver to the service to send the notifications in the background
    BirthdayReceiver birthdayReceiver = new BirthdayReceiver();
    public void onCreate()
    {
        super.onCreate();
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
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }


}
