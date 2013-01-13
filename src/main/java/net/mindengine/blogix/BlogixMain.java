/*******************************************************************************
* Copyright 2013 Ivan Shubin http://mindengine.net
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
package net.mindengine.blogix;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import net.mindengine.blogix.compiler.BlogixCompiler;
import net.mindengine.blogix.export.Exporter.BlogixExporter;
import net.mindengine.blogix.web.BlogixServer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;



public class BlogixMain {
    
    private static final Object NO_INSTANCE = null;
    private static final String _BLOGIX_SUFFIX = ".blogix";
    
    private static Method findCommandMethod(String name) {
        Method[] methods = BlogixMain.class.getDeclaredMethods();
        for (Method method : methods) {
            if (Modifier.isStatic(method.getModifiers()) && method.getName().equals("cmd_" + name)) {
                return method;
            }
        }
        return null;
    }


    public static void main(String[] args) throws Exception {
            processConsoleCommands(args);
    }
    
    private static void processConsoleCommands(String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        if (args.length > 0) {
            invokeCommand(args[0], shift(args));
        }
        else {
            printGlobalHelp();
        }
    }


    @SuppressWarnings("unused")
    private static void cmd_help(String[] args) {
        if (args.length > 0) {
            printCommandHelp(args[0]);
        }
        else {
            printGlobalHelp();
        }
    }

    private static void printCommandHelp(String command) {
        try {
            info(IOUtils.toString(BlogixMain.class.getResourceAsStream("/help-command-" + command + ".txt")));
        }
        catch (Exception e) {
            info("There is no command \"" + command + "\"");
            System.exit(1);
        }
    }

    private static void printGlobalHelp() {
        try {
            info(IOUtils.toString(BlogixMain.class.getResourceAsStream("/help.txt")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    private static void cmd_export(String[] args) throws IOException, URISyntaxException {
        String dest = "export";
        if (args.length > 0) {
            dest = args[0];
        }
        
        File destinationDir = new File(dest);
        if (!destinationDir.exists()) {
            destinationDir.mkdirs();
        }
        BlogixExporter exporter = new BlogixExporter(destinationDir);
        info("Exporting all routes to \"" + dest + "\"");
        exporter.exportAll();
    }

    @SuppressWarnings("unused")
    private static void cmd_compile() {
        BlogixCompiler compiler = new BlogixCompiler();
        File sourceDir = new File("src");
        if (!sourceDir.exists()) {
            throw new RuntimeException("There is no src folder");
        }
        compiler.setSourceDir(sourceDir);

        
        File binDir = new File("bin");
        if (!binDir.exists()) {
            binDir.mkdirs();
        }
        compiler.setClassesDir(binDir);
        
        info("Compiling project...");
        compiler.compile();
    }


    @SuppressWarnings("unused")
    private static void cmd_run() throws Exception {
        BlogixServer server  = new BlogixServer();
        info("Launching blogix web server...");
        server.startServer();
    }
    
    @SuppressWarnings("unused")
    private static void cmd_show(String entryType) {
        File fileDir = new File("db" + File.separator + entryType);
        if (fileDir.exists() && fileDir.isDirectory()) {
            File[] files = fileDir.listFiles();
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".blogix")) {
                    System.out.println(trimBlogixSuffixFromEntryFileName(file.getName()));
                }
            }
        }
        else {
            error("There is no such entry type \"" + entryType + "\" in db");
        }
    }
    
    private static void cmd_init(String websiteType) throws IOException {
        String appPath = getAppPath();
        File templatesDir = new File(appPath + File.separator + "templates");
        if (!templatesDir.exists()) {
            error("Incorrect path to application: " + appPath);
        }
        
        for (File templateDir : templatesDir.listFiles()) {
            if (templateDir.isDirectory() && templateDir.getName().equals(websiteType)) {
                initWebsiteFromTemplate(templateDir);
                info("Done! Initialized " + websiteType);
                return;
            }
        }
        error("There is no such template: " + websiteType);
    }
    
    @SuppressWarnings("unused")
    private static void cmd_new(String[] args) throws Exception {
        if (args.length > 1) {
            String entryType = args[0];
            String title = args[1];
            if (entryType.equals("post")) {
                createPost(title);
            }
            else if (entryType.equals("doc")) {
                createDoc(title);
            }
            else if (entryType.equals("category")) {
                createCategory(title);
            }
            else error("There is no such entry: " + entryType);
        }
        else {
            error("Wrong ammount of arguments");
        }
    }
    
    @SuppressWarnings("unused")
    private static void cmd_delete(String[] args) {
        if (args.length > 1) {
            String entryType = args[0];
            String id = args[1];
            String entryFolder = getEntryFolderForType(entryType);
            if (entryFolderExists(entryFolder)) {
                File entryFile = new File("db" + File.separator + entryFolder + File.separator + id + _BLOGIX_SUFFIX);
                if (entryFile.exists()) { 
                    entryFile.delete();
                    info("Removed " + entryFile.getPath());
                }
                else error("No such entry '" + id + "' for type '" + entryType + "'" );
            }
            else error("There is no such type: " + entryType);
            
        }
        else error("Not enough arguments");
    }
    
    @SuppressWarnings("unused")
    private static void cmd_attach(String [] args) throws IOException {
        if (args.length > 2) {
            String entryType = args[0];
            String id = args[1];
            String entryFolder = getEntryFolderForType(entryType);
            
            if (id.equals("last")) {
                id = getLastEntryInFolder(entryFolder);
            }
            if (entryExists(entryFolder, id)) {
                for (int i = 2; i < args.length; i++) {
                    attachFileToEntry(args[i], entryFolder, id);
                }
            }
            else error("Entry '" + id + "' does not exist in " + entryFolder);
            
        }
        else error("Not enough arguments");
    }
    
    private static void attachFileToEntry(String filePath, String entryFolder, String id) throws IOException {
        File fileSrc = new File(filePath);
        if (fileSrc.exists()) {
            String name = fileSrc.getName();
            String destPath = "db" + File.separator + entryFolder + File.separator + id + "." + name;
            File fileDest = new File(destPath);
            FileUtils.copyFile(fileSrc, fileDest);
            
            info("Attached " + destPath);
        }
    }


    private static boolean entryExists(String entryFolder, String id) {
        return new File("db" + File.separator + entryFolder + File.separator + id + _BLOGIX_SUFFIX).exists();
    }


    //Returns id of entry with the latest creation date
    private static String getLastEntryInFolder(String entryFolder) {
        File folder = new File("db" + File.separator + entryFolder);
        File[] files = folder.listFiles();
        
        ArrayList<File> entryFiles = new ArrayList<File>();
        for (File file : files) {
            if (file.getName().endsWith(_BLOGIX_SUFFIX)) {
                entryFiles.add(file);
            }
        }
        Collections.sort(entryFiles, byLastModified());
        if (entryFiles.size() > 0) {
            return trimBlogixSuffixFromEntryFileName(entryFiles.get(0).getName());
        }
        error("There are no entries in '" + entryFolder + "'");
        return null;
    }


    private static Comparator<File> byLastModified() {
        return new Comparator<File>() {
            @Override
            public int compare(File fileA, File fileB) {
                int diff = (int) (fileB.lastModified() - fileA.lastModified());
                if (diff == 0) {
                    diff = fileB.getName().compareTo(fileA.getName());
                }
                return diff;
            }
        };
    }


    private static boolean entryFolderExists(String entryFolder) {
        return new File("db" + File.separator + entryFolder).exists();
    }

    private static String getEntryFolderForType(String entryType) {
        if (entryType.endsWith("y")) {
            return entryType.substring(0, entryType.length() - 1) + "ies";
        }
        else return entryType + "s";
    }


    private static void createCategory(String title) throws Exception {
        if (title.isEmpty()) {
            error("Title should not be empty");
        }
        StringBuffer buff = new StringBuffer();
        buff.append("----\n");
        buff.append("name\n");
        buff.append("   ");
        buff.append(title);
        buff.append("\n");
        
        String fileName = title.toLowerCase().replaceAll("\\s+", " ").replaceAll("\\s", "-");
        String fullPath = "db" + File.separator + "categories" + File.separator + fileName + _BLOGIX_SUFFIX;
        File file = new File(fullPath);
        file.createNewFile();
        FileUtils.writeStringToFile(file, buff.toString());
        info("created " + fullPath);
    }


    private static void createPost(String title) throws Exception {
        String id = blogixDatePrefix() + convertTitleToId(title);
        createPostTo("posts", id, title);
    }
    

    private static void createDoc(String title) throws Exception {
        createPostTo("docs", convertTitleToId(title), title);
    }
    
    private static void createPostTo(String folderName, String id, String title) throws Exception {
        if (title.isEmpty()) {
            error("Title should not be empty");
        }
        StringBuffer buff = new StringBuffer();
        buff.append("--------------------------------\n");
        buff.append("title\n");
        buff.append("   ");
        buff.append(title);
        buff.append("\n");
        buff.append("--------------------------------\n");
        buff.append("date\n");
        buff.append("   ");
        buff.append(currentDateInBlogixFormat());
        buff.append("\n");
        buff.append("--------------------------------\n");
        buff.append("allowComments\n");
        buff.append("   true");
        buff.append("\n");
        buff.append("--------------------------------\n");
        buff.append("categories\n");
        buff.append("   \n================================\n");
        
        
        String fullPath = "db" + File.separator + folderName + File.separator + id + _BLOGIX_SUFFIX;
        File file = new File(fullPath);
        file.createNewFile();
        FileUtils.writeStringToFile(file, buff.toString());
        info("created " + fullPath);
    }

    private static String convertTitleToId(String title) {
        return title.toLowerCase().replaceAll("[^\\dA-Za-z\\.\\-]", "").replaceAll("\\s+", "-");
    }

    private static String blogixDatePrefix() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()) + "-";
    }


    private static Object currentDateInBlogixFormat() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        return sdf.format(new Date());
    }


    private static void initWebsiteFromTemplate(File templateDir) throws IOException {
        FileUtils.copyDirectory(templateDir, new File("."));
    }


    private static String getAppPath() {
        String path = System.getProperty("blogix.app.path");
        if (path == null || path.isEmpty()) {
            error("Incorrect invokation. Property blogix.app.path is not specified");
        }
        return path;
    }


    private static void error(String message) {
        info(message);
        System.exit(1);
    }


    private static void info(String text) {
        System.out.println(text);
    }
    
    private static String[] shift(String[] args) {
        if (args.length > 0) {
            return Arrays.copyOfRange(args, 1, args.length);
        }
        else return new String[0];
    }
    
    private static void invokeCommand(String name, String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Method method = findCommandMethod(name);
        if (method == null) {
            info("Unknown command \"" + name + "\"");
            System.exit(1);
        }
        
        if (method.getParameterTypes().length == 0) {
            method.invoke(NO_INSTANCE);
        }
        else {
            invokeCommandWithArguments(method, args);
        }
    }


    private static void invokeCommandWithArguments(Method method, String[] args) throws IllegalArgumentException, IllegalAccessException, InvocationTargetException {
        Class<?>[] parameterTypes = method.getParameterTypes();
        if (parameterTypes[0].equals(String.class)) {
            method.invoke(NO_INSTANCE, args[0]);
        }
        else {
            method.invoke(NO_INSTANCE, new Object[]{args});
        }
        
    }
    
    private static String trimBlogixSuffixFromEntryFileName(String name) {
        return name.substring(0, name.length() - 7);
    }
}
