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
package net.mindengine.blogix;

/**
 * Used for models to store different information about current route
 * @author ishubin
 *
 */
public class BlogixData {

    private String currentUri;
    private String wayToRoot;
    public String getCurrentUri() {
        return currentUri;
    }
    public void setCurrentUri(String currentUri) {
        this.currentUri = currentUri;
    }
    public String getWayToRoot() {
        return wayToRoot;
    }
    public void setWayToRoot(String wayToRoot) {
        this.wayToRoot = wayToRoot;
    }
}
