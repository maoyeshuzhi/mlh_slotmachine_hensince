package com.maoye.mlh_slotmachine.webservice;

/**
 * Created by Rs on 2018/5/2.
 */

public class LuoChao extends EnvConfig {
    @Override
    public String getWebServiceBaseUrl() {
        return "http://172.29.100.9/";
    }

    @Override
    public String getQuickPayWebServiceBaseUrl() {
        return "http://172.29.36.38:8081/";
    }

    @Override
    public String getBaseUkfUrl() {
        return "http://172.29.9.52:8888/";
    }

    @Override
    public String getH5BaseUrl() {
        return "http://172.31.10.229:9999/";
    }

    @Override
    public int getEnvType() {
        return 1;
    }
}
