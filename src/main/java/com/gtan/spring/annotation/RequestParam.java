package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * RequestParam 注解：将 HTTP 请求参数绑定到控制器方法的参数上。
 * 
 * <p>底层原理说明：</p>
 * <ul>
 *   <li>该注解实现了请求参数到方法参数的自动绑定机制，是 Spring MVC 数据绑定的核心组件</li>
 *   <li>DispatcherServlet 在调用控制器方法前，会解析 HTTP 请求中的参数：</li>
 *   <ul>
 *     <li>对于 GET 请求：从 URL 查询参数中解析</li>
 *     <li>对于 POST 请求：从表单数据中解析</li>
 *     <li>支持类型转换：自动将字符串参数转换为目标类型（如 int、long、Date 等）</li>
 *   </ul>
 *   <li>通过反射机制获取方法参数上的 @RequestParam 注解，提取 value 作为参数名</li>
 *   <li>支持基本数据类型、包装类型、String、以及对应的数组类型</li>
 *   <li>当请求中缺少对应参数时，会抛出异常或提供默认值（可配置）</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * // 基本使用
 * @RequestMapping("/user")
 * public String getUser(@RequestParam("id") Long userId) {
 *     // 请求 /user?id=123 时，userId 会被自动赋值为 123
 *     return "userDetail";
 * }
 * 
 * // 多个参数
 * @RequestMapping("/search")
 * public String search(@RequestParam("keyword") String keyword, 
 *                     @RequestParam("page") int pageNum) {
 *     // 请求 /search?keyword=java&page=1
 *     return "searchResults";
 * }
 * </pre>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see RequestMapping
 * @see Controller
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestParam {

    String value();
}
