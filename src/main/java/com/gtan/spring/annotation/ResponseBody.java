package com.gtan.spring.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * ResponseBody 注解：将控制器方法的返回值直接作为 HTTP 响应体返回，而不是解析为视图名称。
 * 
 * <p>底层原理说明：</p>
 * <ul>
 *   <li>该注解实现了方法返回值的序列化机制，是 Spring MVC 中 RESTful API 开发的核心注解</li>
 *   <li>DispatcherServlet 在处理带有 @ResponseBody 注解的方法时：</li>
 *   <ul>
 *     <li>跳过视图解析器（ViewResolver）的处理流程</li>
 *     <li>使用 HttpMessageConverter 将返回值转换为指定格式（JSON、XML、纯文本等）</li>
 *     <li>根据请求的 Accept 头信息选择合适的转换器</li>
 *     <li>将转换后的数据直接写入 HTTP 响应体</li>
 *   </ul>
 *   <li>支持多种数据格式：Java 对象自动序列化为 JSON，字符串直接输出，字节数组直接传输等</li>
 *   <li>是现代 RESTful Web 服务（@RestController）的基础机制</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>RESTful API 接口开发</li>
 *   <li>AJAX 请求响应</li>
 *   <li>需要直接返回数据的接口</li>
 * </ul>
 * 
 * <p>使用示例：</p>
 * <pre>
 * @Controller
 * public class ApiController {
 *     
 *     // 返回 JSON 格式的用户数据
 *     @RequestMapping("/api/user")
 *     @ResponseBody
 *     public User getUser(@RequestParam("id") Long id) {
 *         User user = userService.findById(id);
 *         return user; // 自动序列化为 JSON
 *     }
 *     
 *     // 返回纯文本
 *     @RequestMapping("/api/hello")
 *     @ResponseBody
 *     public String hello() {
 *         return "Hello, World!"; // 直接作为响应体
 *     }
 * }
 * </pre>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see Controller
 * @see RequestMapping
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ResponseBody {
}
