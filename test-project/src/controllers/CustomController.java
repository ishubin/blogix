package controllers;

import models.*;
import net.mindengine.blogix.Blogix;

public class CustomController {
    
    public static CustomModel someString() {
        return new CustomModel("String from custom controllers");
    }
}