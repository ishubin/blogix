package net.mindengine.blogix.model;

import java.util.Date;

import net.mindengine.blogix.db.Entry;

public class Post implements Comparable<Post> {

    private String id;
    private String title;
    private Boolean allowComments;
    private Date date;
    private String[] sections;
    private String externalUrl;
    private Entry entry;
    private String displayExternalUrl;

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
        return getEntry().body();
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

    public String[] getSections() {
        return sections;
    }

    public void setSections(String[] sections) {
        this.sections = sections;
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
