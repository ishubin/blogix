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
package net.mindengine.blogix.markup;

import static java.util.regex.Pattern.compile;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import freemarker.template.Template;

import net.mindengine.blogix.markup.converters.AsIsConverter;
import net.mindengine.blogix.markup.converters.CodeBlockConverter;
import net.mindengine.blogix.markup.converters.Converter;
import net.mindengine.blogix.markup.converters.PluginsConverter;

public class TextileParser {
    protected static final Converter DEFAULT_CONVERTER = new TextileConverter();
    private static final String NO_BREAKER = null; 
    private static final String NO_DEFINITION = null;
    private static final String NEW_LINE = System.getProperty("line.separator");
    
    private TextileParser nextParser;
    
    
    private static class Rule {
        private Pattern pattern;
        private String breaker;
        private Converter converter;
        public Rule(Pattern pattern, String breaker, Converter converter) {
            this.pattern = pattern;
            this.breaker = breaker;
            this.converter = converter;
        }
        public Pattern getPattern() {
            return pattern;
        }
        public String getBreaker() {
            return breaker;
        }
        public Converter getConverter() {
            return converter;
        }
    }
    
    private class ConverterChunk {
        
        public ConverterChunk(Converter converter, String definition, String breaker) {
            this.converter = converter;
            this.breaker = breaker;
            this.definition = definition;
        }
        private String definition;
        private Converter converter;
        private StringBuffer buffer = new StringBuffer();
        private String breaker;
        
        public String getBreaker() {
            return this.breaker;
        }
        public String convert() {
            return converter.convert(definition, buffer.toString());
        }
        public void appendLine(String line) {
            buffer.append(line);
            buffer.append(NEW_LINE);
        }
    }

    private List<ConverterChunk> chunks;
    private Map<String, Template> plugins; 
    @SuppressWarnings("serial")
    private List<Rule> _rules; 
    
    public TextileParser(Map<String, Template> plugins) {
        this.plugins = plugins;
        
        _rules = new LinkedList<Rule>() {{
            add(new Rule(compile("\\@\\@"), "@@", new AsIsConverter()));
            add(new Rule(compile("(\\$\\$ .*|\\$\\$)"), "$$", new CodeBlockConverter()));
            add(new Rule(compile("\\{\\@ .*"), "}", new PluginsConverter(TextileParser.this.plugins)));
        }};
    }

    public String parse(Iterator<String> iterator) {
        chunks = new LinkedList<ConverterChunk>();
        ConverterChunk currentChunk = newChunk(defaultChunk());
        
        while (iterator.hasNext()) {
            String line = iterator.next();
            
            if (newLineBreaksCurrentChunk(line, currentChunk)) {
                currentChunk = newChunk(defaultChunk());
            }
            else {
                Rule rule = checkIfLineAppliesToRule(line);
                if (rule != null) {
                    currentChunk = newChunk(chunkForRuleWithDefinition(rule, line));
                }
                else {
                    currentChunk.appendLine(line);
                }
            }
        }
        
        return getText();
    }

    private ConverterChunk chunkForRuleWithDefinition(Rule rule, String definition) {
        return new ConverterChunk(rule.getConverter(), definition, rule.getBreaker());
    }

    private Rule checkIfLineAppliesToRule(String line) {
        for (Rule rule : _rules) {
            if (rule.getPattern().matcher(line).matches()) {
                return rule;
            }
        }
        return null;
    }

    private String getText() {
        StringBuffer buffer = new StringBuffer();
        
        for (ConverterChunk chunk : chunks) {
            buffer.append(chunk.convert());
        }
        return buffer.toString();
    }


    private boolean newLineBreaksCurrentChunk(String line, ConverterChunk currentChunk) {
        return line.equals(currentChunk.getBreaker());
    }

    private ConverterChunk newChunk(ConverterChunk chunk) {
        chunks.add(chunk);
        return chunk;
    }

    private ConverterChunk defaultChunk() {
        return new ConverterChunk(DEFAULT_CONVERTER, NO_DEFINITION, NO_BREAKER);
    }

    public TextileParser getNextParser() {
        return nextParser;
    }

    public void setNextParser(TextileParser nextParser) {
        this.nextParser = nextParser;
    }
}
