package com.gtan.spring.controller;

import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.Controller;
import com.gtan.spring.annotation.RequestMapping;
import com.gtan.spring.annotation.RequestParam;
import com.gtan.spring.annotation.ResponseBody;
import com.gtan.spring.entity.User;
import com.gtan.spring.web.ModelAndView;

/**
 * HelloController 示例控制器：演示 Spring MVC 的三种主要响应方式。
 * 
 * <p>底层原理说明：</p>
 * <ul>
 *   <li>该类展示了 Spring MVC 的核心特性：请求映射、参数绑定、数据处理和响应生成</li>
 *   <li>通过 @Controller 和 @Component 双重注解，实现了：</li>
 *   <ul>
 *     <li>IoC 容器管理：@Component 确保该类被实例化并注册为 Bean</li>
 *     <li>MVC 控制器识别：@Controller 标识该类为请求处理器</li>
 *   </ul>
 *   <li>@RequestMapping("/hello") 定义了该控制器的统一路径前缀</li>
 *   <li>方法级别的 @RequestMapping 定义了具体的子路径</li>
 * </ul>
 * 
 * <p>三种响应模式演示：</p>
 * <ul>
 *   <li>纯文本响应：直接返回字符串作为响应内容</li>
 *   <li>JSON 响应：使用 @ResponseBody 将对象序列化为 JSON</li>
 *   <li>视图渲染：使用 ModelAndView 进行服务器端页面渲染</li>
 * </ul>
 * 
 * <p>参数绑定机制：</p>
 * <ul>
 *   <li>@RequestParam 自动将 HTTP 请求参数绑定到方法参数</li>
 *   <li>支持类型转换：字符串自动转换为 Integer、Long 等目标类型</li>
 *   <li>参数验证：缺少必要参数时会抛出异常</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see Controller
 * @see RequestMapping
 * @see ResponseBody
 * @see ModelAndView
 */
@Controller
@Component
@RequestMapping("/hello")
public class HelloController {

    @RequestMapping("/a")
    public String hello(@RequestParam("name") String name, @RequestParam("age") Integer age) {
        // 纯文本响应模式：直接返回字符串作为 HTTP 响应体
        // DispatcherServlet 会将返回的字符串直接写入响应流，不经过视图解析
        return String.format("hello %s, your age is %d", name, age);
    }

    @RequestMapping("/json")
    @ResponseBody
    public User json(@RequestParam("name") String name, @RequestParam("age") Integer age) {
        // JSON 响应模式：@ResponseBody 将对象自动序列化为 JSON
        // 底层机制：DispatcherServlet 使用 HttpMessageConverter 将 User 对象转换为 JSON 格式
        // 响应头会自动设置为 Content-Type: application/json
        User user = new User();
        user.setName(name);
        user.setAge(age);
        return user; // 注意：实际项目中需要 JSON 序列化库（如 Jackson）
    }

    @RequestMapping("/html")
    public ModelAndView html(@RequestParam("name") String name, @RequestParam("age") Integer age) {
        // 视图渲染模式：使用 ModelAndView 进行服务器端模板渲染
        // 底层机制：DispatcherServlet 将数据传递给视图解析器，渲染 HTML 页面
        // 工作流程：
        // 1. 创建 ModelAndView 对象，封装视图名称和数据模型
        // 2. 将请求参数添加到模型上下文中
        // 3. 视图解析器根据视图名称定位模板文件（index.html）
        // 4. 模板引擎将模型数据填充到模板中，生成最终 HTML
        System.out.println("这是返回ModelAndView的Controller方法");
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setView("index.html"); // 指定视图模板文件
        modelAndView.getContext().put("name", name); // 添加模型数据
        modelAndView.getContext().put("age", age.toString()); // 所有数据都作为字符串传递
        return modelAndView; // 返回给 DispatcherServlet 进行视图渲染
    }

}
