package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Component 注解：标识一个类为 Spring IoC 容器管理的组件，
 * 容器在启动时会扫描带该注解的类并将其实例化并注册到 BeanDefinition 中。
 *
 * <p>使用方式：</p>
 * <ul>
 *   <li>可选通过 name 属性指定 Bean 名称，若不指定则使用类的简单类名。</li>
 *   <li>仅支持标注在类（Type）上。</li>
 *   <li>在运行时保留注解信息，供容器反射扫描使用。</li>
 * </ul>
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Component {

    /**
     * Bean 在容器中的名称，默认为空时使用类简单名注册。
     *
     * @return 注册到 IoC 容器中的 Bean 名称
     */
    String name() default "";
}
