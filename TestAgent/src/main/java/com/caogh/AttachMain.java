package com.caogh;


import com.sun.tools.attach.*;

import java.util.List;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 21:55
 */
public class AttachMain {

    public static void main(String[] args) throws Exception{
        List<VirtualMachineDescriptor> list = VirtualMachine.list();
        
        //agentmain 所在的jar
        String jar = "D:/workspace/iflytek/items/SkyWalkingTest/TestAgent/target/TestAgent-1.0-SNAPSHOT.jar";
        
        List<VirtualMachineDescriptor> list1 = null;
        
        while(true){
            list1 = VirtualMachine.list();
            
            for(VirtualMachineDescriptor vmd: list1){
                
                if(!list.contains(vmd)){//发现新的jvm
                    
                    VirtualMachine vm = VirtualMachine.attach(vmd); //attch到新的jvm
                    
                    vm.loadAgent(jar);//加载jar
                    
                    vm.detach();// detch
                    
                    return;
                }
            }
            Thread.sleep(1000);
        }
    }
    
}
