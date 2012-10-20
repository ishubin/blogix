package net.mindengine.blogix.markup.converters;

import org.apache.commons.lang3.StringEscapeUtils;

public class CodeBlockConverter implements Converter {

    @Override
    public String convert(String definition, String text) {
        StringBuffer buffer = new StringBuffer("<code class=\"block\"");
        buffer.append(dataLanguageFromDefinition(definition));
        buffer.append(">");
        buffer.append(StringEscapeUtils.escapeHtml4(text));
        buffer.append("</code>");
        return buffer.toString();
    }

    private String dataLanguageFromDefinition(String definition) {
        if (definition != null && definition.length() > 2) {
            String language = definition.substring(2).trim();
            if (!language.isEmpty()) {
                return String.format(" data-language=\"%s\"", StringEscapeUtils.escapeHtml4(language));
            }
        }
        return "";
    }
}
