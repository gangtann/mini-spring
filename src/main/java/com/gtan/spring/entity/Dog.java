package com.gtan.spring.entity;

import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.Autowired;
import com.gtan.spring.annotation.PostConstruct;

/**
 * Dog - 演示循环依赖的实体类
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 * @see Cat
 * @see Component
 * @see Autowired
 * @see PostConstruct
 */
@Component(name = "myDog")
public class Dog {

    @Autowired
    private Cat cat;

    @PostConstruct
    public void init() {
        System.out.println("成功创建对象Dog");
        System.out.println(cat);
    }
}
