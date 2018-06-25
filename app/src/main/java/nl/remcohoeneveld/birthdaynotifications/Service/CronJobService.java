package nl.remcohoeneveld.birthdaynotifications.Service;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import nl.remcohoeneveld.birthdaynotifications.BroadcastReceiver.BirthdayReceiver;

@SuppressLint("Registered")
public class CronJobService extends Service {

    BirthdayReceiver birthdayReceiver = new BirthdayReceiver();

    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        birthdayReceiver.setBirthdayReceiver(this);
        return START_STICKY;
    }

    @Override
    public void onStart(Intent intent, int startId)
    {
        birthdayReceiver.setBirthdayReceiver(this);
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

}
