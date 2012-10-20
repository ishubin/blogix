package net.mindengine.blogix.markup;

public class DummyMarkup implements Markup {

    @Override
    public String apply(String unformatedText) {
        return unformatedText;
    }

}
