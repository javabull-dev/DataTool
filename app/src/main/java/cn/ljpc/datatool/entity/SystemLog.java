package cn.ljpc.datatool.entity;

import cn.ljpc.datatool.util.TimeUtil;

/**
 * 全局系统日志记录
 */
public class SystemLog {
    private static StringBuffer stringBuffer = new StringBuffer(10000);

    public static String getLog() {
        return stringBuffer.toString();
    }

    public static synchronized void addLog(String log) {
        stringBuffer.append("---------------------\n").append(TimeUtil.getNowDateFormat()).append(log);
    }
}
