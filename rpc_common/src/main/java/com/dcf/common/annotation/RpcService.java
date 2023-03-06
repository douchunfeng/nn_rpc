package com.dcf.common.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Documented
@Target(ElementType.TYPE)
public @interface RpcService {
    String serviceName() default "rpc_service";
}
