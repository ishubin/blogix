package net.mindengine.blogix.web.routes;

import java.lang.reflect.Method;

public class RouteProviderDefinition {

    private Class<?> providerClass;
    private Method   providerMethod;
    public Class<?> getProviderClass() {
        return providerClass;
    }
    public void setProviderClass(Class<?> providerClass) {
        this.providerClass = providerClass;
    }
    public Method getProviderMethod() {
        return providerMethod;
    }
    public void setProviderMethod(Method providerMethod) {
        this.providerMethod = providerMethod;
    }
}
