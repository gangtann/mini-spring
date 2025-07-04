package com.gtan.spring.entity;

import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.Autowired;
import com.gtan.spring.annotation.PostConstruct;

/**
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
@Component(name = "myCat")
public class Cat {

    @Autowired
    private Dog dog;

    @PostConstruct
    public void init() {
        System.out.println("成功创建对象Cat");
        System.out.println(dog);
    }

}
