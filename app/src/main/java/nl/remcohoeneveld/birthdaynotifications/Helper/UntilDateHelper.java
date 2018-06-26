package nl.remcohoeneveld.birthdaynotifications.Helper;

import android.annotation.TargetApi;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class UntilDateHelper {

    public UntilDateHelper() {}
    public static Long getUntilDate(Date date) {

        Date startDateValue = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date nextYear = cal.getTime();
        date.setYear(nextYear.getYear());

        long diff = date.getTime() - startDateValue.getTime();
        long seconds = diff / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = (hours / 24) + 1;

        if (days >= 365){
            date.setYear(startDateValue.getYear());

            long diffNew = date.getTime() - startDateValue.getTime();
            long secondsNew = diffNew / 1000;
            long minutesNew = secondsNew / 60;
            long hoursNew = minutesNew / 60;

            return (hoursNew / 24);
        } else {

            return days;
        }
    }

    private static boolean isWithinRange(Date testDate) {

        Date startDateValue = new Date();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.YEAR, 1);
        Date nextYear = cal.getTime();
        testDate.setYear(nextYear.getYear());

        return !(testDate.before(startDateValue) || testDate.after(nextYear));
    }
}
