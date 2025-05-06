package com.han.spring;

import com.han.spring.sub.Autowired;
import com.han.spring.sub.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class BeanDefinition {

    /**
    * @Description bean的名字
    */
    private final String name;
    /**
    * @Description bean的构造函数
    */
    private final Constructor<?> constructor;
    /**
    * @Description bean的postConstruct方法
    */
    private final Method postConstructMethod;

    private final List<Field> autowiredFields;
    private final Class<?> beanType;

    /**
    * @Description 有参构造函数 使用时传入对象的类型
    * @param type 对象的类型
    */
    public BeanDefinition(Class<?> type) {
        this.beanType = type;
        //获取当前类的component注解
        Component component = type.getAnnotation(Component.class);
        //获取注解中name的值，如果没有就默认使用使用类名
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            this.constructor = type.getConstructor();
            // 获取带有PostConstruct注解的方法 并赋值给postConstructMethod
            this.postConstructMethod = Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                    .findFirst()
                    .orElse(null);
            // 获取带有Autowired注解的字段 并赋值给autowiredFields
            this.autowiredFields = Arrays.stream(type.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Autowired.class))
                    .toList();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public String getName() {
        return name;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }

    public Class<?> getBeanType() {
        return beanType;
    }

}
