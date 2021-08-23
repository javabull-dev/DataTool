package cn.ljpc.datatool.util;

import android.content.Context;
import android.os.Environment;

import org.aeonbits.owner.ConfigFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import cn.ljpc.datatool.entity.Data;
import cn.ljpc.datatool.entity.DataConfig;

public class ConfigFileUtil {

    /**
     * 读取配置文件，加载配置文件中的数据
     *
     * @param context
     * @return
     */
    public static Data loadConfigFile(Context context) {
        File baseDir = context.getFilesDir();
        Data data = new Data();
        File configFile = new File(baseDir, "config.properties");
        if (configFile.exists() && configFile.isFile()) {
            try (InputStreamReader isr = new InputStreamReader(new FileInputStream(configFile), Charset.forName("utf8"))) {
                Properties properties = new Properties();
                properties.load(isr);
                DataConfig dataConfig = ConfigFactory.create(DataConfig.class, properties);
                data.localIP = dataConfig.getLocalIP();
                data.localPort = dataConfig.getLocalPort();
                data.serverPort = dataConfig.getServerPort();
                data.storeFilePath = dataConfig.getStoreFilePath();
                return data;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //赋默认值
        data.localIP = "192.168.43.59";
        data.localPort = 9999;
        data.serverPort = 9999;
        data.storeFilePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        return data;
    }

    /**
     * 将data中的数据保存至配置文件中
     *
     * @param context
     * @param data
     */
    public static void saveConfigFile(Context context, Data data) {
        File baseDir = context.getFilesDir();
        File configFile = new File(baseDir, "config.properties");
        try (OutputStreamWriter osw = new OutputStreamWriter(new FileOutputStream(configFile), Charset.forName("utf8"))) {
            Properties properties = new Properties();
            Map<String, Object> map = new HashMap<>();
            Field[] declaredFields = data.getClass().getDeclaredFields();
            for (int i = 0; i < declaredFields.length; i++) {
                Field field = declaredFields[i];
                //通过属性获取属性值，前提是修饰符必须为public
                Object o = "";
                try {
                    o = String.valueOf(field.get(data));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } finally {
                    map.put(field.getName(), o);
                }
            }
            properties.putAll(map);
            properties.store(osw, "");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
