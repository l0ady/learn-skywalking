package com.caogh;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import net.bytebuddy.implementation.bind.annotation.SuperCall;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 21:16
 */
public class TimeInterceptor {
    
    @RuntimeType
    public static Object intercept(@Origin Method method,
                                   @SuperCall Callable<?> callable) throws Exception {
        long begin = System.currentTimeMillis();
        
        try{
            return callable.call();
        }finally {
            System.out.println(method.getName() + ":"+ (System.currentTimeMillis() - begin) + "ms");
        }
    }
}
