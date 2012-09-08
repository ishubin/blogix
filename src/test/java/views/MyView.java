package views;

import java.io.File;
import java.net.URISyntaxException;

public class MyView {
    
    
    public static String customStringView() {
        return "this is a string content";
    }
    
    public static File customFileView(String argFromController) {
        try {
            return new File(MyView.class.getResource("/custom-file-for-response.txt").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Couldn't find a file");
        }
    }
    
    
    public static File customImage() {
        try {
            return new File(MyView.class.getResource("/sample-files/customImage.jpg").toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException("Couldn't find a file");
        }
    }

}
