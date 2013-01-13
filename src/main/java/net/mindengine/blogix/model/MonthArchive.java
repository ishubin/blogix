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
package net.mindengine.blogix.model;

import java.util.LinkedList;
import java.util.List;

public class MonthArchive {
    private int month;
    private List<Post> posts;
    
    private final static String[] MONTH_SHORT_NAMES = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    private final static String[] MONTH_FULL_NAMES = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
    
    public MonthArchive() {
    }
    public MonthArchive(int month) {
        this.month = month;
    }
    public int getMonth() {
        return month;
    }
    public String getMonthShortName() {
        if (month >=0 && month < MONTH_SHORT_NAMES.length) {
            return MONTH_SHORT_NAMES[month];
        }
        else return "";
    }
    public String getMonthFullName() {
        if (month >=0 && month < MONTH_FULL_NAMES.length) {
            return MONTH_FULL_NAMES[month];
        }
        else return "";
    }
    public void setMonth(int month) {
        this.month = month;
    }
    public List<Post> getPosts() {
        return posts;
    }
    public void setPosts(List<Post> posts) {
        this.posts = posts;
    }
    public void addPost(Post post) {
        if (posts == null) {
            posts = new LinkedList<Post>();
        }
        posts.add(post);
    }

}
