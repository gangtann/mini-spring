# spring-framework

手写简化版 Spring IoC 容器 ApplicationContext：负责扫描指定包下的组件，生成 BeanDefinition，管理 Bean 的实例化、属性注入及生命周期回调。

1. 扫描 packageName 下所有带 @Component 注解的类 (scanPackage、scanCreate)
2. 将扫描到的类包装为 BeanDefinition 并缓存 (wrapper)
3. 初始化所有 BeanPostProcessor 实例 (initBeanPostProcessor)
4. 依赖注入并实例化所有 Bean (createBean、doCreateBean)