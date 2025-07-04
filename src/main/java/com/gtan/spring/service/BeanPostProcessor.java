package com.gtan.spring.service;

/**
 * BeanPostProcessor 接口：提供 Bean 在 Spring IoC 容器实例化完成前后进行自定义处理的钩子（hook）。
 * <p>
 * 在 Bean 初始化流程中，容器会在调用用户定义的初始化方法（如 @PostConstruct 或 InitializingBean 接口方法）
 * 之前调用 beforeInitializeBean，之后调用 afterInitializeBean。
 * 实现该接口可用于在容器管理的 Bean 初始化前后执行额外逻辑，如代理包装、属性校验等。
 * </p>
 *
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 */
public interface BeanPostProcessor {

    /**
     * 在 Bean 初始化方法调用之前执行，可返回原始 Bean 或包装后的代理对象。
     * <p>
     * 典型用途包括：生成动态代理、修改属性值、添加 AOP 切面等。
     * </p>
     *
     * @param bean     容器中待初始化的 Bean 实例
     * @param beanName Bean 在容器中的名称
     * @return 用于后续初始化流程的 Bean 对象，可能是传入的 bean 本身或其代理/包装对象
     */
    default Object beforeInitializeBean(Object bean, String beanName) {
        return bean;
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
    default Object afterInitializeBean(Object bean, String beanName) {
        return bean;
    }
}
