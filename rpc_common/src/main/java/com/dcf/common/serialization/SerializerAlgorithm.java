package com.dcf.common.serialization;


public enum SerializerAlgorithm {

    Java(new JdkSerializer()),

    Json(new GsonSerializer());

    private Serializer serializer;

    private SerializerAlgorithm(Serializer serializer) {
        this.serializer = serializer;
    }

    public Serializer getSerializer() {
        return serializer;
    }
}


