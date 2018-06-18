package nl.remcohoeneveld.birthdaynotifications;

import android.app.Activity;
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
    private DatabaseReference myRef;
    private FirebaseUser user;
    NotificationCompat.Builder notification;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_birthday);

        // creating a new notification with the channelID of the application
        notification = new NotificationCompat.Builder(this, "BirthdayNotifications");
        notification.setAutoCancel(true);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();

        user = FirebaseAuth.getInstance().getCurrentUser();

        mFullnameView = findViewById(R.id.fullname);
        mNicknameView = findViewById(R.id.nickname);
        mBirthdayView = findViewById(R.id.birthdate);
        if (NetworkHelper.initializeNetworkHelper(this)) {

            Button mMessageButton = findViewById(R.id.add_birthday_button);
            mMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (user != null) {
                        String uid = user.getUid();
                        String email = user.getEmail();

                        myRef = database.getReference("users/" + uid);
                        String key = myRef.push().getKey();

                        String fullName = mFullnameView.getText().toString();
                        String nickname = mNicknameView.getText().toString();
                        Date birthDay = new Date(mBirthdayView.getText().toString());

                        writeUserData(uid, key, fullName, nickname, birthDay);

                    } else {
                        Intent intent = new Intent(AddBirthdayActivity.this, MainActivity.class);
                        startActivity(intent);
                    }
                }
            });

        } else {
            Intent intent = new Intent(AddBirthdayActivity.this, LoginActivity.class);
            startActivity(intent);
        }
    }


    public void writeUserData(String userId, String key, String fullName, String nickname, Date birthday) {
        database.getReference("users/" + userId + "/" + key).setValue(new Birthday(birthday.toString(), fullName, nickname));

        database.getReference("users/" + userId + "/" + key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                setResult(Activity.RESULT_OK,
                        new Intent().putExtra("addSuccessMessage", getString(R.string.add_success_message)));
                finish();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                setResult(Activity.RESULT_CANCELED,
                        new Intent().putExtra("addErrorMessage", getString(R.string.add_error_message)));
                finish();
            }
        });
    }
}