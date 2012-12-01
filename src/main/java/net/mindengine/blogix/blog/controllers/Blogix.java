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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.config.BlogixConfig;
import net.mindengine.blogix.db.EntryFilter;
import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;
import net.mindengine.blogix.model.CategoryAggregation;
import net.mindengine.blogix.model.MonthArchive;
import net.mindengine.blogix.model.Pagination;
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.Category;
import net.mindengine.blogix.model.YearArchive;
import net.mindengine.blogix.utils.BlogixFileUtils;

public class Blogix {
    private static final String ALL_CATEGORIES = "allCategories";
    private static final String TITLE = "title";
    private static final int DEFAULT_POSTS_PER_PAGE = 10;
    private static final String CURRENT_PAGE = "currentPage";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static FileDb<Post> postsDb = createPostsDb();
    private static FileDb<Post> docsDb = createDocsDb();
    private static FileDb<Category> categoriesDb = createCategoriesDb();
    
    private static String titleBase = loadBaseTitle();
    
    public static Map<String, Object> base() {
        Map<String, Object> model = loadCommonPostData();
        return model;
    }
    
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
        Post post = postsDb.findById(postId);
        if (post != null) {
            model.put("post", post);
            putTitle(model, post.getTitle());
            
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
        int totalPosts = allPosts.size();
        model.put("allPostsCount", totalPosts);
        model.put("posts", allPosts.sortDesc().page(page, DEFAULT_POSTS_PER_PAGE).asJavaList());
        model.put("currentPage", page);
        Category category = categoriesDb.findById(categoryId);
        model.put("category", category);
        
        int totalPages = (int)(totalPosts / DEFAULT_POSTS_PER_PAGE) + 1;
        model.put("pages", totalPages);
        model.put("pagination", Pagination.create(1, totalPages, 2, page));
        
        model.put(TITLE, generateTitle(category.getName()));
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
        model.put("posts", postsDb.findAll().sortDesc().first(20).asJavaList());
        return model;
    }
    
    public static Map<String, Object> rssFeedForCategory(String categoryId) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("posts", postsDb.findByFieldContaining("categories", categoryId).sortDesc().first(20).asJavaList());
        return model;
    }
    
    public static Map<String, Object> categories() {
        Map<String, Object> model = loadCommonPostData();
        putTitle(model, "Categories");
        
        List<Category> plainCategories = categoriesDb.findAll().asJavaList();
        EntryList<Post> allPosts = postsDb.findAll();
        List<CategoryAggregation> categories = new LinkedList<CategoryAggregation>();
        
        for (Category category : plainCategories) {
            CategoryAggregation ca = new CategoryAggregation();
            ca.setCategory(category);
            ca.setRecentPosts(allPosts.sortDesc().filter(onlyRecentPostsForCategory(category.getId())).first(5).asJavaList());
            categories.add(ca);
        }
        model.put("categories", categories);
        return model;
    }

    
    private static EntryFilter<Post> onlyRecentPostsForCategory(final String categoryId) {
        return new EntryFilter<Post>() {
            @Override
            public boolean applies(Post post) {
                String[] categories = post.getCategories();
                if (categories != null) {
                    for (String category : categories) {
                        if (category.equals(categoryId)) {
                            return true;
                        }
                    }
                }
                return false;
            }
        };
    }

    public static Map<String, Object> recentPosts() {
        return loadCommonPostData();
    }
    
    public static Map<String, Object> document(String documentId) {
        Map<String, Object> model = loadCommonPostData();
        Post post = docsDb.findById(documentId);
        if (post != null) {
            model.put("doc", post);
            putTitle(model, post.getTitle());
            return model;
        }
        else throw new RuntimeException("Cannot find document: " + documentId);
    }
    
    public static Map<String, Object> archive() {
        Map<String, Object> model = loadCommonPostData();
        putTitle(model, "Archive");
        
        List<Post> allPosts = postsDb.findAll().sortDesc().asJavaList();
        
        List<YearArchive> archive = new LinkedList<YearArchive>();
        
        if (allPosts.size() > 0) {
            Collections.sort(allPosts, byDateDescending());
            YearArchive currentYear = new YearArchive(yearOfPost(allPosts.get(0)));
            MonthArchive currentMonth = new MonthArchive(monthOfPost(allPosts.get(0)));
            currentYear.addMonth(currentMonth);
            archive.add(currentYear);
            for (Post post : allPosts) {
                int postYear = yearOfPost(post);
                int postMonth = monthOfPost(post);
                if (currentYear.getYear() != postYear) {
                    currentYear = new YearArchive(postYear);
                    currentMonth = new MonthArchive(postMonth);
                    currentYear.addMonth(currentMonth);
                    archive.add(currentYear);
                }
                else {
                    if (currentMonth.getMonth() != postMonth) {
                        currentMonth = new MonthArchive(postMonth);
                        currentYear.addMonth(currentMonth);
                    }
                }
                currentMonth.addPost(post);
            }
        }
        
        model.put("archive", archive);
        return model;
    }

    private static int monthOfPost(Post post) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(post.getDate());
        return calendar.get(Calendar.MONTH);
    }

    private static int yearOfPost(Post post) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(post.getDate());
        return calendar.get(Calendar.YEAR);
    }

    private static Comparator<Post> byDateDescending() {
        return new Comparator<Post>() {
            @Override
            public int compare(Post p1, Post p2) {
                return (int)(p2.getDate().getTime() / 1000 - p1.getDate().getTime() / 1000);
            }
        };
    }


    private static Map<String, Object> loadCommonPostData() {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("recentPosts", loadRecentPosts());
        model.put(ALL_CATEGORIES, getAllCategories());
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
    
    private static FileDb<Post> createDocsDb() {
        return createDb(Post.class, "docs");
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
        
        int totalPosts = allPosts.size();
        model.put(ALL_POSTS_COUNT, totalPosts);

        List<Post> homePosts = allPosts.sortDesc().page(page, DEFAULT_POSTS_PER_PAGE).asJavaList();
        model.put("posts", homePosts);
        model.put(CURRENT_PAGE, page);
        int totalPages = (int)(totalPosts / DEFAULT_POSTS_PER_PAGE) + 1;
        model.put("pages", totalPages);
        model.put("pagination", Pagination.create(1, totalPages, 2, page));
        
        model.put(TITLE, generateTitle(homeTitle()));
    }

    private static Map<String, Category> getAllCategories() {
        Map<String, Category> map = new HashMap<String, Category>();
        List<Category> categoryList = categoriesDb.findAll().asJavaList();
        
        for (Category category : categoryList) {
            map.put(category.getId(), category);
        }
        return map;
    }

    private static void putTitle(Map<String, Object> model, String secondaryTitle) {
        model.put(TITLE, generateTitle(secondaryTitle));
    }
    
    private static String generateTitle(String secondaryTitle) {
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

        
    
}
