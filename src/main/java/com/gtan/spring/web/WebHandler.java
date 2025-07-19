package com.gtan.spring.web;

import com.gtan.spring.annotation.ResponseBody;
import com.gtan.spring.enumeration.ResultType;

import java.lang.reflect.Method;

/**
 * @author gangtann@126.com
 * @version 1.0
 * @since 2025-07-04
 */
public class WebHandler {

    private final Object controllerBean;

    private final Method method;

    private final ResultType resultType;

    public WebHandler(Object controllerBean, Method method) {
        this.controllerBean = controllerBean;
        this.method = method;
        this.resultType = resolveResultType(controllerBean, method);
    }

    private ResultType resolveResultType(Object controllerBean, Method method) {
        if (method.isAnnotationPresent(ResponseBody.class)) {
            return ResultType.JSON;
        }
        if (method.getReturnType() == ModelAndView.class) {
            return ResultType.LOCAL;
        }
        return ResultType.HTML;
    }

    public Object getControllerBean() {
        return controllerBean;
    }

    public Method getMethod() {
        return method;
    }

    public ResultType getResultType() {
        return resultType;
    }
}
