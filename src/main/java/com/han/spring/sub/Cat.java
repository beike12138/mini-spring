package com.han.spring.sub;

import com.han.spring.Component;

/**
 * @FileName cat
 * @Description TODO
 * @Author beike1024
 * @Date 2025/5/4 1:05
 **/
@Component
public class Cat {

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("Cat创建了  cat里面有一个属性：" + dog);
    }
}
