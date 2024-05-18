package com.cc.utils;

/**
 * 对ThreadLocal进行封装
 * 方便用户在没有办法拿request对象的地方拿request对象中的信息，比如用户id
 * 需要`ThreadLocal`来进行对象的获取，这个线程是贯穿整个运行的，可以通过他来获取
 * 基于ThreadLocal 封装工具类，用户保存和获取当前登录的用户id
 * ThreadLocal以线程为 作用域，保存每个线程中的数据副本
 */
public class BaseContext {

    private static ThreadLocal<Long> threadLocal = new ThreadLocal<>();

    //  设置当前用户id
    public static void setCurrentId(Long id){
        System.out.println("Base"+Thread.currentThread().getName());
        threadLocal.set(id);
    }

    public static Long getCurrentId(){
        return threadLocal.get();
    }
}
