package net.mindengine.blogix.web.routes;

import java.lang.reflect.Method;
import java.util.List;

public class ControllerDefinition {

    private Class<?> controllerClass;
    private Method controllerMethod;
    private List<String> parameters;
    public Class<?> getControllerClass() {
        return controllerClass;
    }
    public void setControllerClass(Class<?> controllerClass) {
        this.controllerClass = controllerClass;
    }
    public Method getControllerMethod() {
        return controllerMethod;
    }
    public void setControllerMethod(Method controllerMethod) {
        this.controllerMethod = controllerMethod;
    }
    public List<String> getParameters() {
        return parameters;
    }
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
}
