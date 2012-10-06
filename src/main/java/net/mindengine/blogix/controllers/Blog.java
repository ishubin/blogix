package net.mindengine.blogix.controllers;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;
import net.mindengine.blogix.model.Post;

public class Blog {
    private static final String CURRENT_PAGE = "currentPage";
    private static final String HOME_POSTS = "homePosts";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static final String BLOGIX_FILEDB_PATH = "blogix.filedb.path";
    private static FileDb<Post> postsDb = createPostsDb(); 
    
    public static Map<String, Object> homeFirstPage() {
        Map<String, Object> model = loadCommonPostData();
        loadHomePage(model, 1);
        return model;
    }

    public static  Map<String, Object> homePage(Integer page) {
        Map<String, Object> model = loadCommonPostData();
        loadHomePage(model, page);
        return model;
    }

    private static Map<String, Object> loadCommonPostData() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("recentPosts", loadRecentPosts());
        return model;
    }

    private static List<Post> loadRecentPosts() {
        return postsDb.findAll().sortDesc().first(5).asJavaList();
    }

    public static  Map<String, Object> post(String postId) {
        Map<String, Object> model = loadCommonPostData();
        model.put("post", postsDb.findById(postId));
        return model;
    }

    public static  Map<String, Object> postsBySection(String sectionId) {
        Map<String, Object> model = loadCommonPostData();
        model.put("post", postsDb.findByFieldContaining("sections", sectionId));
        return null;
    }

    public static  Map<String, Object> postsBySectionAndPage(String string, int i) {
        // TODO Auto-generated method stub
        return null;
    }

    

    private static FileDb<Post> createPostsDb() {
        String filePath = getProperty(BLOGIX_FILEDB_PATH);
        if (filePath == null) {
            throw new IllegalArgumentException(BLOGIX_FILEDB_PATH + " property is not specified correctly");
        }
        return new FileDb<Post>(Post.class, new File(filePath + File.separator + "posts"));
    }

    private static void loadHomePage(Map<String, Object> model, int page) {
        EntryList<Post> allPosts = postsDb.findAll();
        model.put(ALL_POSTS_COUNT, allPosts.size());

        List<Post> homePosts = allPosts.sortDesc().page(page, 10).asJavaList();
        model.put(HOME_POSTS, homePosts);
        model.put(CURRENT_PAGE, 1);
    }

    private static String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }
}
