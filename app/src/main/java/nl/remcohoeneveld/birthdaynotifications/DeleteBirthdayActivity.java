package nl.remcohoeneveld.birthdaynotifications;

import android.content.Intent;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import java.util.Date;
import java.util.Locale;

import nl.remcohoeneveld.birthdaynotifications.Helper.AgeHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.UntilDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;
import nl.remcohoeneveld.birthdaynotifications.Service.CronJobService;

public class DeleteBirthdayActivity extends AppCompatActivity {

    FirebaseListAdapter<Birthday> mAdapter;

    public static DeleteBirthdayActivity instance = null;
    public static boolean active = false;
    String userId;
    String bday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delete_birthday);

        // get database and user
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ListView birthdayRemoveList = findViewById(R.id.birthdayListRemove);
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
                    .setLayout(android.R.layout.two_line_list_item)
                    .build();

            mAdapter = new FirebaseListAdapter<Birthday>(options) {
                @RequiresApi(api = Build.VERSION_CODES.O)
                @Override
                protected void populateView(View v, Birthday birthday, int position) {
                    //set the text1 value to fullname of the person
                    ((TextView) v.findViewById(android.R.id.text1)).setText(birthday.getFull_name());

                    //convert the date of birth to the age
                    Integer age = AgeHelper.getAge(birthday.date_of_birth);

                    // set the undertitle to the textview text2
                    TextView underTitle = v.findViewById(android.R.id.text2);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());

                    // if the date is the same as today then change the text to the birthday else just show the age
                    if (SameDateHelper.initializeSamedate(birthday.getDate_of_birth())) {
                        bday = "Birthday of " + birthday.nickname + " (age " + age + ") " + dateFormat.format(birthday.date_of_birth);
                    } else {
                        bday = dateFormat.format(birthday.date_of_birth);
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
                        // get the nickname from the field (listItem)
                        Field fieldNickname = listItem.getClass().getDeclaredField("nickname");
                        Object nickname = fieldNickname.get(listItem);

                        // get the uniqueID from the field (listitem)
                        Field fieldUniqueID = listItem.getClass().getDeclaredField("uniqueID");
                        Object uniqueID = fieldUniqueID.get(listItem);

                        // FOR DELETING AN ITEM OUT OF FIREBASS
                        // create a query that the UniqueID is equal to the uniqueID of the birthday

                        Query queryChild = database.getReference("users/" + userId).orderByChild("uniqueID").equalTo(uniqueID.toString());

                        // if the queryChild is clicked then remove the snapshotChild
                        queryChild.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                for (DataSnapshot snapshotChild : dataSnapshot.getChildren()) {
                                    snapshotChild.getRef().removeValue();
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Log.e("TAG", "onCancelled", databaseError.toException());
                            }
                        });
                        //showing a message when deleting the birthday
                        Toast.makeText(getApplicationContext(), "Deleted birthday of " + nickname, Toast.LENGTH_SHORT).show();


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
        instance = this;
        active = true;
    }

    @Override
    protected void onStop() {
        // important to check if the adapter has stopped listening on the stop
        super.onStop();
        mAdapter.stopListening();
        instance = null;
        active = false;
    }

    @Override
    protected void onResume() {
        super.onResume();
        instance = this;
        active = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
        instance = null;
        active = false;
    }
}
