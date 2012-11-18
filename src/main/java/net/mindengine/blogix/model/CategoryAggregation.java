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
package net.mindengine.blogix.model;

import java.util.List;

public class CategoryAggregation {
    private Category category;
    private List<Post> recentPosts;
    public Category getCategory() {
        return category;
    }
    public void setCategory(Category category) {
        this.category = category;
    }
    public List<Post> getRecentPosts() {
        return recentPosts;
    }
    public void setRecentPosts(List<Post> recentPosts) {
        this.recentPosts = recentPosts;
    }
    
    public boolean hasPosts() {
        return recentPosts != null && recentPosts.size() > 0; 
    }

}
