package com.gtan.spring.web;

import com.gtan.spring.annotation.Autowired;
import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.catalina.Context;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.io.IOException;
import java.util.logging.LogManager;

/**
 * TomcatServer - 嵌入式 Tomcat 服务器配置和启动器
 * 
 * <p>底层原理说明：</p>
 * <p>该类实现了嵌入式 Tomcat 服务器的配置和启动，是 Spring Boot 嵌入式容器思想的简化实现：</p>
 * 
 * <ul>
 *   <li><strong>嵌入式设计</strong>：无需外部 Tomcat 安装，直接通过 Java 代码启动</li>
 *   <li><strong>自动配置</strong>：通过代码配置端口、上下文路径、Servlet 映射等</li>
 *   <li><strong>依赖注入</strong>：使用 @Autowired 注入 DispatcherServlet 实例</li>
 *   <li><strong>生命周期管理</strong>：通过 @PostConstruct 在 IoC 容器初始化后自动启动</li>
 * </ul>
 * 
 * <p>配置细节：</p>
 * <ul>
 *   <li><strong>端口配置</strong>：默认 8080 端口</li>
 *   <li><strong>上下文路径</strong>：空字符串，表示根路径访问</li>
 *   <li><strong>文档根目录</strong>：当前工作目录（项目根目录）</li>
 *   <li><strong>Servlet 映射</strong>：/* 匹配所有请求，实现前端控制器模式</li>
 * </ul>
 * 
 * <p>日志配置：</p>
 * <ul>
 *   <li>重置 JUL（java.util.logging）配置，避免控制台冗余日志</li>
 *   <li>安装 SLF4J 桥接器，将 Tomcat 日志重定向到 SLF4J</li>
 * </ul>
 * 
 * <p>启动流程：</p>
 * <ol>
 *   <li>创建 Tomcat 实例并配置基本参数</li>
 *   <li>创建 Web 应用上下文（Context）</li>
 *   <li>注册 DispatcherServlet 到上下文</li>
 *   <li>配置 URL 映射规则</li>
 *   <li>启动服务器并监听端口</li>
 * </ol>
 * 
 * <p>访问方式：</p>
 * <ul>
 *   <li>启动后可通过 http://localhost:8080 访问应用</li>
 *   <li>所有请求都会被 DispatcherServlet 处理</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see DispatcherServlet
 * @see Component
 * @see Autowired
 * @see PostConstruct
 */
@Component
public class TomcatServer {

    @Autowired
    private DispatcherServlet dispatcherServlet;

    @PostConstruct
    public void start() {
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();

        int port = 8080;
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(port);
        tomcat.getConnector();

        String contextPath = "";
        String docBase = new File(".").getAbsolutePath();
        Context context = tomcat.addContext(contextPath, docBase);

        tomcat.addServlet(contextPath, "dispatcherServlet", dispatcherServlet);
        context.addServletMappingDecoded("/*", "dispatcherServlet");
        try {
            tomcat.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Tomcat started on port " + port);
    }
    
    /**
     * 获取DispatcherServlet实例（用于配置拦截器）
     * 
     * @return DispatcherServlet实例
     */
    public DispatcherServlet getDispatcherServlet() {
        return dispatcherServlet;
    }

}
