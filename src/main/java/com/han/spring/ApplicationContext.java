package com.han.spring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public class ApplicationContext {

    /**
     * @param packageName 包名:"a\b\c"
     * @Description 构造方法
     */
    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    /**
     * @Description 创建一个map用来存放bean 也就是单例池
     */
    private final Map<String, Object> ioc = new HashMap<>();

    private final Map<String, Object> loadingIoc = new HashMap<>();
    /**
     * @Description 创建一个map用来存放bean图纸
     */
    private final Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    private final List<BeanPostProcessor> postProcessors = new ArrayList<>();

    public void initContext(String packageName) throws Exception {
        //ApplicationContext.class.getClassLoader().getResource(packageName);
        //List<Class<?>> list = scanPackage(packageName).stream().filter(clazz -> clazz.isAnnotationPresent(Component
        // .class)).toList();
//        scanPackage(packageName).stream().filter(this::canCreate).map(this::wrapper).forEach(this::createBean);
        scanPackage(packageName).stream().filter(this::canCreate).forEach(this::wrapper);
        initBeanPostProcessor();
        beanDefinitionMap.values().forEach(this::createBean);
    }

    /**
    * @Description 从图纸集合beanDefinitionMap中筛选出类型为 BeanPostProcessor 的 Bean图纸（也叫bean定义）
     * 然后通过 createBean() 方法实例化这些 BeanPostProcessor 类型的 Bean
     * 最后将这些处理器注册到 postProcessors 列表中，用于在后续 Bean 初始化前后进行拦截处理
    */
    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream()
                .filter(beanDefinition -> BeanPostProcessor.class.isAssignableFrom(beanDefinition.getBeanType()))
                .map(this::createBean)
                .map(bean -> (BeanPostProcessor) bean)
                .forEach(postProcessors::add);
    }

    /**
     * @param packageName 包名:"a\b\c"
     * @return java.util.List<java.lang.Class < ?>>
     * @Description 扫描指定包下的类 返回类的集合
     */
    public List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        // a.b.c
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        // 使用toURI替换getFile()
        Path path = Path.of(resource.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")) {
                    // absolutePath = D:\java_Study\spring_study\mini-spring\target\classes\com\han\spring\sub\cat.class
                    // 我们需要将它变成com.han.spring.sub.cat
                    String replaceStr = absolutePath.toString().replace(File.separator, ".");
                    int packageIndex = replaceStr.indexOf(packageName);
                    String className = replaceStr.substring(packageIndex, replaceStr.length() - ".class".length());
                    try {
                        classList.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }

    /**
     * @param type 类型
     * @return boolean
     * @Description 根据类是否有Component注解 判断是否需要创建bean
     */
    protected boolean canCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    /**
     * @param type 类型
     * @Description 获取bean的图纸，并将图纸放入到beanDefinitionMap（如果名字重复就抛出异常）
     */
    protected void wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("bean名字重复");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
    }

    /**
     * @param beanDefinition 图纸
     * @Description 根据图纸检测ioc中是否有这个bean，如果有就直接返回，如果没有就创建一个
     */
    protected Object createBean(BeanDefinition beanDefinition) {
        // 从图纸中获取类名
        String name = beanDefinition.getName();
        // 查找ioc中是否有这个bean，如果有，直接返回
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)) {
            return loadingIoc.get(name);
        }
        // 没有就创建一个
        return doCreateBean(beanDefinition);
    }

    /**
     * @param beanDefinition 图纸
     * @Description 根据BeanDefinition图纸创建bean
     */
    private Object doCreateBean(BeanDefinition beanDefinition) {
        // 拿到图纸中的构造器
        Constructor<?> constructor = beanDefinition.getConstructor();
        // 使用构造器创建bean 这里只使用了默认无参构造器
        Object bean = null;
        try {
            bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            autowiredBean(bean, beanDefinition);
            bean = initializeBean(bean, beanDefinition);//初始化完成后的bean,经过postProcessor可能有被修改，变得与原先的bean不同
            // 将bean从loadingIoc中移除，然后把bean放入ioc中
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return bean;
    }

    /**
     * @param bean           bean
     * @param beanDefinition 图纸
     * @Description 注入
     */
    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
            autowiredField.set(bean, getBean(autowiredField.getType()));
        }
    }
    /**
    * @Description 初始化bean，执行周期性函数
    * @param bean bean
    	* @param beanDefinition 图纸
    */
    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException,
            IllegalAccessException {
        for (BeanPostProcessor postProcessor : postProcessors) {
            bean = postProcessor.beforeInitialization(bean, beanDefinition.getName());
        }

        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null) {
            postConstructMethod.invoke(bean);
        }

        for (BeanPostProcessor postProcessor : postProcessors) {
            bean = postProcessor.afterInitialization(bean, beanDefinition.getName());
        }
        return bean;
    }
    public Object getBean(String beanName) {
        if (beanName == null) {
            return null;
        }
        Object bean = this.ioc.get(beanName);
        if (bean != null) {
            return bean;
        }
        if (beanDefinitionMap.containsKey(beanName)) {
            return createBean(beanDefinitionMap.get(beanName));
        }
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        String beanName = this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(beanName);
//        return this.ioc.values().stream()
//                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
//                .map(bean -> (T) bean)
//                .findAny()
//                .orElse(null);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .map(beanName -> getBean(beanName))
                .map(bean -> (T) bean)
                .toList();
//        return this.ioc.values().stream()
//                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
//                .map(bean -> (T) bean)
//                .toList();
    }
}


















