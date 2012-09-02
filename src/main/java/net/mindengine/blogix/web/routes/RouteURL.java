package net.mindengine.blogix.web.routes;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

public class RouteURL {
    
    private String urlPattern;
    private String originalUrl;
    private Pattern pattern;
    
    // List of names that would match the corresponding regex group.
    // e.g. Given a following url
    //         /article/{date}/{title}
    // will be replaced to following regex
    //          /article/[a-zA-Z0-9\_\-]*/[a-zA-Z0-9\_\-]*
    // so first it will init parameters list with parameters name in same order as they are defined in rout url
    // and in the end will take each regex matched group, extract the value and associate it with corresponding parameter in list
    private List<String> parameters = new LinkedList<String>();

    public RouteURL() {
    }
    
    public RouteURL(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

    public List<String> getParameters() {
        return parameters;
    }

    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }
    
    public Pattern asRegexPattern() {
        if ( pattern == null ) {
            pattern = Pattern.compile( getUrlPattern() );
        }
        return pattern;
    }

    public String getOriginalUrl() {
        return originalUrl;
    }

    public void setOriginalUrl(String originalUrl) {
        this.originalUrl = originalUrl;
    }

}
