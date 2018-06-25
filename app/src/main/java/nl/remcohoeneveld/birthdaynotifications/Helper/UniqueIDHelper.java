package nl.remcohoeneveld.birthdaynotifications.Helper;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Random;

public class UniqueIDHelper {
    public UniqueIDHelper() {}
    public static int createUniqueId() {

        Random rand = new Random();

        int id = rand.nextInt(50) + 1;
        //return unique id
        return id;
    }
}
