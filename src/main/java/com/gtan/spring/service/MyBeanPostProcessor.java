package com.gtan.spring.service;

import com.gtan.spring.annotation.Component;

/**
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 */
@Component
public class MyBeanPostProcessor implements BeanPostProcessor{
    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        System.out.println(beanName + "初始化完成");
        return bean;
    }
}
