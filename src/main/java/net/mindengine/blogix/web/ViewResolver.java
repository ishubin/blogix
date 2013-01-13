/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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
package net.mindengine.blogix.web;

import java.io.OutputStream;
import java.util.Map;

import net.mindengine.blogix.Blogix;


public abstract class ViewResolver {
    
    private Blogix blogix;
    
    public ViewResolver(Blogix blogix) {
        this.setBlogix(blogix);
    }

    public abstract boolean canResolve(String view);
    public abstract void resolveViewAndRender(Map<String, Object> routeModel, Object objectModel, String view, OutputStream outputStream) throws Exception;

    public Blogix getBlogix() {
        return blogix;
    }

    public void setBlogix(Blogix blogix) {
        this.blogix = blogix;
    }

}
