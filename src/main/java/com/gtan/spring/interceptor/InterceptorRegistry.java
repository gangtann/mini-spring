package com.gtan.spring.interceptor;

import java.util.ArrayList;
import java.util.List;

/**
 * InterceptorRegistry - 拦截器注册和管理中心
 * 
 * <p>底层原理说明：</p>
 * <p>拦截器注册中心负责管理所有拦截器的生命周期，包括注册、排序和执行。
 * 它采用了责任链模式，将多个拦截器按顺序组织成一条处理链。</p>
 * 
 * <p>设计模式：</p>
 * <ul>
 *   <li><strong>责任链模式</strong>：多个拦截器按顺序执行，形成处理链</li>
 *   <li><strong>建造者模式</strong>：支持链式注册，提高代码可读性</li>
 *   <li><strong>策略模式</strong>：不同的拦截器实现不同的处理策略</li>
 * </ul>
 * 
 * <p>执行顺序：</p>
 * <ol>
 *   <li>按注册顺序执行preHandle()方法</li>
 *   <li>按注册逆序执行postHandle()方法</li>
 *   <li>按注册逆序执行afterCompletion()方法</li>
 * </ol>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-19
 */
public class InterceptorRegistry {
    
    /**
     * 注册的拦截器列表
     * 使用ArrayList保证顺序性
     */
    private final List<Interceptor> interceptors = new ArrayList<>();
    
    /**
     * 注册拦截器
     * 
     * @param interceptor 要注册的拦截器实例
     * @return this 支持链式调用
     */
    public InterceptorRegistry addInterceptor(Interceptor interceptor) {
        if (interceptor != null) {
            interceptors.add(interceptor);
        }
        return this;
    }
    
    /**
     * 获取所有注册的拦截器
     * 
     * @return 拦截器列表（不可修改）
     */
    public List<Interceptor> getInterceptors() {
        return new ArrayList<>(interceptors);
    }
    
    /**
     * 获取拦截器数量
     * 
     * @return 拦截器数量
     */
    public int size() {
        return interceptors.size();
    }
    
    /**
     * 清空所有拦截器
     * 
     * @return this 支持链式调用
     */
    public InterceptorRegistry clear() {
        interceptors.clear();
        return this;
    }
    
    /**
     * 检查是否为空
     * 
     * @return 是否没有注册任何拦截器
     */
    public boolean isEmpty() {
        return interceptors.isEmpty();
    }
}