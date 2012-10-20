package net.mindengine.blogix.markup;

import java.util.Arrays;
import java.util.List;

public class TextileMarkup implements Markup {
    
    @Override
    public String apply(String unformatedText) {
        
        List<String> list = Arrays.asList(unformatedText.split("\\r?\\n"));
        return new TextileParser().parse(list.iterator());
    }
        
}
