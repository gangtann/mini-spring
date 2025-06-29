package com.gtan.spring;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
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
 * 手写 Spring
 * <p>
 * Spring 就负责两件事：一件是造对象，另一件是拿对象
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-06-29
 */
public class ApplicationContext {

    private Map<String, Object> ioc = new HashMap<>();

    private Map<String, BeanDefinition> beanDefinitionMap = new HashMap<>();

    public ApplicationContext(String packageName) throws Exception {
        initContext(packageName);
    }

    public Object getBean(String beanName) {
        Object bean = ioc.get(beanName);
        if (bean != null) {
            return bean;
        }
        return null;
    }

    public <T> T getBean(Class<T> beanType) {
        return this.ioc.values().stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean)
                .findAny()
                .orElseGet(null);
    }

    public <T> List<T> getBeans(Class<T> beanType) {
        return this.ioc.values().stream()
                .filter(bean -> beanType.isAssignableFrom(bean.getClass()))
                .map(bean -> (T) bean)
                .toList();
    }

    /**
     * 1. 造什么对象
     * 2. 怎么造对象：
     * 2.1 如果构造函数有参数，参数从哪来；
     * 2.2 如果属性自动注入，属性从哪来；
     * 2.3 如果对象定义生命周期函数，生命周期由谁执行
     */
    public void initContext(String packageName) throws Exception {
        scanPackage(packageName).stream().filter(this::scanCreate).map(this::wrapper).forEach(this::createBean);
    }

    public List<Class<?>> scanPackage(String packageName) throws Exception {
        List<Class<?>> classList = new ArrayList<>();
        URL resource = this.getClass().getClassLoader().getResource(packageName.replace(".", File.separator));
        Path path = Paths.get(resource.toURI());
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Path absolutePath = file.toAbsolutePath();
                if (absolutePath.toString().endsWith(".class")) {
                    String className = absolutePath.toString().replace(File.separator, ".");
                    int packageIndex = className.indexOf(packageName);
                    className = className.substring(packageIndex, className.length() - ".class".length());
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
     * protected：继承类可以访问，重写该函数，自定义什么类支持加载
     *
     * @param type 类型
     * @return 是否扫描创建
     */
    protected boolean scanCreate(Class<?> type) {
        return type.isAnnotationPresent(Component.class);
    }

    protected BeanDefinition wrapper(Class<?> type) {
        BeanDefinition beanDefinition = new BeanDefinition(type);
        if (beanDefinitionMap.containsKey(beanDefinition.getName())) {
            throw new RuntimeException("bean name is exists");
        }
        beanDefinitionMap.put(beanDefinition.getName(), beanDefinition);
        return beanDefinition;
    }

    protected void createBean(BeanDefinition beanDefinition) {
        String name = beanDefinition.getName();
        if (ioc.containsKey(name)) {
            return;
        }
        doCreateBean(beanDefinition);
    }

    private void doCreateBean(BeanDefinition beanDefinition) {
        Constructor<?> constructor = beanDefinition.getConstructor();
        Object bean = null;
        try {
            bean = constructor.newInstance();
            autowiredBean(bean, beanDefinition);
            Method postConstructMethod = beanDefinition.getPostConstructMethod();
            if (postConstructMethod != null) {
                postConstructMethod.invoke(bean);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        ioc.put(beanDefinition.getName(), bean);
    }

    private void autowiredBean(Object bean, BeanDefinition beanDefinition) {
        for (Field autowiredField : beanDefinition.getAutowiredFields()) {
            autowiredField.setAccessible(true);
        }

    }

}
