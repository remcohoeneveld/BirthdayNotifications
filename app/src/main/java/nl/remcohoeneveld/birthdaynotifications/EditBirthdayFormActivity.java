package nl.remcohoeneveld.birthdaynotifications;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import nl.remcohoeneveld.birthdaynotifications.Helper.NetworkHelper;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;

public class EditBirthdayFormActivity extends AppCompatActivity {

    private EditText mFullnameView;
    private EditText mNicknameView;
    private EditText mBirthdayView;
    private FirebaseDatabase database;
    private FirebaseUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_birthday_form);

        // Write a message to the database
        database = FirebaseDatabase.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();

        // set the textviews by id
        mFullnameView = findViewById(R.id.fullname);
        mNicknameView = findViewById(R.id.nickname);
        mBirthdayView = findViewById(R.id.birthdate);

        // get the current birthday data
        Intent intent = getIntent();
        String nick = intent.getStringExtra("nickname");
        String full = intent.getStringExtra("full_name");
        String bday = intent.getStringExtra("date_of_birth");

        // set the textview to the current birthday
        mFullnameView.setText(full);
        mNicknameView.setText(nick);
        mBirthdayView.setText(bday);

        if (NetworkHelper.initializeNetworkHelper(this)) {

            Button mMessageButton = findViewById(R.id.edit_birthday_button);
            mMessageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    userData();
                }
            });

        } else {
            finish();
        }
    }

    private void userData() {
        if (user != null) {
            // Reset errors.
            mFullnameView.setError(null);
            mNicknameView.setError(null);
            mBirthdayView.setError(null);


            // the fullname nickname and birthday
            String fullName = mFullnameView.getText().toString();
            String nickname = mNicknameView.getText().toString();
            String birthDay = mBirthdayView.getText().toString();

            boolean cancel = false;
            View focusView = null;

            // Check for a valid password, if the user entered one.
            if (TextUtils.isEmpty(fullName)) {
                mFullnameView.setError(getString(R.string.error_invalid_name));
                focusView = mFullnameView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(nickname)) {
                mNicknameView.setError(getString(R.string.error_invalid_name));
                focusView = mNicknameView;
                cancel = true;
            }

            // Check for a valid email address.
            if (TextUtils.isEmpty(birthDay)) {
                mBirthdayView.setError(getString(R.string.error_invalid_date));
                focusView = mBirthdayView;
                cancel = true;
            }

            if (cancel) {
                // There was an error; don't attempt login and focus the first
                // form field with an error.
                focusView.requestFocus();
            } else {
                try {
                    UpdateUserData(fullName, nickname, birthDay);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        } else {
            finish();
        }
    }

    public void UpdateUserData(String fullName, String nickname, String birthday) throws ParseException {

        //get the firebase data
        String userId = user.getUid();

        Intent intent = getIntent();
        String key = intent.getStringExtra("key");

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        Date date = dateFormat.parse(birthday);

        database.getReference("users/" + userId + "/" + key).setValue(new Birthday(date, fullName, nickname));
        database.getReference("users/" + userId + "/" + key).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                setResult(Activity.RESULT_OK,
                        new Intent().putExtra("editSuccessMessage", getString(R.string.edit_succes_message)));
                finish();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                setResult(Activity.RESULT_CANCELED,
                        new Intent());
                finish();
            }
        });
    }
}
