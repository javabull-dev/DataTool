package cn.ljpc.datatool.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {

    public static String getNowDateFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String format = dateFormat.format(new Date());
        return format;
    }
}
