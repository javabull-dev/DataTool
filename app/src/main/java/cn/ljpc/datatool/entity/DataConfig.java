package cn.ljpc.datatool.entity;

import org.aeonbits.owner.Config;

public interface DataConfig extends Config {

    @Key("serverPort")
    @DefaultValue("9999")
    int getServerPort();

    @Key("storeFilePath")
    @DefaultValue("")
    String getStoreFilePath();

    @Key("localIP")
    @DefaultValue("192.168.43.59")
    String getLocalIP();

    @Key("localPort")
    @DefaultValue("9999")
    int getLocalPort();
}
