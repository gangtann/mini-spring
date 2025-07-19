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
 * BeanDefinition - Bean 元数据定义的核心数据结构
 * 
 * <p>底层原理说明：</p>
 * <p>BeanDefinition 是 Spring IoC 容器的核心数据结构，它封装了创建和管理 Bean 所需的所有元数据信息：</p>
 * 
 * <ul>
 *   <li><strong>Bean 类型信息</strong>：存储 Bean 的 Class 对象，用于反射实例化</li>
 *   <li><strong>Bean 名称</strong>：唯一标识符，用于容器中的 Bean 查找</li>
 *   <li><strong>实例化策略</strong>：存储无参构造函数，支持反射创建实例</li>
 *   <li><strong>依赖注入点</strong>：收集所有 @Autowired 字段，实现自动装配</li>
 *   <li><strong>生命周期回调</strong>：存储 @PostConstruct 方法，支持初始化回调</li>
 * </ul>
 * 
 * <p>构建过程：</p>
 * <ol>
 *   <li>通过反射提取 @Component 注解的 name 属性作为 Bean 名称</li>
 *   <li>获取无参构造函数用于实例化（要求必须有 public 无参构造）</li>
 *   <li>扫描所有字段收集 @Autowired 注解的依赖注入点</li>
 *   <li>扫描所有方法收集 @PostConstruct 生命周期回调方法</li>
 * </ol>
 * 
 * <p>异常处理：</p>
 * <ul>
 *   <li>缺少无参构造函数时抛出 RuntimeException</li>
 *   <li>确保 Bean 可以被反射实例化</li>
 * </ul>
 * 
 * <p>设计模式：</p>
 * <ul>
 *   <li>不可变对象：所有字段使用 final 修饰，确保线程安全</li>
 *   <li>建造者模式：通过构造函数一次性收集所有元数据</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 * @see ApplicationContext
 * @see Component
 * @see Autowired
 * @see PostConstruct
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
