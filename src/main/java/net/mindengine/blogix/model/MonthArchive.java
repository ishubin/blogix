package net.mindengine.blogix.model;

import java.util.LinkedList;
import java.util.List;

public class MonthArchive {
    private int month;
    private List<Post> posts;
    
    public MonthArchive() {
    }
    public MonthArchive(int month) {
        this.month = month;
    }
    public int getMonth() {
        return month;
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
