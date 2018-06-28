package nl.remcohoeneveld.birthdaynotifications.BroadcastReceiver;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nl.remcohoeneveld.birthdaynotifications.Helper.AgeHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.LoginActivity;
import nl.remcohoeneveld.birthdaynotifications.MainActivity;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;
import nl.remcohoeneveld.birthdaynotifications.R;
import nl.remcohoeneveld.birthdaynotifications.Service.CronJobService;

public class BirthdayReceiver extends BroadcastReceiver {

    public static final String TAG = "BirthdayReceiver";

    private String userID;
    private Context mContext;
    private NotificationManager nm;

    PendingIntent contentIntent;
    DatabaseHelper databaseHelper;

    @SuppressLint("WakelockTimeout")
    @Override
    public void onReceive(Context context, final Intent intent) {
        // get the powerManager
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        //wake up the device
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        //what to do when awake
        FirebaseApp.initializeApp(context);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseHelper = new DatabaseHelper(context);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        //set the context (we shall use this later on)
        mContext = context;

        // Get the data from the database as a Cursor object
        Cursor data = databaseHelper.getData();

        // get the known userID from the database
        while (data.moveToNext()) {
            // get the userID
            userID = data.getString(1);
        }

        // if a UserID is found
        if (userID != null) {
            //close the database
            databaseHelper.close();

            //get the database reference from the userID that was found
            try {
                database.getReference("users/" + userID).addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Birthday birthday = snapshot.getValue(Birthday.class);
                            if (birthday != null) {
                                // Don't use CheckBirthDayTask in MainActivity
                                if (isMyServiceRunning(CronJobService.class)) {
                                    (new CheckBirthdayTask()).execute(birthday);
                                } else {
                                    Log.d(TAG, "Service is not running anymore.");
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } catch (Exception e){
                System.out.println(context.getString(R.string.exception_error));
            }
        }

        wl.release();
    }

    public void setBirthdayReceiver(Context context) {
        // create an alarm manager from alarm service
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, BirthdayReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        assert am != null;

        //// Millisec * Second * Minute * Hour

        // Repeat once per 1 minute!
        //am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60, pi);

        // Repeat once per day!
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, pi);
    }

    public void cancelBirthdayReceiver(Context context) {
        //cancel all outgoing alarms
        Intent intent = new Intent(context, BirthdayReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.cancel(sender);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckBirthdayTask extends AsyncTask<Birthday, Integer, String> {
        private String TAG = getClass().getSimpleName();

        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG + " PreExceute", "On pre Exceute......");
        }

        protected String doInBackground(Birthday... birthdays) {
            Log.d(TAG + " DoINBackGround", "On doInBackground...");

            // for the birthdays that occured create a notification from the birthday object
            for (Birthday bday : birthdays) {
                if (SameDateHelper.initializeSamedate(bday.getDate_of_birth())) {
                    try {
                        // get the age variable for the notification
                        Integer age = AgeHelper.getAge(bday.date_of_birth);
                        createNotification(bday.nickname + " birthday!", bday.full_name + " has now turned " + age.toString(), bday.notificationID, mContext);

                        Intent notificationIntent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                        contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(),
                                0, notificationIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);

                    } catch (Throwable t) {
                        t.printStackTrace();
                    }

                }
            }

            return "You are at PostExecute";
        }

        // create notification function
        private void createNotification(String contentTitle, String contentText, Integer notificationID, Context context) {

            Log.d("createNotification", "title is [" + contentTitle + "]");

            Intent myintent = new Intent(context, LoginActivity.class);
            myintent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                    Intent.FLAG_ACTIVITY_NEW_TASK);

            int randomNumber = (int) (Math.random() * 9000) + 1000;
            PendingIntent conIntent = PendingIntent.getActivity(context, randomNumber,
                    myintent, PendingIntent.FLAG_UPDATE_CURRENT);

            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(R.drawable.ic_birthday_cake)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText)
                    .setContentIntent(conIntent);

            //Show the notification
            nm.notify(notificationID, builder.build());
        }

        protected void onProgressUpdate(Integer... a) {
            super.onProgressUpdate(a);
            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG + " onPostExecute", "" + result);
        }
    }

    // check if service is running (if service is stopped don't call the async task (notifications))
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
