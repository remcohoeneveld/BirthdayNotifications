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
import android.widget.Toast;

import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.Date;

import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.UniqueIDHelper;
import nl.remcohoeneveld.birthdaynotifications.MainActivity;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;

public class BirthdayReceiver extends BroadcastReceiver {

    private static final String TAG = "BROADCASTRECEIVER";
    private String userID;
    DatabaseHelper databaseHelper;
    private Context mContext;
    PendingIntent contentIntent;
    private Notification noti;
    private NotificationManager nm;

    @SuppressLint("WakelockTimeout")
    @Override
    public void onReceive(Context context, Intent intent) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        assert pm != null;
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "");
        wl.acquire();
        FirebaseApp.initializeApp(context);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        databaseHelper = new DatabaseHelper(context);
        nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        mContext = context;

        Cursor data = databaseHelper.getData();

        while (data.moveToNext()){
            // get the userID
            userID = data.getString(1);
        }
        Log.d(TAG,userID);

        Toast.makeText(context, userID, Toast.LENGTH_SHORT).show();

        database.getReference("users/" + userID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Birthday birthday = snapshot.getValue(Birthday.class);

                    if (birthday != null) {
                        (new CheckBirthdayTask()).execute(birthday);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {

            }
        });

        wl.release();
    }

    public void setBirthdayReceiver(Context context)
    {
        AlarmManager am =( AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, BirthdayReceiver.class);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        assert am != null;
        am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000 * 60 * 10, pi); // Millisec * Second * Minute
    }

    public void cancelBirthdayReceiver(Context context)
    {
        Intent intent = new Intent(context, BirthdayReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.cancel(sender);
    }


    public static int getAge(Date dateOfBirth) {
        Calendar today = Calendar.getInstance();
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(dateOfBirth);
        if (birthDate.after(today)) {
            throw new IllegalArgumentException("You don't exist yet");
        }
        int todayYear = today.get(Calendar.YEAR);
        int birthDateYear = birthDate.get(Calendar.YEAR);
        int todayDayOfYear = today.get(Calendar.DAY_OF_YEAR);
        int birthDateDayOfYear = birthDate.get(Calendar.DAY_OF_YEAR);
        int todayMonth = today.get(Calendar.MONTH);
        int birthDateMonth = birthDate.get(Calendar.MONTH);
        int todayDayOfMonth = today.get(Calendar.DAY_OF_MONTH);
        int birthDateDayOfMonth = birthDate.get(Calendar.DAY_OF_MONTH);
        int age = todayYear - birthDateYear;

        // If birth date is greater than todays date (after 2 days adjustment of leap year) then decrement age one year
        if ((birthDateDayOfYear - todayDayOfYear > 3) || (birthDateMonth > todayMonth)) {
            age--;

            // If birth date and todays date are of same month and birth day of month is greater than todays day of month then decrement age
        } else if ((birthDateMonth == todayMonth) && (birthDateDayOfMonth > todayDayOfMonth)) {
            age--;
        }
        return age;
    }

    @SuppressLint("StaticFieldLeak")
    private class CheckBirthdayTask extends AsyncTask<Birthday, Integer, String>
    {
        private String TAG = getClass().getSimpleName();

        protected void onPreExecute (){
            super.onPreExecute();
            Log.d(TAG + " PreExceute","On pre Exceute......");
        }

        protected String doInBackground(Birthday...birthdays) {
            Log.d(TAG + " DoINBackGround","On doInBackground...");

            int count = birthdays.length;

            for (Birthday bday : birthdays){
                if (SameDateHelper.initializeSamedate(bday.getDate_of_birth())) {
                    // here to notification must be added!

                    try {
                        Integer age = getAge(bday.date_of_birth);
                        createNotification( bday.nickname + " birthday!" , bday.full_name + " has now turned " + age.toString() , mContext);

                        Intent notificationIntent = new Intent(mContext.getApplicationContext(), MainActivity.class);
                        contentIntent = PendingIntent.getActivity(mContext.getApplicationContext(),
                                0, notificationIntent,
                                PendingIntent.FLAG_CANCEL_CURRENT);

                        String message = "Congratulate " + bday.full_name + "!";

                        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show();

                    } catch (Throwable t){
                        t.printStackTrace();
                    }

                }
            }

            return "You are at PostExecute";
        }

        private void createNotification(String contentTitle, String contentText,Context context) {

            Log.d("createNotification", "title is [" + contentTitle + "]");

            nm = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
            //Build the notification using Notification.Builder
            Notification.Builder builder = new Notification.Builder(mContext)
                    .setSmallIcon(android.R.drawable.btn_star)
                    .setAutoCancel(true)
                    .setContentTitle(contentTitle)
                    .setContentText(contentText);

            //Show the notification
            nm.notify(UniqueIDHelper.createUniqueId(), builder.build());
        }

        protected void onProgressUpdate(Integer...a){
            super.onProgressUpdate(a);
            Log.d(TAG + " onProgressUpdate", "You are in progress update ... " + a[0]);
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG + " onPostExecute", "" + result);
        }
    }
}
