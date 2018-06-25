package nl.remcohoeneveld.birthdaynotifications;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import nl.remcohoeneveld.birthdaynotifications.Helper.AgeHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.DatabaseHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.SameDateHelper;
import nl.remcohoeneveld.birthdaynotifications.Helper.UniqueIDHelper;
import nl.remcohoeneveld.birthdaynotifications.Model.Birthday;
import nl.remcohoeneveld.birthdaynotifications.Service.CronJobService;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseListAdapter<Birthday> mAdapter;

    public static MainActivity instance = null;
    String userId;
    String bday;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // set the toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // get database and user
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final ListView birthdayList = findViewById(R.id.birthdayList);
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

                    // if the date is the same as today then change the text to the birthday else just show the age
                    if (SameDateHelper.initializeSamedate(birthday.getDate_of_birth())) {
                        bday = "Its the birthday of " + birthday.nickname + " (age " + age + ")";
                    } else {
                        bday = "(age " + age + ")";
                    }

                    underTitle.setText(bday);
                }
            };

            // set the adapter mAdapter from firebase
            birthdayList.setAdapter(mAdapter);
            birthdayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected item text from ListView
                    Object listItem = birthdayList.getItemAtPosition(position);
                    try {
                        // get the nickname from the field (listItem)
                        Field fieldNickname = listItem.getClass().getDeclaredField("nickname");
                        Object nickname = fieldNickname.get(listItem);

                        // get the uniqueID from the field (listitem)
                        Field fieldUniqueID = listItem.getClass().getDeclaredField("uniqueID");
                        Object uniqueID = fieldUniqueID.get(listItem);

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
                        // showing a message when deleting the birthday
                        Toast.makeText(getApplicationContext(), "Deleted birthday of " + nickname, Toast.LENGTH_SHORT).show();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (NoSuchFieldException e) {
                        e.printStackTrace();
                    }

                }
            });
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), AddBirthdayActivity.class);
                startActivityForResult(intent, 1000);
            }
        });

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // stop the service when MainActivity is active
        stopService(new Intent(this,CronJobService.class));

    }

    @Override
    protected void onStart() {
        // important to check if the adapter is listening otherwise the listview will be empty
        super.onStart();
        mAdapter.startListening();
        instance = this;

    }

    @Override
    protected void onStop() {
        // important to check if the adapter has stopped listening on the stop
        super.onStop();
        mAdapter.stopListening();
        instance = null;
    }

    @Override
    protected void onResume() {
        super.onResume();
         instance = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        instance = null;
    }

    @Override
    public void onBackPressed() {

        // when onBackPressed show the LogOutDialog
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            ShowLogoutDialog();
        }

    }

    private void ShowLogoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.add_logout_message));
        builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                MainActivity.super.onBackPressed();
                FirebaseAuth.getInstance().signOut();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        if (id == R.id.action_log_out) {
            FirebaseAuth.getInstance().signOut();
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        //@ TODO: 26/06/2018 change these ids and fill them with the correct activity
        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // showing a message when a new birthday is created succesfully and checking if the operation is not cancelled
        if (resultCode == Activity.RESULT_OK) {
            String addSuccessMessage = data.getStringExtra("addSuccessMessage");

            if (addSuccessMessage != null) {
                Toast.makeText(getApplicationContext(), addSuccessMessage, Toast.LENGTH_SHORT).show();
            }
        }
        if (resultCode == Activity.RESULT_CANCELED) {
            Toast.makeText(getApplicationContext(), getString(R.string.add_error_message), Toast.LENGTH_SHORT).show();
        }
    }
}

