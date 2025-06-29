package com.gtan.spring;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
// Component 注解可以加到类上
@Target(ElementType.TYPE)
// 在运行时获得 Component 的注解信息
@Retention(RetentionPolicy.RUNTIME)
/**
 * 只要添加了 Component 注解的类，我们都需要把它加载到容器中
 */
public @interface Component {
    String name() default "";
}
