package com.han.spring.sub;

import com.han.spring.Component;

/**
 * @FileName Dog
 * @Description TODO
 * @Author beike1024
 * @Date 2025/5/4 3:05
 **/
@Component(name = "myDog")
public class Dog {

    @Autowired
    private Cat cat;
    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Dog创建了  dog里面有一只猫：" + cat);
        System.out.println("Dog创建了  dog里面有一只狗：" + dog);
    }
}
