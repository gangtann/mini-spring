package com.gtan.spring.service;

import com.gtan.spring.annotation.Component;
import com.gtan.spring.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * ApplicationContext - 手写简化版 Spring IoC 容器核心实现
 * 
 * <p>底层原理说明：</p>
 * <p>这是整个 Spring IoC 容器的核心实现，模拟了 Spring 框架的以下关键机制：</p>
 * 
 * <ul>
 *   <li><strong>组件扫描机制</strong>：通过扫描指定包路径，自动发现带有 @Component 注解的类</li>
 *   <li><strong>Bean 定义注册</strong>：将扫描到的类封装为 BeanDefinition，包含类的元数据信息</li>
 *   <li><strong>依赖注入</strong>：通过 @Autowired 注解实现自动装配，支持按类型注入</li>
 *   <li><strong>生命周期管理</strong>：支持 @PostConstruct 注解的初始化方法回调</li>
 *   <li><strong>Bean 后置处理器</strong>：实现 BeanPostProcessor 接口，支持 Bean 初始化前后的增强处理</li>
 *   <li><strong>单例模式</strong>：默认采用单例模式管理 Bean 实例，确保线程安全</li>
 * </ul>
 * 
 * <p>核心工作流程：</p>
 * <ol>
 *   <li><strong>扫描阶段</strong>：递归扫描指定包下的所有 .class 文件</li>
 *   <li><strong>过滤阶段</strong>：识别带有 @Component 注解的类，过滤出需要管理的 Bean</li>
 *   <li><strong>注册阶段</strong>：为每个符合条件的类创建 BeanDefinition 并注册</li>
 *   <li><strong>实例化阶段</strong>：根据 BeanDefinition 创建 Bean 实例</li>
 *   <li><strong>注入阶段</strong>：处理 @Autowired 注解，完成依赖注入</li>
 *   <li><strong>初始化阶段</strong>：调用 @PostConstruct 方法和 BeanPostProcessor</li>
 * </ol>
 * 
 * <p>数据结构说明：</p>
 * <ul>
 *   <li><strong>beanDefinitionMap</strong>：存储 Bean 的元数据定义，key 为 Bean 名称</li>
 *   <li><strong>ioc</strong>：存储已实例化的单例 Bean，实现缓存机制</li>
 *   <li><strong>loadingIoc</strong>：临时存储正在创建中的 Bean，解决循环依赖问题</li>
 *   <li><strong>beanPostProcessors</strong>：存储所有 Bean 后置处理器实例</li>
 * </ul>
 * 
 * <p>异常处理：</p>
 * <ul>
 *   <li>循环依赖检测：通过 loadingIoc 避免无限递归创建</li>
 *   <li>重复 Bean 名称检测：确保每个 Bean 名称唯一</li>
 *   <li>反射异常处理：包装并抛出运行时异常</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 * @see BeanDefinition
 * @see BeanPostProcessor
 * @see Component
 * @see Autowired
 */
public class ApplicationContext {

    /**
     * 已完成初始化并注册到容器的单例 Bean 缓存
     */
    private Map<String, Object> ioc = new HashMap<>();

    /**
     * 正在创建中 Bean 缓存，用于解决循环依赖
     */
    private Map<String, Object> loadingIoc = new HashMap<>();

    /**
     * 扫描到的 Bean 定义元数据映射，key 为 Bean 名称
     */
    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    /**
     * 所有 BeanPostProcessor 实例列表，用于 Bean 初始化前后增强
     */
    private List<BeanPostProcessor> beanPostProcessors = new ArrayList<>();

    /**
     * 构造器：接受根包名，启动容器初始化
     *
     * @param packageName 要扫描的基础包路径
     * @throws Exception 扫描或实例化过程中可能抛出多种反射异常
     */
    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    /**
     * 获取指定名称的 Bean 实例
     *
     * @param beanName 在容器中注册的 Bean 名称
     * @return 对应的 Bean 实例或 null
     */
    public Object getBean(String beanName) {
        if (beanName == null) {
            return null;
        }
        // 已实例化的单例
        if (ioc.containsKey(beanName)) {
            return ioc.get(beanName);
        }
        // 延迟创建
        if (beanDefinitionMap.containsKey(beanName)) {
            return createBean(beanDefinitionMap.get(beanName));
        }
        return null;
    }

    /**
     * 根据类型获取单个 Bean 实例
     *
     * @param beanType Bean 的 Class 类型
     * @param <T>      泛型
     * @return 匹配的 Bean 实例或 null
     */
    public <T> T getBean(Class<T> beanType) {
        String beanName = this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .findFirst()
                .orElse(null);
        return (T) getBean(beanName);
    }

