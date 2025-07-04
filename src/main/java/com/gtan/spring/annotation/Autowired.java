package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解 Autowired：自动注入标记，用于在 IoC 容器中声明需要进行依赖注入的字段。
 * <p>
 * 在容器实例化 Bean 时，会扫描所有带有 @Autowired 注解的字段，
 * 并根据字段类型或名称从容器中获取对应的依赖实例并注入。
 * </p>
 * <p>
 * 注解可以标注在字段上，不支持在构造器或方法参数上使用。
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Autowired {
}
