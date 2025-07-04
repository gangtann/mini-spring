package com.gtan.spring.service;

import com.gtan.spring.annotation.Component;

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
 * 手写简化版 Spring IoC 容器 ApplicationContext：
 * 负责扫描指定包下的组件，生成 BeanDefinition，
 * 管理 Bean 的实例化、属性注入及生命周期回调。
 * <p>
 * 核心流程：
 * 1. 扫描 packageName 下所有带 @Component 注解的类 (scanPackage、scanCreate)
 * 2. 将扫描到的类包装为 BeanDefinition 并缓存 (wrapper)
 * 3. 初始化所有 BeanPostProcessor 实例 (initBeanPostProcessor)
 * 4. 依赖注入并实例化所有 Bean (createBean、doCreateBean)
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
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
                .filter(this::scanCreate)
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
     * 递归扫描指定包下的所有类
     *
     * @param packageName 基础包路径
     * @return Class 列表
     * @throws Exception IO 或 Class loading 异常
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
    protected boolean scanCreate(Class<?> type) {
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
