package net.mindengine.blogix.markup;

import java.io.StringWriter;

import net.java.textilej.parser.MarkupParser;
import net.java.textilej.parser.builder.HtmlDocumentBuilder;
import net.java.textilej.parser.markup.textile.TextileDialect;
import net.mindengine.blogix.markup.converters.Converter;

import org.apache.commons.lang3.text.translate.AggregateTranslator;
import org.apache.commons.lang3.text.translate.CharSequenceTranslator;
import org.apache.commons.lang3.text.translate.LookupTranslator;

public class TextileConverter implements Converter {

    private static final String AMP_CONVERTED = "$#$amp$#$";
    private static final String LESS_THAN_CONVERTER = "$#$lt$#$";
    private static final String GREATER_THAN_CONVERTER = "$#$gt$#$";

    @Override
    public String convert(String definition, String text) {
        
        
        MarkupParser parser = createParser();
        StringWriter sw = new StringWriter();
        HtmlDocumentBuilder builder = new HtmlDocumentBuilder(sw);
        builder.setEmitAsDocument(false);
        builder.setEmitDtd(false);
        parser.setBuilder(builder);
        
        parser.parse(encode(text));
        return decode(sw.toString());
    }
    
    public static final CharSequenceTranslator ESCAPE_XML = 
            new AggregateTranslator(
                new LookupTranslator(new String[][]{
                        {"&", AMP_CONVERTED},  
                        {"<", LESS_THAN_CONVERTER}, 
                        {">", GREATER_THAN_CONVERTER}, 
                    })
            );
    
    public static final CharSequenceTranslator UNESCAPE_XML = 
            new AggregateTranslator(
                new LookupTranslator(new String[][]{
                        {AMP_CONVERTED, "&amp;"},  
                        {LESS_THAN_CONVERTER, "&lt;"}, 
                        {GREATER_THAN_CONVERTER, "&gt;"}, 
                    })
            );
    
    private String encode(String text) {
        return ESCAPE_XML.translate(text);
    }
    
    private String decode(String text) {
        return UNESCAPE_XML.translate(text);
    }


    private MarkupParser createParser() {
        MarkupParser parser = new MarkupParser();
        parser.setDialect(new TextileDialect());
        return parser;
    }
}
