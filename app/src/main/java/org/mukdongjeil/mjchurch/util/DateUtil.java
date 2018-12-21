package org.mukdongjeil.mjchurch.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {

    public static final long A_DAY_TIME_MILLIS = 1000 * 60 * 60 * 24;

    private static final SimpleDateFormat TODAY_DATE_FORMAT
            = new SimpleDateFormat("오늘 aa hh시 mm분");
    private static final SimpleDateFormat YESTERDAY_DATE_FORMAT
            = new SimpleDateFormat("어제 aa hh시 mm분");
    private static final SimpleDateFormat DEFAULT_DATE_FORMAT
            = new SimpleDateFormat("yyyy년 MM월 dd일 aa hh시 mm분");

    public static final String convertReadableDateTime(long timeMillis) {
        long currentTimeMillis = System.currentTimeMillis();
        long timeGap = currentTimeMillis - timeMillis;
        if (timeGap < A_DAY_TIME_MILLIS) {
            return TODAY_DATE_FORMAT.format(new Date(timeMillis));
        } else if (timeGap < (2 * A_DAY_TIME_MILLIS)) {
            return YESTERDAY_DATE_FORMAT.format(new Date(timeMillis));
        } else {
            return DEFAULT_DATE_FORMAT.format(new Date(timeMillis));
        }
    }
}
