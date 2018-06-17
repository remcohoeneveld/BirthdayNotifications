package nl.remcohoeneveld.birthdaynotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ERROR: ";
    NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creating a new notification with the channelID of the application
        notification = new NotificationCompat.Builder(this, "BirthdayNotifications");
        notification.setAutoCancel(true);

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("message");

        // setting the firebase value text
        myRef.setValue("Wish Remco a Happy birthday!");


        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                // getting the value out of the dataSnapshot (firebase)
                String value = dataSnapshot.getValue(String.class);

                // getting the text by id (textContainer)
                TextView text = findViewById(R.id.textContainer);

                // adding the value to the text
                text.setText(value);

                // Calling the sendNotification function to send a notification with the value
                sendNotification(value, System.currentTimeMillis());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value so give this error
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });
    }

    public void sendNotification(String value, Long time){
        notification.setSmallIcon(R.drawable.ic_stat_bdaycake);
        // for people with accessibility services the ticker (setTicker() will be audibly announced)
        notification.setTicker("There is a birthday notification");
        // when is the notification shown
        notification.setWhen(time);
        // the content title of the notification
        notification.setContentTitle("Notification!");
        // the content text of the notification
        notification.setContentText(value);

        // creating a random integer for the Unique identifier
        Random rand = new Random();

        // getting a random integer
        int  n = rand.nextInt(50) + 1;

        // creating the notifcation intent
        Intent intent = new Intent(this,MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        // building the notifcation manager
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // notifying the notifcation manager (checking of the nm != null)
        assert nm != null;
        nm.notify(n, notification.build());
    }


}
