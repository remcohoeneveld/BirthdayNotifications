package nl.remcohoeneveld.birthdaynotifications;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
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
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
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

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseListAdapter<Birthday> mAdapter;

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
            birthdayList.setAdapter(mAdapter);
            birthdayList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    // Get the selected item text from ListView
                    Object listItem = birthdayList.getItemAtPosition(position);
                    try {
                        // get the nickname from the field (listItem)
                        Field fieldDate = listItem.getClass().getDeclaredField("date_of_birth");
                        Object date_of_birth = fieldDate.get(listItem);

                        // get the nickname from the field (listItem)
                        Field fieldNickname = listItem.getClass().getDeclaredField("nickname");
                        Object nickname = fieldNickname.get(listItem);

                        if (UntilDateHelper.getUntilDate((Date) date_of_birth) > 0) {
                            Toast.makeText(getApplicationContext(), "Birthday of " + nickname + "has " + UntilDateHelper.getUntilDate((Date) date_of_birth) + " more days to go", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), "Today is the birthday of " + nickname, Toast.LENGTH_SHORT).show();
                        }

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
                Intent intent = new Intent(getApplicationContext(), AddBirthdayFormActivity.class);
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

        // stop the service when MainActivity is started
        stopService(new Intent(this,CronJobService.class));
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

        // stop the service when MainActivity is active
        stopService(new Intent(this,CronJobService.class));
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        FirebaseAuth.getInstance().signOut();
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
                // sign out the user
                FirebaseAuth.getInstance().signOut();
                // start the service again
                startService(new Intent(MainActivity.this,CronJobService.class));
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

        if (id == R.id.nav_delete_birthday) {
            Intent intent = new Intent(getApplicationContext(), DeleteBirthdayActivity.class);
            startActivityForResult(intent, 1000);
        } else if (id == R.id.nav_edit_birthday) {
            Intent intent = new Intent(getApplicationContext(), EditBirthdayActivity.class);
            startActivity(intent);
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

