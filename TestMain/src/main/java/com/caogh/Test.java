package com.caogh;

/**
 * Created by caogh
 *
 * @date: 2021/1/31 17:42
 */
public class Test {
    public static void main(String[] args) throws InterruptedException {
        
        System.out.println("The get number: "+new TestClass().getNumber());
        
        while(true){
            Thread.sleep(1000);
            System.out.println("The get number: "+new TestClass().getNumber());
        }
    }
}
