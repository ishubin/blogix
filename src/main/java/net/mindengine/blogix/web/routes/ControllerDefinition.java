/*******************************************************************************
* Copyright 2012 Ivan Shubin http://mindengine.net
* 
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
* 
*   http://www.apache.org/licenses/LICENSE-2.0
* 
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
******************************************************************************/
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
