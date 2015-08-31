package io.xpush.chat.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Comparator;
import java.util.TimeZone;

import io.xpush.chat.models.XPushMessage;

public class DateUtils {

    public static String getDate( long timestamp ){
        return getDate( timestamp, "yyyy-MM-dd HH:mm:ss" );
    }

    public static String getDate( long timestamp, String dateFormat ){
        Calendar calendar = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();
        calendar.add(Calendar.MILLISECOND, tz.getOffset(calendar.getTimeInMillis()));

        SimpleDateFormat sdf = new SimpleDateFormat( dateFormat );
        java.util.Date currenTimeZone = new java.util.Date((long)timestamp);

        return sdf.format(currenTimeZone);
    }

    public static String getTimeString( long timestamp ){
        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat sdf = new SimpleDateFormat( "M월 dd일" );
        java.util.Date updated = new java.util.Date((long)timestamp);

        java.util.Date currentDate =  calendar.getTime();

        String result = sdf.format( updated );
        if( result.equals(sdf.format(currentDate)) ){
            sdf = new SimpleDateFormat( "a h:mm" );
            result = sdf.format(updated);
        }

        return result;
    }
}
