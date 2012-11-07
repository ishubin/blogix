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
package net.mindengine.blogix.blog.providers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.db.Entry;
import net.mindengine.blogix.db.EntryFilter;
import net.mindengine.blogix.db.EntryList;
import net.mindengine.blogix.db.FileDb;
import net.mindengine.blogix.model.Category;
import net.mindengine.blogix.model.Post;

public class BlogixProvider {
    
    private static final int DEFAULT_PAGES_PER_POST = 10;
    private static final String BLOGIX_FILEDB_PATH = "blogix.filedb.path";
    private static FileDb<Post> postsDb = createPostsDb(); 
    private static FileDb<Category> categoriesDb = createCategoriesDb();
    
    @SuppressWarnings({ "unchecked" })
    public static Map<String, Object>[] allHomePages() {
        int pages = postsDb.findAllEntries().pages(DEFAULT_PAGES_PER_POST);
        Map<String, Object>[] m = new Map[pages];
        
        for (int i=0; i<pages; i++) {
            m[i] = new HashMap<String, Object>();
            m[i].put("page", (i+1) );
        }
        return m;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object>[] allPosts() {
        List<String> postIds = postsDb.findAllIds().asJavaList();
        Map<String, Object>[] m = new Map[postIds.size()];
        
        int i = 0;
        for (String postId : postIds) {
            m[i] = new HashMap<String, Object>();
            m[i].put("postId", postId);
            i++;
        }
        return m;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object>[] allFilesForPosts() {
        List<String> postIds = postsDb.findAllIds().asJavaList();
        
        ArrayList<Map<String, Object>> map = new ArrayList<Map<String, Object>>();
        for (String postId : postIds) {
            List<String> files = postsDb.findAttachments(postId).asJavaList();
            for (String file : files) {
                Map<String, Object> m = new HashMap<String, Object>();
                m.put("postId", postId);
                m.put("fileName", file);
                map.add(m);
            }
        }
        return map.toArray(new Map[0]);
    }
    
    
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object>[] allCategories() {
        List<String> categories = categoriesDb.findAllIds().asJavaList();
        Map<String, Object> map[] = new Map[categories.size()];
        
        int i = 0;
        for (String category : categories) {
            map[i] = new HashMap<String, Object>();
            map[i].put("category", category);
            
            i++;
        }
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object>[] allCategoriesAndPages() {
        List<String> categories = categoriesDb.findAllIds().asJavaList();
        EntryList<Entry> allPosts = postsDb.findAllEntries();
        
        ArrayList<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
        for (String category : categories) {
            int pages = allPosts.filter(byCategoriesContaining(category)).pages(DEFAULT_PAGES_PER_POST);

            for (int page = 1; page <= pages; page++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("category", category);
                map.put("page", page);
                maps.add(map);
            }
        }
        return maps.toArray(new Map[0]);
    }
    
    private static EntryFilter<Entry> byCategoriesContaining(final String category) {
        return new EntryFilter<Entry>() {
            @Override
            public boolean applies(Entry entry) {
                String categories = entry.field("categories");
                return (categories != null && categories.contains(category));
            }
        };
    }

    private static FileDb<Post> createPostsDb() {
        return createDb(Post.class, "posts");
    }

    private static FileDb<Category> createCategoriesDb() {
        return createDb(Category.class, "categories");
    }

    private static <T extends Comparable<T>> FileDb<T> createDb(Class<T> objectClass, String directoryName) {
        String filePath = getProperty(BLOGIX_FILEDB_PATH);
        if (filePath == null) {
            throw new IllegalArgumentException(BLOGIX_FILEDB_PATH + " property is not specified correctly");
        }
        return new FileDb<T>(objectClass, new File(filePath + File.separator + directoryName));
    }
    
    private static String getProperty(String propertyName) {
        return System.getProperty(propertyName);
    }
}
