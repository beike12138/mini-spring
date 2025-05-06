package com.han.spring;

/**
 * @FileName MyBeanPostProcessor
 * @Description TODO
 * @Author beike1024
 * @Date 2025/5/4 22:02
 **/
@Component
public class MyBeanPostProcessor implements BeanPostProcessor{

    @Override
    public Object beforeInitialization(Object bean, String beanName) {
        System.out.println( beanName + "初始化开始");
        return bean;
    }

    @Override
    public Object afterInitialization(Object bean, String beanName) {
        System.out.println( beanName + "初始化完成");
        return bean;
    }
}
