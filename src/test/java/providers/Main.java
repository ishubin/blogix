package providers;

import java.util.HashMap;
import java.util.Map;

public class Main {
    
    @SuppressWarnings("rawtypes")
    public static Map[] provideArticles() {
        
        Map<String, String> article1 = new HashMap<String, String>();
        article1.put("id", "123");
        article1.put("date", "2012-01-12");
        
        Map<String, String> article2 = new HashMap<String, String>();
        article2.put("id", "1");
        article2.put("date", "2012-01-13");
        
        return new Map[]{article1, article2}; 
    }

}
