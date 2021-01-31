### java agent打包plugin

    <plugin>
        <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <version>2.4</version>
            <configuration>
                <appendAssemblyId>false</appendAssemblyId>
                <!-- 将TestAgent的所有依赖包都打到jar包中-->
                <descriptorRefs>
                    <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                    <!-- 添加MANIFEST.MF中的各项配置-->
                    <manifest>
                        <!-- 添加 mplementation-*和Specification-*配置项-->
                        <addDefaultImplementationEntries>true</addDefaultImplementationEntries>
                        <addDefaultSpecificationEntries>true</addDefaultSpecificationEntries>
                    </manifest>
                    <!-- 将 premain-class 配置项设置为com.xxx.TestAgent-->
                    <manifestEntries>
                        <Premain-Class>com.caogh.dubbo.TestAgent</Premain-Class>
                    </manifestEntries>
                </archive>
            </configuration>
        <executions>
            <execution>
                <!-- 绑定到package生命周期阶段上 -->
                <phase>package</phase>
                <goals>
                    <!-- 绑定到package生命周期阶段上 -->
                    <goal>single</goal>
                </goals>
            </execution>
        </executions>
    </plugin>

#### 执行命令
    package -Dcheckstyle.skip -f pom.xml
#### 查看结果
    META-INF/MANIFEST.MF中Premain-Class: com.xxx.TestAgent

#### java agent参数修改
    -javaagent:D:\workspace\iflytek\items\SkyWalkingTest\TestAgent\target\TestAgent-1.0-SNAPSHOT.jar=option1=111,option2=222

#### 修改类的实现
- addTransformer()/removeTransformer() 方法：注册/注销一个 ClassFileTransformer 类的实例，该 Transformer 会在类加载的时候被调用，可用于修改类定义。
- redefineClasses() 方法：该方法针对的是已经加载的类，它会对传入的类进行重新定义。
- **getAllLoadedClasses()方法：**返回当前 JVM 已加载的所有类。
- getInitiatedClasses() 方法：返回当前 JVM 已经初始化的类。
- getObjectSize()方法：获取参数指定的对象的大小。

#### 通过prmain修改将来要加载的类

    //main方法
    public static void main(String[] args) {
        TestClass testClass = new TestClass();
        System.out.println("The get number: "+testClass.getNumber());
    }

    //premain中
    inst.addTransformer(new Transformer(), true);//a
    inst.retransformClasses(TestClass.class);//b
    System.out.println("premain done");

    //Transformer.java实现ClassFileTransformer中的transform方法
    //重新返回修改后的类的字节码(byte[])
    getBytesFromFile("TestMain/target/classes/com/caogh/TestClass.class.2");

### 总结
    1. 将TestClass的实现类修改为TestClass.class.2,系统仅执行了后者的构造方法，并没有执行TestClass的构造方法。
    2. 替换字节码的过程：
       a的位置往TransformerManager的成员变量TransformerManager.TransformerInfo[] mTransformerList中添加了ClassFileTransformer的实现类；
       b会调用本地方法找到JVM层的类实例InstanceKlass，并获取类的字节码，存放在class_definitions数组中;然后通过获取mTransformerList中的ClassFileTransformer.transform获取新的字节码替换老的实现。

### byte budy 实现方法拦截,打印方法执行时长。
#### 添加依赖
    <dependency>
        <groupId>com.caogh</groupId>
        <artifactId>TestMain</artifactId>
        <version>1.0-SNAPSHOT</version>
        <scope>compile</scope>
    </dependency>
    <dependency>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy</artifactId>
        <version>1.9.2</version>
    </dependency>
    <dependency>
        <groupId>net.bytebuddy</groupId>
        <artifactId>byte-buddy-agent</artifactId>
        <version>1.9.2</version>
    </dependency>
#### 创建ByteBuddyAgent类和拦截器TimeInterceptor,
    //TimeInterceptor
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

### Attach API 基础
    在 Java 5 中，Java 开发者只能通过 Java Agent 中的 premain() 方法在 main() 方法执行之前进行一些操作，这种方式在一定程度上限制了灵活性。Java 6 针对这种状况做出了改进，提供了一个 agentmain() 方法，Java 开发者可以在 main() 方法执行以后执行 agentmain() 方法实现一些特殊功能。

