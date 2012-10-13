package net.mindengine.blogix.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.Section;

public class Blog {
    private static final int DEFAULT_POSTS_PER_PAGE = 10;
    private static final String CURRENT_PAGE = "currentPage";
    private static final String HOME_POSTS = "homePosts";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static final String BLOGIX_FILEDB_PATH = "blogix.filedb.path";
    private static FileDb<Post> postsDb = createPostsDb(); 
    private static FileDb<Section> sectionsDb = createSectionDb();
    
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

    public static  Map<String, Object> post(String postId) {
        Map<String, Object> model = loadCommonPostData();
        model.put("post", postsDb.findById(postId));
        return model;
    }

    public static  Map<String, Object> postsBySection(String sectionId) {
        return postsBySectionAndPage(sectionId, 1);
    }

    public static  Map<String, Object> postsBySectionAndPage(String sectionId, Integer page) {
        Map<String, Object> model = loadCommonPostData();
        EntryList<Post> allPosts = postsDb.findByFieldContaining("sections", sectionId);
        model.put("allPostsCount", allPosts.size());
        model.put("posts", allPosts.page(page, DEFAULT_POSTS_PER_PAGE).asJavaList());
        model.put("currentPage", page);
        model.put("section", sectionsDb.findById(sectionId));
        return model;
    }
    
    public static File fileForPost(String postId, String fileName) throws FileNotFoundException {
        String fullAttachmentName = postId + "." + fileName;
        if (postsDb.findAttachments(postId + "." + fileName).asJavaList().contains(fullAttachmentName)) {
            return postsDb.findAttachmentAsFile(fullAttachmentName);
        }
        throw new FileNotFoundException("There is no '" + fileName + "' attachment for blog ");
    }
    
    public static Map<String, Object> rssFeedAll() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("posts", postsDb.findAll().asJavaList());
        return model;
    }
    
    public static Map<String, Object> rssFeedForSection(String sectionId) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("posts", postsDb.findByFieldContaining("sections", sectionId).asJavaList());
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

    private static FileDb<Post> createPostsDb() {
        return createDb(Post.class, "posts");
    }

    private static FileDb<Section> createSectionDb() {
        return createDb(Section.class, "sections");
    }

    private static <T2 extends Comparable<T2>> FileDb<T2> createDb(Class<T2> objectClass, String directoryName) {
        String filePath = getProperty(BLOGIX_FILEDB_PATH);
        if (filePath == null) {
            throw new IllegalArgumentException(BLOGIX_FILEDB_PATH + " property is not specified correctly");
        }
        return new FileDb<T2>(objectClass, new File(filePath + File.separator + directoryName));
    }

    private static void loadHomePage(Map<String, Object> model, int page) {
        EntryList<Post> allPosts = postsDb.findAll();
        model.put(ALL_POSTS_COUNT, allPosts.size());

        List<Post> homePosts = allPosts.sortDesc().page(page, DEFAULT_POSTS_PER_PAGE).asJavaList();
        model.put(HOME_POSTS, homePosts);
        model.put(CURRENT_PAGE, 1);
    }

    private static String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }
}
