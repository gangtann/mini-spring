package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequestMapping 注解：将 HTTP 请求映射到控制器类或方法的处理函数。
 * 
 * <p>底层原理说明：</p>
 * <ul>
 *   <li>该注解是 Spring MVC 请求映射机制的核心，负责建立 URL 与处理方法之间的映射关系</li>
 *   <li>可以标注在类级别和方法级别：</li>
 *   <ul>
 *     <li>类级别：定义基础路径前缀</li>
 *     <li>方法级别：定义具体的请求路径</li>
 *   </ul>
 *   <li>DispatcherServlet 通过反射扫描所有 @Controller 类中的 @RequestMapping 方法，
 *       建立 URL 到 HandlerMethod 的映射表（HandlerMapping）</li>
 *   <li>支持 RESTful 风格的 URL 设计，通过路径变量实现动态路由</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 类级别映射
 * @Controller
 * @RequestMapping("/user")
 * public class UserController {
 *     
 *     // 方法级别映射，完整路径为 /user/getById
 *     @RequestMapping("/getById")
 *     public String getUser(@RequestParam("id") Long id) {
 *         return "userDetail";
 *     }
 * }
 * </pre>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see Controller
 * @see RequestParam
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestMapping {

    String value();
}
