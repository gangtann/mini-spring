package com.gtan.spring.service;

import com.gtan.spring.annotation.Autowired;
import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.PostConstruct;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

/**
 * BeanDefinition：描述一个 Bean 的元数据信息，包括类型、实例化构造器、
 * 依赖注入字段列表及生命周期回调方法。
 * <p>
 * 该类在扫描到带 @Component 注解的类时生成，用于 IoC 容器管理。
 * </p>
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
public class BeanDefinition {

    /**
     * Bean 的实际类型（Class 对象）
     */
    private final Class<?> beanType;

    /**
     * Bean 在容器中的名称，默认为 Component 注解中指定的 name 或类简单名
     */
    private final String name;

    /**
     * 无参构造器，用于反射实例化 Bean
     */
    private final Constructor<?> constructor;

    /**
     * 标注了 @Autowired 的字段列表，需要在实例化后进行注入
     */
    private final List<Field> autowiredFields;

    /**
     * 标注了 @PostConstruct 的方法，在 Bean 实例化并注入完成后执行
     */
    private final Method postConstructMethod;

    /**
     * 构造一个 BeanDefinition，提取类型上的 @Component 配置及注入点信息
     *
     * @param type 带有 @Component 注解的 Bean 类
     * @throws RuntimeException 如果该类不具有公共无参构造器
     */
    public BeanDefinition(Class<?> type) {
        this.beanType = type;
        // 提取 Component 注解及其 name
        Component component = type.getDeclaredAnnotation(Component.class);
        this.name = component.name().isEmpty() ? type.getSimpleName() : component.name();
        try {
            // 反射获取无参构造器
            this.constructor = type.getConstructor();
            // 查找 @PostConstruct 方法
           this.postConstructMethod = Arrays.stream(type.getDeclaredMethods())
                    .filter(method -> method.isAnnotationPresent(PostConstruct.class))
                    .findFirst()
                    .orElse(null);
            // 查找所有 @Autowired 字段
            this.autowiredFields = Arrays.stream(type.getDeclaredFields())
                    .filter(field -> field.isAnnotationPresent(Autowired.class))
                    .toList();
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Bean class must have a public no-arg constructor", e);
        }
    }

    /**
     * @return Bean 的类型
     */
    public Class<?> getBeanType() {
        return beanType;
    }

    /**
     * @return Bean 在容器中的名称
     */
    public String getName() {
        return name;
    }

    /**
     * @return 用于实例化 Bean 的无参构造器
     */
    public Constructor<?> getConstructor() {
        return constructor;
    }

    /**
     * @return 需要依赖注入的字段列表
     */
    public List<Field> getAutowiredFields() {
        return autowiredFields;
    }

    /**
     * @return 在实例化及注入后需要执行的生命周期方法，可能为 null
     */
    public Method getPostConstructMethod() {
        return postConstructMethod;
    }

}
