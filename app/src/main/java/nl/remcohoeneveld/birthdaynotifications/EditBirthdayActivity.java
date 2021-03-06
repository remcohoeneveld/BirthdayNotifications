package nl.remcohoeneveld.birthdaynotifications;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseListAdapter;
import com.firebase.ui.database.FirebaseListOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.Locale;

import nl.remcohoeneveld.birthdaynotifications.Helper.AgeHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;

public class EditBirthdayActivity extends AppCompatActivity {
    FirebaseListAdapter<Birthday> mAdapter;

    String userId;
    String bday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_birthday);

        // get database and user
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ListView birthdayRemoveList = findViewById(R.id.birthdayListEdit);
        // get the firebase user
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // add the databasehelper to add the userid everytime the user has logged in succesfully (for notifications)
        DatabaseHelper dbhelper = new DatabaseHelper(this);

        if (user != null) {
            //get the firebase data
            userId = user.getUid();

            try {
                dbhelper.addData(userId);
            } catch (Exception e) {
                System.out.println("Exception occurred");
            }

            Query query = database.getReference("users").child(userId);

            FirebaseListOptions<Birthday> options = new FirebaseListOptions.Builder<Birthday>()
                    .setQuery(query, Birthday.class)
                    .setLayout(R.layout.birthday_listview)
                    .build();

            mAdapter = new FirebaseListAdapter<Birthday>(options) {
                @Override
                protected void populateView(View v, Birthday birthday, int position) {
                    //set the text1 value to fullname of the person
                    ((TextView) v.findViewById(R.id.text1)).setText(birthday.getFull_name());

                    //convert the date of birth to the age
                    Integer age = AgeHelper.getAge(birthday.date_of_birth);
                    String ageMessage = age + " years old";
                    ((TextView) v.findViewById(R.id.text2)).setText(ageMessage);

                    // set the undertitle to the textview text2
                    TextView underTitle = v.findViewById(R.id.text3);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());

                    ((TextView) v.findViewById(R.id.text4)).setText(dateFormat.format(birthday.date_of_birth));

                    // if the date is the same as today then change the text to the birthday else just show the age
                    if (SameDateHelper.initializeSamedate(birthday.getDate_of_birth())) {
                        bday = "Today is the birthday of " + birthday.nickname;
                        underTitle.setTextColor(getResources().getColor(R.color.colorPrimaryDark));
                    } else {
                        bday = birthday.nickname;
                        underTitle.setTextColor(getResources().getColor(R.color.darkgrey));
                    }

                    underTitle.setText(bday);
                }
            };

            // set the adapter mAdapter from firebase
            birthdayRemoveList.setAdapter(mAdapter);
            birthdayRemoveList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected item text from ListView
                    Object listItem = birthdayRemoveList.getItemAtPosition(position);
                    try {
                        // get the uniqueID from the field (listitem)
                        Field fieldUniqueID = listItem.getClass().getDeclaredField("uniqueID");
                        Object uniqueID = fieldUniqueID.get(listItem);

                        // FOR EDITING AN ITEM OUT OF FIREBASE
                        // create a query that the UniqueID is equal to the uniqueID of the birthday

                        Query queryChild = database.getReference("users/" + userId).orderByChild("uniqueID").equalTo(uniqueID.toString());

                        // if the queryChild is clicked then remove the snapshotChild
                        queryChild.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                                    Birthday birthday = snapshotChild.getValue(Birthday.class);

                                    String key = snapshotChild.getKey();

                                    Intent intent = new Intent(getApplicationContext(), EditBirthdayFormActivity.class);
                                    intent.putExtra("key",key);

                                    if (birthday != null) {
                                        SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
                                        String date_of_birth = dateFormat.format(birthday.date_of_birth);

                                        intent.putExtra("full_name", birthday.full_name)
                                                .putExtra("nickname", birthday.nickname)
                                                .putExtra("date_of_birth", date_of_birth);
                                    }

                                    startActivityForResult(intent, 1000);

                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {
                                Log.e("TAG", "onCancelled", databaseError.toException());
                            }
                        });


                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }

                }
            });
        }

    }

    @Override
    protected void onStart() {
        // important to check if the adapter is listening otherwise the listview will be empty
        super.onStart();
        mAdapter.startListening();
    }

    @Override
    protected void onStop() {
        // important to check if the adapter has stopped listening on the stop
        super.onStop();
        mAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // showing a message when a new birthday is created succesfully and checking if the operation is not cancelled
        if (resultCode == Activity.RESULT_OK) {
            String addSuccessMessage = data.getStringExtra("editSuccessMessage");

            if (addSuccessMessage != null) {
                Toast.makeText(getApplicationContext(), addSuccessMessage, Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), getString(R.string.add_error_message), Toast.LENGTH_SHORT).show();
        }
    }
}
