package net.mindengine.blogix.web.routes;

public class Route {

    private RouteURL url;
    private ControllerDefinition controller;
    private String view;
    private RouteProviderDefinition provider;
    
    
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
    
    
}
