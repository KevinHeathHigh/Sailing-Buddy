package net.hobbitsoft.android.sailingbuddy.utilities;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

public class ConversionUtils {

    public static double getMilesFromMeters(Double meters) {
        return meters * 0.000621371192;
    }

    public static double getMetersFromMiles(Double miles) {
        return miles * 1609.344;
    }

    public static String getDoubleAsRoundedString(Double number) {
        BigDecimal bigDecimal = new BigDecimal(number);
        bigDecimal = bigDecimal.setScale(2, RoundingMode.HALF_EVEN);
        return NumberFormat.getNumberInstance().format(bigDecimal);
    }

    public static Date returnTodayMidnight() {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date(System.currentTimeMillis()));
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

}
