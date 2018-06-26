package nl.remcohoeneveld.birthdaynotifications.BroadcastReceiver;

import android.annotation.SuppressLint;
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
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import nl.remcohoeneveld.birthdaynotifications.Helper.AgeHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.UniqueIDHelper;
import nl.remcohoeneveld.birthdaynotifications.MainActivity;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;

public class BirthdayReceiver extends BroadcastReceiver {

    private String userID;
    private Context mContext;
    private NotificationManager nm;

    PendingIntent contentIntent;
    DatabaseHelper databaseHelper;

    @SuppressLint("WakelockTimeout")
    @Override
    public void onReceive(Context context, final Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        FirebaseApp.initializeApp(context);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseHelper = new DatabaseHelper(context);
        nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        mContext = context;

        Cursor data = databaseHelper.getData();

        while (data.moveToNext()) {
            // get the userID
            userID = data.getString(1);
        }

        if (userID != null) {

            database.getReference("users/" + userID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        Birthday birthday = snapshot.getValue(Birthday.class);

                        String key = snapshot.getKey();

                        Log.d("KEY:", key);

                        if (birthday != null) {
                            // Don't use CheckBirthDayTask in MainActivity
                            if (MainActivity.instance == null) {
                                (new CheckBirthdayTask()).execute(birthday);
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {

                }
            });
        }

        wl.release();
    }

    public void setBirthdayReceiver(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, BirthdayReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        assert am != null;

        // Millisec * Second * Minute * Hour
        // Repeat once per day!
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 60 * 24, pi);
    }

    public void cancelBirthdayReceiver(Context context) {
        Intent intent = new Intent(context, BirthdayReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(sender);
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

            int count = birthdays.length;

            for (Birthday bday : birthdays) {
                if (SameDateHelper.initializeSamedate(bday.getDate_of_birth())) {
                    try {
                        // get the age variable for the notification
                        Integer age = AgeHelper.getAge(bday.date_of_birth);
                        createNotification(bday.nickname + " birthday!", bday.full_name + " has now turned " + age.toString(), mContext);

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

        private void createNotification(String contentTitle, String contentText, Context context) {

            Log.d("createNotification", "title is [" + contentTitle + "]");

            nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);

            //Show the notification
            nm.notify(UniqueIDHelper.createUniqueId(), builder.build());
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
}
