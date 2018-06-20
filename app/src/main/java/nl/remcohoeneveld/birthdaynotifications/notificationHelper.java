package nl.remcohoeneveld.birthdaynotifications;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import java.util.Random;

import static android.content.Context.NOTIFICATION_SERVICE;

public class notificationHelper {

    private NotificationCompat.Builder notification;

    public notificationHelper(){}

    public void sendNotification(String value, Long time, Activity activity) {

        // creating a new notification with the channelID of the application
        notification = new NotificationCompat.Builder(activity, "BirthdayNotifications");
        notification.setAutoCancel(true);

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
        Intent intent = new Intent(activity, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(activity, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        notification.setContentIntent(pendingIntent);

        // building the notifcation manager
        NotificationManager nm = (NotificationManager) activity.getSystemService(NOTIFICATION_SERVICE);

        // notifying the notifcation manager (checking of the nm != null)
        assert nm != null;
        nm.notify(n, notification.build());
    }
}
