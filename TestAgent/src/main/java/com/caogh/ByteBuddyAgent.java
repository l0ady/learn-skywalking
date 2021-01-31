package com.caogh;

import net.bytebuddy.agent.builder.AgentBuilder;
import net.bytebuddy.description.method.MethodDescription;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;
import net.bytebuddy.utility.JavaModule;

import java.lang.instrument.Instrumentation;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 21:11
 */
public class ByteBuddyAgent {

    public static void premain(String args, Instrumentation instrumentation) {
        AgentBuilder.Transformer transformer = new AgentBuilder.Transformer() {

            @Override
            public DynamicType.Builder<?> transform(DynamicType.Builder<?> builder,
                                                    TypeDescription typeDescription,
                                                    ClassLoader classLoader,
                                                    JavaModule javaModule) {
                return builder.method(ElementMatchers.<MethodDescription>any())
                        .intercept(MethodDelegation.to(TimeInterceptor.class));
            }
        };
        new AgentBuilder.Default()
                .type(ElementMatchers.nameStartsWith("com.caogh"))
                .transform(transformer)
                .installOn(instrumentation);
    }
}
