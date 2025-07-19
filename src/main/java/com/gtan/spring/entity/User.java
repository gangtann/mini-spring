package com.gtan.spring.entity;

/**
 * User - 用户实体类
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 */
public class User {

    private String name;

    private Integer age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }
}
