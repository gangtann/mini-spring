package com.gtan.spring.web;

import com.alibaba.fastjson2.JSONObject;
import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.Controller;
import com.gtan.spring.annotation.PostConstruct;
import com.gtan.spring.annotation.RequestMapping;
import com.gtan.spring.annotation.RequestParam;
import com.gtan.spring.interceptor.Interceptor;
import com.gtan.spring.interceptor.InterceptorRegistry;
import com.gtan.spring.service.BeanPostProcessor;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * DispatcherServlet - Spring MVC 的前端控制器核心实现
 * 
 * <p>底层原理说明：</p>
 * <p>DispatcherServlet 是 Spring MVC 框架的核心组件，实现了前端控制器模式，负责：</p>
 * 
 * <ul>
 *   <li><strong>请求路由分发</strong>：根据 URL 将请求分发到对应的控制器方法</li>
 *   <li><strong>参数绑定</strong>：将 HTTP 请求参数自动绑定到方法参数</li>
 *   <li><strong>类型转换</strong>：支持基本数据类型的自动转换（String -> int、Integer 等）</li>
 *   <li><strong>视图解析</strong>：根据返回类型选择合适的响应处理方式</li>
 *   <li><strong>异常处理</strong>：统一的异常处理和错误响应</li>
 * </ul>
 * 
 * <p>工作流程：</p>
 * <ol>
 *   <li>初始化阶段：通过 BeanPostProcessor 扫描所有 @Controller 类，建立 URL 到方法的映射</li>
 *   <li>请求处理：接收 HTTP 请求，查找对应的处理器方法</li>
 *   <li>参数解析：解析请求参数并绑定到方法参数</li>
 *   <li>方法调用：通过反射调用控制器方法</li>
 *   <li>结果处理：根据返回类型生成不同类型的响应</li>
 * </ol>
 * 
 * <p>支持的响应类型：</p>
 * <ul>
 *   <li><strong>HTML 响应</strong>：直接返回 HTML 字符串</li>
 *   <li><strong>JSON 响应</strong>：使用 FastJSON 将对象序列化为 JSON</li>
 *   <li><strong>模板渲染</strong>：支持简单的模板引擎（gtan{} 占位符替换）</li>
 * </ul>
 * 
 * <p>URL 映射机制：</p>
 * <ul>
 *   <li>类级别 @RequestMapping 作为基础路径前缀</li>
 *   <li>方法级别 @RequestMapping 作为具体路径</li>
 *   <li>完整 URL = 类路径 + 方法路径（如：/user/getById）</li>
 * </ul>
 * 
 * <p>参数绑定规则：</p>
 * <ul>
 *   <li>@RequestParam 注解：显式指定参数名</li>
 *   <li>默认参数名：使用方法参数名作为参数名</li>
 *   <li>类型转换：支持 String、Integer 等基本类型</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see WebHandler
 * @see Controller
 * @see RequestMapping
 * @see RequestParam
 */
@Component
public class DispatcherServlet extends HttpServlet implements BeanPostProcessor {

    private static final Pattern PATTERN = Pattern.compile("gtan\\{(.*?)}");

    private Map<String, WebHandler> handlerMap = new HashMap<>();

    private final InterceptorRegistry interceptorRegistry = new InterceptorRegistry();

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebHandler handler = findHandler(req);
        if (handler == null) {
            resp.setContentType("text/html;charset=UTF-8");
            resp.getWriter().write("<h1>Error! 你的请求没有对应的处理器！</h1> <br>");
            return;
        }

        Exception dispatchException = null;
        ModelAndView modelAndView = null;
        
