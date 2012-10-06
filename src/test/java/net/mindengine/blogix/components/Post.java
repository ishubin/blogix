package net.mindengine.blogix.components;


public class Post implements Comparable<Post> {

    private String id;
    private String title;
    private String[] sections;
    private String body;
    private Boolean commentsEnabled;
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }
    public String[] getSections() {
        return sections;
    }
    public void setSections(String[] sections) {
        this.sections = sections;
    }
    public String getBody() {
        return body;
    }
    public void setBody(String body) {
        this.body = body;
    }
    public Boolean getCommentsEnabled() {
        return commentsEnabled;
    }
    public void setCommentsEnabled(Boolean commentsEnabled) {
        this.commentsEnabled = commentsEnabled;
    }
    @Override
    public int compareTo(Post otherParam) {
        return id.compareTo(otherParam.id);
    }

}
