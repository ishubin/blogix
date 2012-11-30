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

import java.util.Map;

public class Route {

    private RouteURL url;
    private ControllerDefinition controller;
    private String view;
    private RouteProviderDefinition provider;
    private Map<String, Object> model;
    
    
    public RouteURL getUrl() {
        return url;
    }
    public void setUrl(RouteURL url) {
        this.url = url;
    }
    public String getView() {
        return view;
    }
    public void setView(String view) {
        this.view = view;
    }
    public ControllerDefinition getController() {
        return controller;
    }
    public void setController(ControllerDefinition controller) {
        this.controller = controller;
    }
    public RouteProviderDefinition getProvider() {
        return provider;
    }
    public void setProvider(RouteProviderDefinition provider) {
        this.provider = provider;
    }
    public Map<String, Object> getModel() {
        return this.model;
    }
    public void setModel(Map<String, Object> model) {
        this.model = model;
    }
    
    
}
