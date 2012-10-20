package net.mindengine.blogix.markup.converters;

public class AsIsConverter implements Converter {

    @Override
    public String convert(String definition, String text) {
        return text;
    }

}
