package nl.remcohoeneveld.birthdaynotifications;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

public class AddBirthdayActivity extends AppCompatActivity {

    private static final String TAG = "ERROR: ";
    private EditText mFullnameView;
    private EditText mNicknameView;
    private EditText mBirthdayView;
    private FirebaseDatabase database;
    private FirebaseUser user;
    NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // creating a new notification with the channelID of the application
        notification = new NotificationCompat.Builder(this, "BirthdayNotifications");
        notification.setAutoCancel(true);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        //myRef = database.getReference("message");

        user = FirebaseAuth.getInstance().getCurrentUser();

        mFullnameView = findViewById(R.id.fullname);
        mNicknameView = findViewById(R.id.nickname);
        mBirthdayView = findViewById(R.id.birthdate);

        Button mMessageButton = findViewById(R.id.add_birthday_button);
        mMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (user != null) {
                    String uid = user.getUid();
                    String email = user.getEmail();

                    String fullName = mFullnameView.getText().toString();
                    String nickname = mNicknameView.getText().toString();
                    Date birthDay =  new Date(mBirthdayView.getText().toString());

                    writeUserData(uid,email,fullName,nickname,birthDay);
                }
            }
        });

        if (NetworkHelper.initializeNetworkHelper(this)) {
            if (user != null) {
                // Name, email address, and profile photo Url
                String name = user.getDisplayName();
                String email = user.getEmail();
                Uri photoUrl = user.getPhotoUrl();

                // Check if user's email is verified
                boolean emailVerified = user.isEmailVerified();

                // The user's ID, unique to the Firebase project. Do NOT use this value to
                // authenticate with your backend server, if you have onremcoe. Use
                // FirebaseUser.getToken() instead.
                String uid = user.getUid();


                Log.v(TAG, "name=" + name);
                Log.v(TAG, "email=" + email);
                Log.v(TAG, "photoUrl=" + photoUrl);
                Log.v(TAG, "emailVerified=" + emailVerified);

                Toast.makeText(getApplicationContext(), "You signed in as " + email, Toast.LENGTH_SHORT).show();

            } else {
                Intent intent = new Intent(AddBirthdayActivity.this, MainActivity.class);
                startActivity(intent);
            }
        } else {
            Intent intent = new Intent(AddBirthdayActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }


    public void writeUserData(String userId, String email, String fullName, String nickname, Date birthday) {
        String key = database.getReference("users/" + userId).push().getKey();
        database.getReference("users/" + userId + "/" + key).setValue(new Birthday(birthday.toString(),fullName,nickname));

        Log.d("Added bday for : ",email);
    }


    public void sendNotification(String value, Long time) {
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
        int n = rand.nextInt(50) + 1;

        // creating the notifcation intent
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        // building the notifcation manager
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // notifying the notifcation manager (checking of the nm != null)
        assert nm != null;
        nm.notify(n, notification.build());
    }


}