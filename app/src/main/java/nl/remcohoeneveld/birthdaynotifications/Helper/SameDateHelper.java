package nl.remcohoeneveld.birthdaynotifications.Helper;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SameDateHelper {
    public SameDateHelper() {
    }

    public static boolean initializeSamedate(Date date){
        Calendar birthDate = Calendar.getInstance();
        birthDate.setTime(date);
        Calendar currentDate = Calendar.getInstance();
        currentDate.getTime();

        boolean sameMonth = birthDate.get(Calendar.MONTH) == currentDate.get(Calendar.MONTH);
        boolean sameDay = birthDate.get(Calendar.DAY_OF_MONTH) == currentDate.get(Calendar.DAY_OF_MONTH);
        return (sameDay && sameMonth);
    }
}
