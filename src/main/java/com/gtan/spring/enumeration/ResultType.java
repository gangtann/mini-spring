package com.gtan.spring.enumeration;

/**
 * ResultType - 控制器方法返回结果类型枚举
 * 
 * <p>底层原理说明：</p>
 * <p>该枚举定义了 DispatcherServlet 处理控制器方法返回值的不同策略：</p>
 * 
 * <ul>
 *   <li><strong>JSON</strong>：将返回值序列化为 JSON 格式，用于 RESTful API</li>
 *   <li><strong>HTML</strong>：将返回值作为纯 HTML 字符串直接响应</li>
 *   <li><strong>LOCAL</strong>：使用模板引擎渲染本地 HTML 模板文件</li>
 * </ul>
 * 
 * <p>设计目的：</p>
 * <ul>
 *   <li>统一响应处理策略，避免魔法字符串</li>
 *   <li>提供类型安全的返回类型标识</li>
 *   <li>简化 DispatcherServlet 中的类型判断逻辑</li>
 * </ul>
 * 
 * <p>使用场景：</p>
 * <ul>
 *   <li>WebHandler 中标识处理器方法的返回类型</li>
 *   <li>DispatcherServlet 中根据类型选择不同的响应处理方式</li>
 *   <li>支持扩展其他响应类型（如 XML、PDF 等）</li>
 * </ul>
 * 
 * <p>类型映射：</p>
 * <table border="1">
 *   <tr><th>枚举值</th><th>处理策略</th><th>Content-Type</th><th>使用场景</th></tr>
 *   <tr><td>JSON</td><td>FastJSON 序列化</td><td>application/json</td><td>RESTful API</td></tr>
 *   <tr><td>HTML</td><td>直接字符串响应</td><td>text/html</td><td>简单页面响应</td></tr>
 *   <tr><td>LOCAL</td><td>模板引擎渲染</td><td>text/html</td><td>复杂页面模板</td></tr>
 * </table>
 * 
 * <p>扩展性：</p>
 * <ul>
 *   <li>可以轻松添加新的枚举值支持更多响应类型</li>
 *   <li>保持向后兼容，不影响现有代码</li>
 * </ul>
 * 
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 * @see com.gtan.spring.web.DispatcherServlet
 * @see com.gtan.spring.web.WebHandler
 */
public enum ResultType {

    JSON,

    HTML,

    LOCAL;

}
