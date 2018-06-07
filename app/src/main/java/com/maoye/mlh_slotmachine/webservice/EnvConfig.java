package com.maoye.mlh_slotmachine.webservice;

/**
 * Created by Rs on 2018/4/9.
 */

public abstract class EnvConfig {
    private static Stage stage = Stage.PROD;

    public static EnvConfig instance() {
        switch (stage) {
            case DEV:
                return new DevConfig();
            case PROD:
                return new ProdConfig();
            case LUOCHAO:
                return new LuoChao();
            default:
                return null;
        }
    }
    public abstract String getWebServiceBaseUrl();
    public abstract String getQuickPayWebServiceBaseUrl();
    public abstract String getBaseUkfUrl();

    public  abstract String getH5BaseUrl();

    public abstract int getEnvType();
    public enum Stage {
        DEV, PROD ,LUOCHAO
    }
}
