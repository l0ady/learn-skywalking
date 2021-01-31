package com.caogh;


import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 17:41
 */
public class TestAgent {

    public static void premain(String agentArgs, Instrumentation instrumentation) throws UnmodifiableClassException {
        System.out.println("this is a java agent with two args");

        print("tow args");

        System.out.println("参数1:"+agentArgs+"\n");

        instrumentation.addTransformer(new Transformer(), true);
        instrumentation.retransformClasses(TestClass.class);

        System.out.println("premain done");
    }

    public static void agentmain(String agentArgs){

        System.out.println("this is a java agent with one args");
        print("one args");
        System.out.println("参数2:"+agentArgs+"\n");
    }

    private static void print(String name){

        System.out.println(name + "调用栈--------------------------------");

        java.util.Map<Thread, StackTraceElement[]> ts = Thread.getAllStackTraces();

        StackTraceElement[] ste = ts.get(Thread.currentThread());

        for (StackTraceElement s : ste) {

            System.out.println("doConsumeBatchedInput:   " + s.toString());

        }
        System.out.println(name + "调用栈--------------------------------");
    }
}
