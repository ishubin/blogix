package controllers;

import models.Article;

public class Main {
    
    public static String home() {
        return "home value from controller";
    }
    
    public static Article showArticle(String date, String id) {
        
        if ( id == null || id.isEmpty() ) {
            throw new IllegalArgumentException("Can't find article with id: " + id);
        }
        return new Article(id, date, "Title for " + id, "Article description for " + id);
    }
}
