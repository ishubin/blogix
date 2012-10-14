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
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.Section;

public class BlogixProvider {
    
    private static final int DEFAULT_PAGES_PER_POST = 10;
    private static final String BLOGIX_FILEDB_PATH = "blogix.filedb.path";
    private static FileDb<Post> postsDb = createPostsDb(); 
    private static FileDb<Section> sectionsDb = createSectionDb();
    
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
    public static Map<String, Object>[] allSections() {
        List<String> sections = sectionsDb.findAllIds().asJavaList();
        Map<String, Object> map[] = new Map[sections.size()];
        
        int i = 0;
        for (String section : sections) {
            map[i] = new HashMap<String, Object>();
            map[i].put("section", section);
            
            i++;
        }
        return map;
    }
    
    @SuppressWarnings("unchecked")
    public static Map<String, Object>[] allSectionsAndPages() {
        List<String> sections = sectionsDb.findAllIds().asJavaList();
        EntryList<Entry> allPosts = postsDb.findAllEntries();
        
        ArrayList<Map<String, Object>> maps = new ArrayList<Map<String,Object>>();
        for (String section : sections) {
            int pages = allPosts.filter(bySectionsContaining(section)).pages(DEFAULT_PAGES_PER_POST);

            for (int page = 1; page <= pages; page++) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("section", section);
                map.put("page", page);
                maps.add(map);
            }
        }
        return maps.toArray(new Map[0]);
    }
    
    private static EntryFilter<Entry> bySectionsContaining(final String section) {
        return new EntryFilter<Entry>() {
            @Override
            public boolean applies(Entry entry) {
                String sections = entry.field("sections");
                return (sections != null && sections.contains(section));
            }
        };
    }

    private static FileDb<Post> createPostsDb() {
        return createDb(Post.class, "posts");
    }

    private static FileDb<Section> createSectionDb() {
        return createDb(Section.class, "sections");
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
