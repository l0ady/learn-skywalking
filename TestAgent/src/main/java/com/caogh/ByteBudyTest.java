package com.caogh;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.implementation.FixedValue;
import net.bytebuddy.implementation.MethodDelegation;

import static net.bytebuddy.dynamic.loading.ClassLoadingStrategy.Default.INJECTION;
import static net.bytebuddy.matcher.ElementMatchers.isDeclaredBy;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 22:53
 */
public class ByteBudyTest {

    public static void main(String[] args) throws Exception{
        hello();
        
    }

    public static void hello() throws Exception{
        String hello = new ByteBuddy()
                .subclass(DB.class)
                .method(named("hello"))
                .intercept(MethodDelegation.to(new Interceptor()))
                .make()
                .load(ClassLoader.getSystemClassLoader())
                .getLoaded()
                .newInstance()
                .hello(new Object());

        System.out.println(hello);
    }
    
    public static void foo() throws Exception{
        Foo foo = new ByteBuddy()
                .subclass(Foo.class)
                .method(isDeclaredBy(Foo.class))
                .intercept(FixedValue.value("One"))
                .method(named("foo"))
                .intercept(FixedValue.value("Two"))
                .method(named("foo").and(takesArguments(1)))
                .intercept(FixedValue.value("Three"))
                .make()
                .load(ClassLoader.getSystemClassLoader(), INJECTION)
                .getLoaded()
                .newInstance();

        System.out.println(foo.boo());
        System.out.println(foo.foo());
        System.out.println(foo.foo(null));
    }
}