    /**
     * 根据类型获取所有匹配 Bean 实例列表
     *
     * @param beanType Bean 的 Class 类型
     * @param <T>      泛型
     * @return Bean 实例列表
     */
    public <T> List<T> getBeans(Class<T> beanType) {
        return this.beanDefinitionMap.values().stream()
                .filter(beanDefinition -> beanType.isAssignableFrom(beanDefinition.getBeanType()))
                .map(BeanDefinition::getName)
                .map(this::getBean)
                .map(bean -> (T) bean)
                .toList();
    }

    /**
     * 核心初始化流程
     *
     * @param packageName 要扫描的基础包路径
     * @throws Exception 扫描或反射操作异常
     */
    public void initContext(String packageName) throws Exception {
        // 1. 扫描包并生成 BeanDefinition
        scanPackage(packageName).stream()
                .filter(this::canCreate)
                .forEach(this::wrapper);
        // 2. 初始化 BeanPostProcessor
        initBeanPostProcessor();
        // 3. 实例化所有 Bean
        beanDefinitionMap.values().forEach(this::createBean);
    }

    /**
     * 扫描所有 BeanPostProcessor 并实例化
     */
    private void initBeanPostProcessor() {
        beanDefinitionMap.values().stream()
                .filter(beanDefinition -> BeanPostProcessor.class.isAssignableFrom(beanDefinition.getBeanType()))
                .map(this::createBean)
                .map(beanPostProcessor -> (BeanPostProcessor) beanPostProcessor)
                .forEach(beanPostProcessors::add);
    }

    /**
     * 递归扫描指定包下的所有类 - 实现类路径扫描的核心机制
     *
     * @param packageName 基础包路径（如：com.gtan.spring）
     * @return 扫描到的所有类的 Class 对象列表
     * @throws Exception 文件系统访问异常或类加载异常
     */
    public List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        String pkgPath = packageName.replace(".", File.separator);
        URL resource = this.getClass().getClassLoader().getResource(pkgPath);
        Path root = Paths.get(resource.toURI());
        Files.walkFileTree(root, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String pathStr = file.toAbsolutePath().toString();
                if (pathStr.endsWith(".class")) {
                    String className = pathStr
                            .replace(File.separatorChar, '.')
                            .replaceAll("^.*" + packageName, packageName)
                            .replaceAll("\\.class$", "");
                    try {
                        classList.add(Class.forName(className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
                return FileVisitResult.CONTINUE;
            }
        });
        return classList;
    }

    /**
     * 判断类是否带有 @Component 注解
     *
     * @param type 要检查的 Class
     * @return 如果标注 Component 则返回 true
     */
    protected boolean canCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    /**
     * 将扫描到的类包装为 BeanDefinition 并缓存
     *
     * @param type 带 Component 注解的 Class
     * @return 生成的 BeanDefinition
     */
    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("Duplicate bean name: " + beanDefinition.getName());
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    /**
     * 根据 BeanDefinition 创建或获取 Bean 实例
     *
     * @param beanDefinition Bean 的定义元数据
     * @return Bean 实例
     */
    protected Object createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return ioc.get(name);
        }
        if (loadingIoc.containsKey(name)) {
            return loadingIoc.get(name);
        }
        return doCreateBean(beanDefinition);
    }

    /**
     * 真正的 Bean 实例化、注入和初始化流程
     */
    private Object doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        try {
            // 1. 实例化
            Object bean = constructor.newInstance();
            loadingIoc.put(beanDefinition.getName(), bean);
            // 2. 属性注入
            autowiredBean(bean, beanDefinition);
            // 3. 初始化前后钩子及 PostConstruct 方法
            bean = initializeBean(bean, beanDefinition);
            // 4. 注册到单例缓存
            loadingIoc.remove(beanDefinition.getName());
            ioc.put(beanDefinition.getName(), bean);
            return bean;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 调用 BeanPostProcessor 和 @PostConstruct 方法
     */
    private Object initializeBean(Object bean, BeanDefinition beanDefinition) throws InvocationTargetException,
            IllegalAccessException {
        // beforeInitialize 钩子
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.beforeInitializeBean(bean, beanDefinition.getName());
        }
        // @PostConstruct 方法
        Method postConstructMethod = beanDefinition.getPostConstructMethod();
        if (postConstructMethod != null) {
            postConstructMethod.invoke(bean);
        }
        // afterInitialize 钩子
        for (BeanPostProcessor beanPostProcessor : beanPostProcessors) {
            bean = beanPostProcessor.afterInitializeBean(bean, beanDefinition.getName());
        }
        return bean;
    }

    /**
     * 执行字段注入，将所有 @Autowired 字段设置为对应 Bean 实例
     */
    private void autowiredBean(Object bean, BeanDefinition beanDefinition) throws IllegalAccessException {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
            autowiredField.set(bean, getBean(autowiredField.getType()));
        }
    }

}
