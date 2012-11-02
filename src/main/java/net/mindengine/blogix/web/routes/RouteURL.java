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

    public boolean isParameterized() {
        return parameters != null && !parameters.isEmpty();
    }

}
