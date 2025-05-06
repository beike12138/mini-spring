package com.han.spring;

/**
 * @FileName BeanPostProcessor
 * @Description 通过实现BeanPostProcessor接口，实现Bean初始化前后的逻辑
 * @Author beike1024
 * @Date 2025/5/4 21:12
 **/
public interface BeanPostProcessor {

    default Object beforeInitialization(Object bean, String beanName){
        return bean;
    }

    default Object afterInitialization(Object bean, String beanName){
        return bean;
    }
}
