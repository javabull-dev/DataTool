package cn.ljpc.datatool;

import android.app.Application;

import cn.ljpc.datatool.entity.Data;
import cn.ljpc.datatool.service.TcpServer;
import cn.ljpc.datatool.util.ConfigFileUtil;

public class ApplicationContext extends Application {

    public static Data sData = null;

    public static boolean serverStatus = false;

    public static final TcpServer sServer = new TcpServer();

    @Override
    public void onCreate() {
        super.onCreate();
        sData = ConfigFileUtil.loadConfigFile(this);
    }
}