        try {
            // 1. 执行前置拦截器
            if (!applyPreHandle(req, resp, handler)) {
                return; // 被拦截器中断
            }
            
            // 2. 执行实际的Controller方法
            Object controllerBean = handler.getControllerBean();
            Object[] args = resolveArgs(req, handler.getMethod());
            Object result = handler.getMethod().invoke(controllerBean, args);

            // 3. 处理返回结果
            modelAndView = processResult(result);
            
            // 4. 执行后置拦截器
            applyPostHandle(req, resp, handler, modelAndView);
            
            // 5. 渲染视图并执行完成拦截器
            render(modelAndView, req, resp, handler);
            
        } catch (Exception ex) {
            dispatchException = ex;
            handleDispatchException(req, resp, handler, dispatchException);
        } finally {
            // 6. 执行完成拦截器（无论成功或失败都会执行）
            try {
                triggerAfterCompletion(req, resp, handler, dispatchException);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
    
    /**
     * 执行所有前置拦截器
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @return 是否继续执行
     * @throws Exception 拦截器异常
     */
    private boolean applyPreHandle(HttpServletRequest request, HttpServletResponse response, 
                                  WebHandler handler) throws Exception {
        for (Interceptor interceptor : interceptorRegistry.getInterceptors()) {
            if (!interceptor.preHandle(request, response, handler)) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * 执行所有后置拦截器
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param modelAndView 模型视图对象
     * @throws Exception 拦截器异常
     */
    private void applyPostHandle(HttpServletRequest request, HttpServletResponse response,
                                WebHandler handler, ModelAndView modelAndView) throws Exception {
        // 逆序执行后置拦截器
        List<Interceptor> interceptors = interceptorRegistry.getInterceptors();
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).postHandle(request, response, handler, modelAndView);
        }
    }
    
    /**
     * 执行完成拦截器
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param exception 异常对象
     * @throws Exception 拦截器异常
     */
    private void triggerAfterCompletion(HttpServletRequest request, HttpServletResponse response,
                                       WebHandler handler, Exception exception) throws Exception {
        // 逆序执行完成拦截器
        List<Interceptor> interceptors = interceptorRegistry.getInterceptors();
        for (int i = interceptors.size() - 1; i >= 0; i--) {
            interceptors.get(i).afterCompletion(request, response, handler, exception);
        }
    }

    /**
     * 处理Controller方法返回结果
     *
     * @param result 方法返回结果
     * @return ModelAndView对象
     */
    private ModelAndView processResult(Object result) {
        if (result == null) {
            return null;
        }

        if (result instanceof ModelAndView) {
            return (ModelAndView) result;
        }

        // 根据返回类型创建对应的ModelAndView
        ModelAndView modelAndView = new ModelAndView();
        if (result instanceof String) {
            modelAndView.setView("text");
            modelAndView.getContext().put("content", result.toString());
        } else {
            modelAndView.setView("json");
            modelAndView.getContext().put("data", JSONObject.toJSONString(result));
        }
        return modelAndView;
    }
    
    /**
     * 渲染视图
     * 
     * @param modelAndView 模型视图对象
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @throws Exception 渲染异常
     */
    private void render(ModelAndView modelAndView, HttpServletRequest request, HttpServletResponse response,
                       WebHandler handler) throws Exception {
        if (modelAndView == null) {
            return;
        }
        
        String view = modelAndView.getView();
        Map<String, String> context = modelAndView.getContext();
        
        switch (handler.getResultType()) {
            case HTML -> {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(context.getOrDefault("content", ""));
            }
            case JSON -> {
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(JSONObject.toJSONString(context.get("data")));
            }
            case LOCAL -> {
                InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(view);
                if (resourceAsStream != null) {
                    try (resourceAsStream) {
                        String html = new String(resourceAsStream.readAllBytes());
                        html = renderTemplate(html, context);
                        response.setContentType("text/html;charset=UTF-8");
                        response.getWriter().write(html);
                    }
                }
            }
        }
    }
    
    /**
     * 处理调度异常
     * 
     * @param request HTTP请求
     * @param response HTTP响应
     * @param handler 处理器
     * @param exception 异常对象
     * @throws IOException IO异常
     */
    private void handleDispatchException(HttpServletRequest request, HttpServletResponse response,
                                        WebHandler handler, Exception exception) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        response.getWriter().write("<h1>服务器内部错误: " + exception.getMessage() + "</h1>");
    }
    
    /**
     * 获取拦截器注册中心
     * 
     * @return 拦截器注册中心
     */
    public InterceptorRegistry getInterceptorRegistry() {
        return interceptorRegistry;
    }

    private String renderTemplate(String template, Map<String, String> context) {
        Matcher matcher = PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder();
        while (matcher.find()) {
            String key = matcher.group(1);
            String value = context.getOrDefault(key, "");
            matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private Object[] resolveArgs(HttpServletRequest req, Method method) {
        Parameter[] parameters = method.getParameters();
        Object[] args = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            RequestParam requestParam = parameter.getAnnotation(RequestParam.class);
            String value;
            if (requestParam != null) {
                value = req.getParameter(requestParam.value());
            } else {
                value = req.getParameter(parameter.getName());
            }

            Class<?> parameterType = parameter.getType();
            if (String.class.isAssignableFrom(parameterType)) {
                args[i] = value;
            } else if (Integer.class.isAssignableFrom(parameterType)) {
                args[i] = Integer.parseInt(value);
            } else {
                args[i] = null;
            }
        }
        return args;
    }

    private WebHandler findHandler(HttpServletRequest req) {
        return handlerMap.get(req.getRequestURI());
    }

    /**
     * 在 Bean 初始化方法调用之后执行，可返回原始 Bean 或替换后的 Bean 对象。
     * <p>
     * 典型用途包括：执行额外验证、记录初始化日志、对代理对象进行增强等。
     * </p>
     *
     * @param bean     已完成初始化的 Bean 实例
     * @param beanName Bean 在容器中的名称
     * @return 最终要注册到容器中的 Bean 对象，可能是传入的 bean 本身或其包装/代理对象
     */
    @Override
    public Object afterInitializeBean(Object bean, String beanName) {
        if (Interceptor.class.isAssignableFrom(bean.getClass())) {
            interceptorRegistry.addInterceptor((Interceptor) bean);
            return bean;
        }
        if (!bean.getClass().isAnnotationPresent(Controller.class)) {
            return bean;
        }
        RequestMapping classRequestMapping = bean.getClass().getDeclaredAnnotation(RequestMapping.class);
        String classUrl = classRequestMapping != null ? classRequestMapping.value() : "";
        Arrays.stream(bean.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(RequestMapping.class))
                .forEach(method -> {
                    RequestMapping methodRequestMapping = method.getDeclaredAnnotation(RequestMapping.class);
                    String methodUrl = methodRequestMapping != null ? methodRequestMapping.value() : "";
                    String url = classUrl.concat(methodUrl);
                    if (handlerMap.containsKey(url)) {
                        throw new RuntimeException("url: " + url + " 已被其他方法注册！");
                    }
                    handlerMap.put(url, new WebHandler(bean, method));
                });
        return bean;
    }
}
