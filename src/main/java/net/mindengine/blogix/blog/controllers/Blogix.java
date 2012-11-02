package net.mindengine.blogix.blog.controllers;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.config.BlogixConfig;
import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.Section;
import net.mindengine.blogix.utils.BlogixFileUtils;

public class Blogix {
    private static final String TITLE = "title";
    private static final int DEFAULT_POSTS_PER_PAGE = 10;
    private static final String CURRENT_PAGE = "currentPage";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static FileDb<Post> postsDb = createPostsDb(); 
    private static FileDb<Section> sectionsDb = createSectionDb();
    
    private static String titleBase = loadBaseTitle();
    
    public static Map<String, Object> homeFirstPage() {
        Map<String, Object> model = loadCommonPostData();
        loadHomePage(model, 1);
        return model;
    }
    
    private static String title(String secondaryTitle) {
        if (secondaryTitle ==  null) {
            secondaryTitle = "";
        }
        StringBuffer buffer = new StringBuffer(secondaryTitle);
        if (titleBase != null) {
            buffer.append(" | ");
            buffer.append(titleBase);
        }
        return buffer.toString();
    }

    private static String homeTitle() {
        return titleFromConfig("blogix.title.home");
    }

    private static String loadBaseTitle() {
        return titleFromConfig("blogix.title.base");
    }

    private static String titleFromConfig(String propertyName) {
        String title = BlogixConfig.getConfig().getProperty(propertyName, null);
        if (title != null) {
            title = title.trim();
            if (title != null) {
                return title;
            }
        }
        return null;
    }

    public static  Map<String, Object> homePage(Integer page) {
        Map<String, Object> model = loadCommonPostData();
        loadHomePage(model, page);
        return model;
    }

    public static  Map<String, Object> post(String postId) {
        Map<String, Object> model = loadCommonPostData();
        Post post = postsDb.findById(postId);
        if (post != null) {
            model.put("post", post);
            model.put(TITLE, title(post.getTitle()));
            return model;
        }
        else throw new RuntimeException("Cannot find post: " + postId);
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
        Section section = sectionsDb.findById(sectionId);
        model.put("section", section);
        
        model.put(TITLE, title(section.getName()));
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

    private static <T extends Comparable<T>> FileDb<T> createDb(Class<T> objectClass, String directoryName) {
        String filePath = BlogixConfig.getConfig().getBlogixFileDbPath();
        if (filePath == null) {
            throw new IllegalArgumentException("Blogix database is not specified correctly");
        }
        return new FileDb<T>(objectClass, BlogixFileUtils.findFile(filePath + File.separator + directoryName));    
    }

    private static void loadHomePage(Map<String, Object> model, int page) {
        EntryList<Post> allPosts = postsDb.findAll();
        model.put(ALL_POSTS_COUNT, allPosts.size());

        List<Post> homePosts = allPosts.sortDesc().page(page, DEFAULT_POSTS_PER_PAGE).asJavaList();
        model.put("posts", homePosts);
        model.put(CURRENT_PAGE, 1);
        model.put(TITLE, title(homeTitle()));
    }

}
