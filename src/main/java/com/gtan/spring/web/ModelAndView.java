package com.gtan.spring.web;

import java.util.HashMap;
import java.util.Map;

/**
 * ModelAndView - Spring MVC 的视图模型封装类
 * 
 * <p>底层原理说明：</p>
 * <p>ModelAndView 是 Spring MVC 中连接控制器和视图的核心桥梁，实现了 MVC 模式中 Model 和 View 的解耦：</p>
 * 
 * <ul>
 *   <li><strong>视图名称</strong>：指定要渲染的视图模板文件（如：index.html）</li>
 *   <li><strong>数据模型</strong>：存储需要在视图中使用的数据（键值对形式）</li>
 *   <li><strong>类型安全</strong>：所有数据都以字符串形式传递，确保模板渲染的兼容性</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>服务器端模板渲染（如：Thymeleaf、Freemarker、JSP 等）</li>
 *   <li>静态 HTML 模板 + 动态数据填充</li>
 *   <li>需要返回完整 HTML 页面的请求</li>
 * </ul>
 * 
 * <p>工作流程：</p>
 * <ol>
 *   <li>控制器创建 ModelAndView 实例</li>
 *   <li>设置视图名称（模板文件路径）</li>
 *   <li>添加模型数据到上下文</li>
 *   <li>返回给 DispatcherServlet 进行视图渲染</li>
 *   <li>模板引擎将数据填充到模板中生成最终 HTML</li>
 * </ol>
 * 
 * <p>数据存储特点：</p>
 * <ul>
 *   <li>使用 HashMap 存储键值对，支持快速查找</li>
 *   <li>键为字符串类型，对应模板中的占位符名称</li>
 *   <li>值为字符串类型，确保模板渲染的兼容性</li>
 *   <li>支持任意数量的数据项添加</li>
 * </ul>
 * 
 * <p>示例用法：</p>
 * <pre>
 * ModelAndView modelAndView = new ModelAndView();
 * modelAndView.setView("user/profile.html");
 * modelAndView.getContext().put("username", "张三");
 * modelAndView.getContext().put("age", "25");
 * return modelAndView;
 * </pre>
 * 
 * <p>模板占位符示例（HTML 模板）：</p>
 * <pre>
 * &lt;html&gt;
 *   &lt;body&gt;
 *     &lt;h1&gt;欢迎，gtan{username}！&lt;/h1&gt;
 *     &lt;p&gt;年龄：gtan{age}&lt;/p&gt;
 *   &lt;/body&gt;
 * &lt;/html&gt;
 * </pre>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see DispatcherServlet
 */
public class ModelAndView {

    private String view;

    private Map<String, String> context = new HashMap<>();

    public String getView() {
        return view;
    }

    public void setView(String view) {
        this.view = view;
    }

    public Map<String, String> getContext() {
        return context;
    }
}
