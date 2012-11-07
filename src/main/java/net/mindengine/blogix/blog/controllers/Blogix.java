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
import net.mindengine.blogix.model.Category;
import net.mindengine.blogix.utils.BlogixFileUtils;

public class Blogix {
    private static final String TITLE = "title";
    private static final int DEFAULT_POSTS_PER_PAGE = 10;
    private static final String CURRENT_PAGE = "currentPage";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static FileDb<Post> postsDb = createPostsDb(); 
    private static FileDb<Category> categoriesDb = createCategoriesDb();
    
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

    public static  Map<String, Object> postsByCategory(String categoryId) {
        return postsByCategoryAndPage(categoryId, 1);
    }

    public static  Map<String, Object> postsByCategoryAndPage(String categoryId, Integer page) {
        Map<String, Object> model = loadCommonPostData();
        EntryList<Post> allPosts = postsDb.findByFieldContaining("categories", categoryId);
        model.put("allPostsCount", allPosts.size());
        model.put("posts", allPosts.page(page, DEFAULT_POSTS_PER_PAGE).asJavaList());
        model.put("currentPage", page);
        Category category = categoriesDb.findById(categoryId);
        model.put("category", category);
        
        model.put(TITLE, title(category.getName()));
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
    
    public static Map<String, Object> rssFeedForCategory(String categoryId) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("posts", postsDb.findByFieldContaining("categories", categoryId).asJavaList());
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

    private static FileDb<Category> createCategoriesDb() {
        return createDb(Category.class, "categories");
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

    public static Map<String, Object> categories() {
        return null;
    }

}
