package com.dcf.common.config;



import com.dcf.common.serialization.SerializerAlgorithm;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {
    static Properties properties;
    static {
        try (InputStream in = Config.class.getResourceAsStream("/rpc.properties")) {
            properties = new Properties();
            properties.load(in);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }
    public static int getServicePort() {
        String value = properties.getProperty("nacos.service.port");
        if(value == null) {
            return 8080;
        } else {
            return Integer.parseInt(value);
        }
    }

    public static String getServiceName() {
        String value = properties.getProperty("nacos.service.name");
        if(value == null) {
            return "rpc_server";
        } else {
            return value;
        }
    }

    public static String getNacoServerAddr() {
        String value = properties.getProperty("nacos.serverAddr");
        if(value == null) {
            return "rpc_server";
        } else {
            return value;
        }
    }

    public static SerializerAlgorithm getSerializerAlgorithm() {
        String value = properties.getProperty("serializer.algorithm");
        if(value == null) {
            return SerializerAlgorithm.Java;
        } else {
            return SerializerAlgorithm.valueOf(value);
        }
    }
}