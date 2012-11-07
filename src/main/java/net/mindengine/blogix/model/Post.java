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

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.mindengine.blogix.db.Entry;

public class Post implements Comparable<Post> {

    private String id;
    private String title;
    private Boolean allowComments;
    private Date date;
    private String[] categories;
    private String externalUrl;
    private Entry entry;
    private String displayExternalUrl;
    
    Pattern CONTENT_PREVIEW_BREAKER_PATTERN = Pattern.compile("\\n[>]+[\\s]*\\n");

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getContent() {
        if (entry == null) {
            throw new IllegalArgumentException("Entry is not set for post");
        }
        return stripContentPreviewBreakers(getEntry().body());
    }

    public String getContentPreview() {
        String content = getEntry().body();
        Matcher matcher = CONTENT_PREVIEW_BREAKER_PATTERN.matcher(content);
        if (matcher.find()) {
            int index = matcher.start();
            if (index > 0 && index < content.length()) {
                return content.substring(0, index);
            }    
        }
        return content;
    }
    
    private String stripContentPreviewBreakers(String content) {
        return CONTENT_PREVIEW_BREAKER_PATTERN.matcher(content).replaceAll("\n");
    }

    public Boolean getAllowComments() {
        return allowComments;
    }

    public void setAllowComments(Boolean allowComments) {
        this.allowComments = allowComments;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String[] getCategories() {
        return categories;
    }

    public void setCategories(String[] categories) {
        this.categories = categories;
    }

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Entry getEntry() {
        return entry;
    }

    public void setEntry(Entry entry) {
        this.entry = entry;
    }

    public String field(String name) {
        if (getEntry() == null) {
            throw new IllegalArgumentException("Entry is not set for post");
        }
        return getEntry().field(name);
    }

    public String getDisplayExternalUrl() {
        return this.displayExternalUrl;
    }

    public void setDisplayExternalUrl(String displayExternalUrl) {
        this.displayExternalUrl = displayExternalUrl;
    }

    @Override
    public int compareTo(Post otherPost) {
        return id.compareTo(otherPost.id);
    }
    
    @Override
    public String toString() {
        return "Post: " + id;
    }

}
