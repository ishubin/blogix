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
package net.mindengine.blogix.tests.blog;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.mindengine.blogix.blog.controllers.Blogix;
import net.mindengine.blogix.model.Category;
import net.mindengine.blogix.model.CategoryAggregation;
import net.mindengine.blogix.model.MonthArchive;
import net.mindengine.blogix.model.Post;
import net.mindengine.blogix.model.YearArchive;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.internal.matchers.TypeSafeMatcher;
import org.testng.annotations.Test;

public class BlogixControllerTest {
    
    private static final String PAGINATION = "pagination";
    private static final String PAGES = "pages";
    private static final String TITLE_BASE = " | Blogix Blog";
    private static final String TITLE = "title";
    private static final int NUMBER_OF_ALL_POSTS_FOR_CATEGORY_1 = 12;
    private static final int NUMBER_OF_ALL_POSTS_FOR_CATEGORY_2 = 2;
    private static final int NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST = 4;
    private static final int DEFAULT_NUMBER_OF_RECENT_POSTS = 5;
    private static final int NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST = 10;
    private static final int NUMBER_OF_ALL_POSTS_IN_TEST = 14;
    private static final String RECENT_POSTS = "recentPosts";
    private static final String ALL_POSTS_COUNT = "allPostsCount";
    private static final String POSTS = "posts";
    private static final String CURRENT_PAGE = "currentPage";
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void homePage_shouldGive_onlyPosts_forFirstPage() throws Exception {
        Map<String, Object> homePageModel = Blogix.homeFirstPage();
        
        assertCommonModelDataForPosts(homePageModel);
        
        assertThat(homePageModel, hasKey(POSTS));
        assertThat(homePageModel, hasKey(CURRENT_PAGE));
        assertThat(homePageModel, hasKey(ALL_POSTS_COUNT));
        assertThat(homePageModel, hasKey(PAGINATION));
        
        assertHomeFirstPagePosts((List<Post>) homePageModel.get(POSTS));
        
        assertThat((Integer) homePageModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        assertThat((Integer) homePageModel.get(CURRENT_PAGE), is(1));
        assertThat((Integer) homePageModel.get(PAGES), is(2));
        
        assertThat((String) homePageModel.get(TITLE), is("Home" + TITLE_BASE));
    }

    @SuppressWarnings("unchecked")
    @Test
    public void homePageByNumber_shouldGive_onlyPosts_forThatNumber() throws Exception {
        Map<String, Object> homePageModel = Blogix.homePage(2);
        
        assertCommonModelDataForPosts(homePageModel);
        
        assertThat(homePageModel, hasKey(POSTS));
        assertThat(homePageModel, hasKey(CURRENT_PAGE));
        assertThat(homePageModel, hasKey(ALL_POSTS_COUNT));
        assertThat(homePageModel, hasKey(PAGES));
        assertThat(homePageModel, hasKey(PAGINATION));
        
        List<Post> homePosts = (List<Post>) homePageModel.get(POSTS);
        assertHomeSecondPagePosts(homePosts);
        
        assertThat("'" + ALL_POSTS_COUNT + "' field in homePageModel should be", (Integer) homePageModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        assertThat("'" + CURRENT_PAGE + "' field in homePageModel should be", (Integer) homePageModel.get(CURRENT_PAGE), is(2));
        
        assertThat((Integer) homePageModel.get(PAGES), is(2));
        
        assertThat((String) homePageModel.get(TITLE), is("Home" + TITLE_BASE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void categoriesPage_shouldGive_allCategories_with5RecentPosts_perEachCategory() {
        Map<String, Object> categoriesModel = Blogix.categories();
        assertCommonModelDataForPosts(categoriesModel);
        
        assertThat(categoriesModel, hasKey("categories"));
        
        List<CategoryAggregation> categories = (List<CategoryAggregation>) categoriesModel.get("categories");
        assertThat("Categories list should not be null", categories, is(notNullValue()));
        assertThat("Amount of categories in list should be", categories.size(), is(3));
        assertThat("First category name should be", categories.get(0).getCategory().getName(), is("Category 1"));
        assertThat("First category should contain 5 recent posts", categories.get(0).getRecentPosts(), hasRecentPostsWithIds("2012-01-01-title-12", 
                "2012-01-01-title-11",
                "2012-01-01-title-10",
                "2012-01-01-title-09",
                "2012-01-01-title-08"));
        
        assertThat("Second category name should be", categories.get(1).getCategory().getName(), is("Category 2"));
        assertThat("Third category name should be", categories.get(2).getCategory().getName(), is("Category 3"));
        
        assertThat("Third category should contain only 2 recent posts", categories.get(2).getRecentPosts(), hasRecentPostsWithIds("2012-01-01-title-14", "2012-01-01-title-13"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void recentPosts_shouldGive_only5RecentPosts() {
        Map<String, Object> recentPostsModel = Blogix.recentPosts();
        
        assertThat(recentPostsModel, hasKey(RECENT_POSTS));
        List<Post> recentPosts = (List<Post>) recentPostsModel.get(RECENT_POSTS);
        assertThat("Recent posts should not be null", recentPosts, is(notNullValue()));
        
        assertThat("Recent posts should contain 5 recent posts for any category", recentPosts, hasRecentPostsWithIds("2012-01-01-title-14",
                "2012-01-01-title-13",
                "2012-01-01-title-12",
                "2012-01-01-title-11",
                "2012-01-01-title-10"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void archive_shouldGive_allPosts_dividedBy_Year_andThenBy_Month() {
        Map<String, Object> archiveModel = Blogix.archive();
        assertCommonModelDataForPosts(archiveModel);
        assertThat(archiveModel, hasKey("archive"));
        
        List<YearArchive> archive = (List<YearArchive>) archiveModel.get("archive");
        assertThat("Archive should not be null", archive, is(notNullValue()));
        assertThat("Archive should have amount of years", archive.size(), is(2));
        
        assert2013Archive(archive);
        assert2012Archive(archive);
    }
    
    private void assert2012Archive(List<YearArchive> archive) {
        YearArchive archive2012 = archive.get(1);
        assertThat("Second year in the archive should be", archive2012.getYear(), is(2012));
        assertThat("Second year in archive should have amount of months", archive2012.getMonths().size(), is(2));
        
        MonthArchive firstMonth = archive2012.getMonths().get(0);
        assertThat("First month in 2012 archive should be", firstMonth.getMonth(), is(1));
        assertThat("First month in 2012 archive should have posts", firstMonth.getPosts(), hasRecentPostsWithIds("2012-01-01-title-13", "2012-01-01-title-12"));
        
        MonthArchive secondMonth = archive2012.getMonths().get(1);
        assertThat("Second month in 2012 archive should be", secondMonth.getMonth(), is(0));
        assertThat("Second month in 2012 archive should have posts", secondMonth.getPosts(), hasRecentPostsWithIds("2012-01-01-title-11",
                "2012-01-01-title-10",
                "2012-01-01-title-09",
                "2012-01-01-title-08",
                "2012-01-01-title-07",
                "2012-01-01-title-06",
                "2012-01-01-title-05",
                "2012-01-01-title-04",
                "2012-01-01-title-03",
                "2012-01-01-title-02",
                "2012-01-01-title-01"));
    }

    private void assert2013Archive(List<YearArchive> archive) {
        YearArchive archive2013 = archive.get(0);
        assertThat("First year in the archive should be", archive2013.getYear(), is(2013));
        assertThat("First year in the archive should have amount of months", archive2013.getMonths().size(), is(1));
        assertThat("First month in first year should be", archive2013.getMonths().get(0).getMonth(), is(2));
        assertThat("Posts should be ", archive2013.getMonths().get(0).getPosts(), hasRecentPostsWithIds("2012-01-01-title-14"));
    }

    @Test
    public void readsSingleBlogPostById() throws Exception {
        String postId = "2012-01-01-title-01";
        Map<String, Object> postModel = Blogix.post(postId);
        assertThat(postModel, hasKey("post"));
        assertCommonModelDataForPosts(postModel);
        
        Post post = (Post) postModel.get("post");
        
        assertThat("Post should be not null", post, is(notNullValue()));
        assertThat(post.getId(), is(postId));
        assertThat("title for post '" + postId + "' should be", post.getTitle(), is("Title 1"));
        assertThat("content for post '" + postId + "' should be", post.getContent(), is("Content 1\nPart 1\nContent 1 part 2"));
        assertThat("content preview part for '" + postId + "' should be", post.getContentPreview(), is("Content 1\nPart 1"));
        assertThat("commentsAllowed for post '" + postId + "' should be", post.getAllowComments(), is(true));
        assertThat("date for post '" + postId + "' should be", post.getDate(), is(new Date(1325372400000L)));
        assertThat("categories for post '" + postId + "' should be", post.getCategories(), is(new String[]{"category1", "category2"}));
        assertThat("externalUrl for post '" + postId + "' should be", post.getExternalUrl(), is("www.google.com"));
        assertThat("externalUrl for post '" + postId + "' should be", post.getDisplayExternalUrl(), is("Google It!"));
        
        //Verifying that post can access additional custom fields
        assertThat("customField is incorrect for post '" + postId + "'", post.field("customField"), is("customValue"));
        
        assertThat((String) postModel.get(TITLE), is("Title 1" + TITLE_BASE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void searchesForPostsByCategory() throws Exception {
        Map<String, Object> postsModel = Blogix.postsByCategory("category1");
        assertThat(postsModel, hasKey("posts"));
        assertThat(postsModel, hasKey("category"));
        assertThat(postsModel, hasKey(CURRENT_PAGE));
        assertThat(postsModel, hasKey(ALL_POSTS_COUNT));
        
        assertCommonModelDataForPosts(postsModel);
        
        Category category = (Category) postsModel.get("category");
        assertThat(category.getId(), is("category1"));
        assertThat(category.getName(), is("Category 1"));
        
        List<Post> posts = (List<Post>) postsModel.get("posts");
        assertThat(posts.size(), is(NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST));
        
        for (int i = 1; i <= NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST; i++) {
            String strNumber = Integer.toString(i);
            if (i<10) {
                strNumber = "0" + strNumber;
            }
            assertThat(posts.get(i - 1).getId(), is("2012-01-01-title-" + strNumber));
        }
        
        assertThat((Integer)postsModel.get(ALL_POSTS_COUNT), is(12));
        assertThat((Integer)postsModel.get(CURRENT_PAGE), is(1));
        
        assertThat((String) postsModel.get(TITLE), is("Category 1" + TITLE_BASE));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void searchesForPostsByCategoryAndPage() throws Exception {
        Map<String, Object> postsModel = Blogix.postsByCategoryAndPage("category1", 2);
        
        assertThat(postsModel, hasKey("posts"));
        assertThat(postsModel, hasKey("category"));
        assertThat(postsModel, hasKey(CURRENT_PAGE));
        assertThat(postsModel, hasKey(ALL_POSTS_COUNT));
        
        assertCommonModelDataForPosts(postsModel);
        
        Category category = (Category) postsModel.get("category");
        assertThat(category.getId(), is("category1"));
        assertThat(category.getName(), is("Category 1"));
        
        List<Post> posts = (List<Post>) postsModel.get("posts");
        assertThat(posts.size(), is(2));
        assertThat(posts.get(0).getId(), is("2012-01-01-title-11"));
        assertThat(posts.get(1).getId(), is("2012-01-01-title-12"));
        assertThat((Integer)postsModel.get(ALL_POSTS_COUNT), is(NUMBER_OF_ALL_POSTS_FOR_CATEGORY_1));
        assertThat((Integer)postsModel.get(CURRENT_PAGE), is(2));
        
        assertThat((String) postsModel.get(TITLE), is("Category 1" + TITLE_BASE));
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForAllPosts() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedAll();
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_IN_TEST));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-01"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-02"));
    }
    
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForPostsByCategory1() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedForCategory("category1");
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_FOR_CATEGORY_1));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-01"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-02"));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void rssFeedForPostsByCategory3() throws Exception {
        Map<String, Object> rssModel = Blogix.rssFeedForCategory("category3");
        assertThat(rssModel, hasKey("posts"));
        List<Post> posts = (List<Post>) rssModel.get("posts");
        assertThat("RSS feed for all should have all posts", posts.size(), is(NUMBER_OF_ALL_POSTS_FOR_CATEGORY_2));
        
        assertThat("First post should be", posts.get(0).getId(), is("2012-01-01-title-13"));
        assertThat("Second post should be", posts.get(1).getId(), is("2012-01-01-title-14"));
    }
    
    
    @Test
    public void readAttachmentForPost() throws Exception {
        File file = Blogix.fileForPost("2012-01-01-title-02", "file01.txt");
        assertThat("Attachment should not be null", file, is(notNullValue()));
        assertThat("Attachment should be named", file.getName(), is("2012-01-01-title-02.file01.txt"));
        assertThat("Attachment should exist", file.exists(), is(true));
    }
    
    private void assertRecentPosts(List<Post> recentPosts) {
        assertThat("List of recentPosts should be", recentPosts, is(notNullValue()));
        assertThat("Size of recentPosts should be", recentPosts.size(), is(DEFAULT_NUMBER_OF_RECENT_POSTS));
    }

    private void assertHomeFirstPagePosts(List<Post> homePosts) {
        assertThat("List of homePosts should be", homePosts, is(notNullValue()));
        assertThat("Size of homePosts should be", homePosts.size(), is(NUMBER_OF_FIRST_PAGE_POSTS_IN_TEST));
        
        Iterator<Post> it = homePosts.iterator();
        int index = NUMBER_OF_ALL_POSTS_IN_TEST;
        while(it.hasNext()) {
            Post post = it.next();
            assertThat(post.getTitle(), is("Title " + (index)));
            index--;
        }
    }

    private void assertHomeSecondPagePosts(List<Post> homePosts) {
        assertThat("List of homePosts should be not null", homePosts, is(notNullValue()));
        assertThat("Size of homePosts should be", homePosts.size(), is(NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST));
        
        Iterator<Post> it = homePosts.iterator();
        int index = NUMBER_OF_SECOND_PAGE_POSTS_IN_TEST;
        while(it.hasNext()) {
            Post post = it.next();
            assertThat(post.getTitle(), is("Title " + (index)));
            index--;
        }
    }
    
    private Matcher<? super List<Post>> hasRecentPostsWithIds(final String ...postIds) {
        return new TypeSafeMatcher<List<Post>>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("has posts with ids: " + postIds);
            }

            @Override
            public boolean matchesSafely(List<Post> list) {
                if (list.size() != postIds.length) {
                    return false;
                }
                
                int i = 0;
                for (Post post : list) {
                    if (!post.getId().equals(postIds[i])) {
                        return false;
                    }
                    i++;
                }
                return true;
            }
        };
    }
    
    @SuppressWarnings("unchecked")
    private void assertCommonModelDataForPosts(Map<String, Object> model) {
        assertThat(model, hasKey(RECENT_POSTS));
        assertThat(model, hasKey(TITLE));
        List<Post> recentPosts = (List<Post>) model.get(RECENT_POSTS);
        assertRecentPosts(recentPosts);
    }
        
}
