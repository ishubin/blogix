package net.mindengine.blogix.db;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;

public class DbEntryParser {
    private static final char PARAMETER_DELIMITER = '-';
    private static final char BODY_DELIMITER = '=';
    private static final String UTF_8 = "UTF-8";
    private File file;

    public DbEntryParser(File file) {
        this.file = file;
    }
    
    Map<String, String> data = new HashMap<String, String>();

    public Map<String, String> load() throws IOException {
        LineIterator lineIterator = FileUtils.lineIterator(file, UTF_8);
        
        ParsingState state = new DummyState();
        while(state != null && lineIterator.hasNext()) {
            state = state.readLine(lineIterator);
            if (!lineIterator.hasNext() ){
                if (state != null) {
                    state.done();
                }
                break;
            }
        }
        return data;
    }

    private abstract class ParsingState {
        public abstract ParsingState readLine(LineIterator lines);
        public abstract void done();
    }
    
    private class DummyState extends ParsingState {
        @Override
        public ParsingState readLine(LineIterator lines) {
            String line = lines.nextLine();
            if (lineContainsOnly(line,PARAMETER_DELIMITER)) {
                return new ParseParam();
            }
            return null;
        }

        @Override
        public void done() {
        }
    }

    private class ParseParam extends ParsingState {
        private String name = null;
        private StringBuffer buffer = new StringBuffer();
        @Override
        public ParsingState readLine(LineIterator lines) {
            String line = lines.nextLine();
            if (lineContainsOnly(line,PARAMETER_DELIMITER)) {
                done();
                return new ParseParam();
            }
            else if (lineContainsOnly(line,BODY_DELIMITER)) {
                done();
                return new ParseBody();
            }
            else {
                if (name == null) {
                    name = line.trim();
                }
                else {
                    if (buffer.length() > 0) {
                        buffer.append('\n');
                    }
                    buffer.append(line.trim());
                }
            }
            return this;
        }
        @Override
        public void done() {
            if (name != null) {
                DbEntryParser.this.data.put(name, buffer.toString());
            }
        }
    }
    
    private class ParseBody extends ParsingState {

        private StringBuffer buffer = new StringBuffer();
        
        @Override
        public ParsingState readLine(LineIterator lines) {
            String line = lines.nextLine();
            if (lineContainsOnly(line,PARAMETER_DELIMITER)) {
                done();
                return new ParseParam();
            }
            else {
                if (buffer.length() > 0) {
                    buffer.append('\n');
                }
                buffer.append(line);
            }
            return this;
        }

        @Override
        public void done() {
            DbEntryParser.this.data.put("body", buffer.toString());
        }
        
    }
    
    private boolean lineContainsOnly(String line, char c) {
        if (line.length() > 3) {
            if (line.charAt(0) == c) {
                line = line.trim();
                for (int i=0; i<line.length(); i++) {
                    if (line.charAt(i) != c) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
