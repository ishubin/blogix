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

}
