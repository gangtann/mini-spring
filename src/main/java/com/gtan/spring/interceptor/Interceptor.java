package com.gtan.spring.interceptor;

import com.gtan.spring.web.ModelAndView;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Interceptor 接口 - 定义MVC拦截器的核心行为
 *
 * <p>底层原理说明：</p>
 * <p>拦截器在MVC框架中扮演着"请求守门员"的角色，它允许在请求到达Controller之前
 * 和响应返回客户端之前进行拦截处理。这种设计模式实现了AOP（面向切面编程）的核心思想。</p>
 *
 * <p>工作流程：</p>
 * <ol>
 *   <li><strong>前置处理</strong>：在Controller方法执行前调用，可进行权限校验、日志记录等</li>
 *   <li><strong>后置处理</strong>：在Controller方法执行后调用，可进行结果增强、性能监控等</li>
 *   <li><strong>完成处理</strong>：在视图渲染完成后调用，可进行资源清理、最终日志记录等</li>
 * </ol>
 *
 * <p>与过滤器的区别：</p>
 * <ul>
 *   <li>拦截器更贴近业务，可以访问Spring上下文中的Bean</li>
 *   <li>拦截器可以精确控制到方法级别</li>
 *   <li>拦截器支持细粒度的异常处理</li>
 * </ul>
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-19
 */
public interface Interceptor {

    /**
     * 前置处理 - 在Controller方法执行前调用
     *
     * <p>典型应用场景：</p>
     * <ul>
     *   <li>用户身份认证和权限校验</li>
     *   <li>请求日志记录</li>
     *   <li>防重复提交校验</li>
     *   <li>参数预处理</li>
     *   <li>性能计时开始</li>
     * </ul>
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  目标处理器（Controller方法信息）
     * @return true继续执行，false中断请求
     * @throws Exception 处理异常时可抛出异常
     */
    boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;

    /**
     * 后置处理 - 在Controller方法执行后，视图渲染前调用
     *
     * <p>典型应用场景：</p>
     * <ul>
     *   <li>对返回结果进行统一包装</li>
     *   <li>添加响应头信息</li>
     *   <li>记录方法执行时间</li>
     *   <li>异常转换处理</li>
     *   <li>数据脱敏处理</li>
     * </ul>
     *
     * @param request      HTTP请求对象
     * @param response     HTTP响应对象
     * @param handler      目标处理器
     * @param modelAndView 模型和视图对象
     * @throws Exception 处理异常时可抛出异常
     */
    void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                    ModelAndView modelAndView) throws Exception;

    /**
     * 完成处理 - 在视图渲染完成后调用
     *
     * <p>典型应用场景：</p>
     * <ul>
     *   <li>资源清理</li>
     *   <li>最终日志记录</li>
     *   <li>性能监控结束</li>
     *   <li>异常统一处理</li>
     *   <li>缓存更新</li>
     * </ul>
     *
     * @param request  HTTP请求对象
     * @param response HTTP响应对象
     * @param handler  目标处理器
     * @param ex       异常对象（如果有的话）
     * @throws Exception 处理异常时可抛出异常
     */
    void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                         Object handler, Exception ex) throws Exception;
}