- VirtualMachine 是对一个 Java 虚拟机的抽象，在 Attach 工具程序监控目标虚拟机的时候会用到该类。VirtualMachine 提供了 JVM 枚举、Attach、Detach 等基本操作。
- VirtualMachineDescriptor 是一个描述虚拟机的容器类，后面示例中会介绍它如何与 VirtualMachine 配合使用。

#### bytebudy 用法
- @RuntimeType 注解：告诉 Byte Buddy 不要进行严格的参数类型检测，在参数匹配失败时，尝试使用类型转换方式（runtime type casting）进行类型转换，匹配相应方法。
- **@This 注解：**注入被拦截的目标对象（即前面示例的 DB 对象）。
- @AllArguments 注解：注入目标方法的全部参数，是不是感觉与 Java 反射的那套 API 有点类似了？
- @Origin 注解：注入目标方法对应的 Method 对象。如果拦截的是字段的话，该注解应该标注到 Field 类型参数。
- @Super 注解：注入目标对象。通过该对象可以调用目标对象的所有方法。
- @SuperCall：这个注解比较特殊，我们要在 intercept() 方法中调用目标方法的话，需要通过这种方式注入，与 Spring AOP 中的 ProceedingJoinPoint.proceed() 方法有点类似，需要注意的是，这里不能修改调用参数，从上面的示例的调用也能看出来，参数不用单独传递，都包含在其中了。另外，@SuperCall 注解还可以修饰 Runnable 类型的参数，只不过目标方法的返回值就拿不到了。
### skywalking 关键字讲解
    SpanContext 和 Baggage
    SpanContext 表示进程边界，在跨进调用时需要将一些全局信息，例如，TraceId、当前 SpanId 等信息封装到 Baggage 中传递到另一个进程（下游系统）中。
    Baggage 是存储在 SpanContext 中的一个键值对集合。它会在一条 Trace 中全局传输，该 Trace 中的所有 Span 都可以获取到其中的信息。
    需要注意的是，由于 Baggage 需要跨进程全局传输，就会涉及相关数据的序列化和反序列化操作，如果在 Baggage 中存放过多的数据，就会导致序列化和反序列化操作耗时变长，使整个系统的 RPC 的延迟增加、吞吐量下降。
    虽然 Baggage 与 Span Tags 一样，都是键值对集合，但两者最大区别在于 Span Tags 中的信息不会跨进程传输，而 Baggage 需要全局传输。因此，OpenTracing 要求实现提供 Inject 和 Extract 两种操作，SpanContext 可以通过 Inject 操作向 Baggage 中添加键值对数据，通过 Extract 从 Baggage 中获取键值对数据。
#### 核心接口语义
    OpenTracing 希望各个实现平台能够根据上述的核心概念来建模实现，不仅如此，OpenTracing 还提供了核心接口的描述，帮助开发人员更好的实现 OpenTracing 规范。
#### Span 接口
    Span接口必须实现以下的功能：
    获取关联的 SpanContext：通过 Span 获取关联的 SpanContext 对象。
    关闭（Finish）Span：完成已经开始的 Span。
    添加 Span Tag：为 Span 添加 Tag 键值对。
    添加 Log：为 Span 增加一个 Log 事件。
    添加 Baggage Item：向 Baggage 中添加一组键值对。
    获取 Baggage Item：根据 Key 获取 Baggage 中的元素。
#### SpanContext 接口
    SpanContext 接口必须实现以下功能，用户可以通过 Span 实例或者 Tracer 的 Extract 能力获取 SpanContext 接口实例。
    遍历 Baggage 中全部的 KV。
#### Tracer 接口
    Tracer 接口必须实现以下功能：
    创建 Span：创建新的 Span。
    注入 SpanContext：主要是将跨进程调用携带的 Baggage 数据记录到当前 SpanContext 中。
    提取 SpanContext ，主要是将当前 SpanContext 中的全局信息提取出来，封装成 Baggage 用于后续的跨进程调用。