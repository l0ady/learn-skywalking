package com.caogh;

import net.bytebuddy.implementation.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 23:08
 */
public class Interceptor {
    
    @RuntimeType
    public Object intercept(
            @This Object object,
            @AllArguments Object[] args,
            @SuperCall  Callable zuper,
            @Origin Method method,
            @Super DB db
    ) throws Exception {
        System.out.println(object);
        System.out.println(db);
        
        try{
            return zuper.call();
        }finally {
            
        }
    }
}
