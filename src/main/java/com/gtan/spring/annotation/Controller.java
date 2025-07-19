package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Controller 注解：标识一个类为 Spring MVC 的控制器（Controller）组件。
 * 
 * <p>底层原理说明：</p>
 * <ul>
 *   <li>该注解本质上是一个特殊的 {@link Component} 注解，用于标识 Web 层的控制器类</li>
 *   <li>在 IoC 容器启动时，会扫描所有带有 @Controller 注解的类，将其注册为 Bean</li>
 *   <li>DispatcherServlet 会专门识别这些控制器类，并将其中的 @RequestMapping 方法注册为请求处理器</li>
 *   <li>这是 Spring MVC 前端控制器模式的核心组成部分，实现了请求分发和业务逻辑的解耦</li>
 * </ul>
 * 
 * <p>使用限制：</p>
 * <ul>
 *   <li>只能标注在类上（ElementType.TYPE）</li>
 *   <li>运行时保留注解信息（RetentionPolicy.RUNTIME），便于反射扫描</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see Component
 * @see RequestMapping
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
}
