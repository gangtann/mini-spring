package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * PostConstruct 注解：标识 Bean 初始化完成后需自动执行的方法。
 * <p>
 * 在 IoC 容器实例化 Bean 并完成依赖注入后，会调用标注此注解的方法，
 * 常用于执行初始化逻辑或验证操作。
 * </p>
 *
 * <ul>
 *   <li>仅支持标注在无参方法上。</li>
 *   <li>方法在实例化并注入完成后自动调用。</li>
 *   <li>在运行时保留注解信息，供容器反射扫描与调用。</li>
 * </ul>
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface PostConstruct {
}
