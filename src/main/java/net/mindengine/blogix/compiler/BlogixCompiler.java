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
package net.mindengine.blogix.compiler;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class BlogixCompiler {
    
    private File classesDir;
    private File sourceDir;
    
    public void loadClassesFromCompiledDirectory() throws Exception {
        new URLClassLoader(new URL[]{classesDir.toURI().toURL()});
    }
    
    public void compile() throws CompilationError {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnostics, Locale.getDefault(), null);
        List<JavaFileObject> javaObjects = scanRecursivelyForJavaObjects(sourceDir, fileManager);
        
        if (javaObjects.size() == 0) {
            throw new CompilationError("There are no source files to compile in " + sourceDir.getAbsolutePath());
        }
        String[] compileOptions = new String[]{"-d", classesDir.getAbsolutePath()} ;
        Iterable<String> compilationOptions = Arrays.asList(compileOptions);
        
        CompilationTask compilerTask = compiler.getTask(null, fileManager, diagnostics, compilationOptions, null, javaObjects) ;
        
        if (!compilerTask.call()) {
            for (Diagnostic<?> diagnostic : diagnostics.getDiagnostics()) {
                System.err.format("Error on line %d in %s", diagnostic.getLineNumber(), diagnostic);
            }
            throw new CompilationError("Could not compile project");
        }
    }

    private List<JavaFileObject> scanRecursivelyForJavaObjects(File dir, StandardJavaFileManager fileManager) {
        List<JavaFileObject> javaObjects = new LinkedList<JavaFileObject>();
        File[] files = dir.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                javaObjects.addAll(scanRecursivelyForJavaObjects(file, fileManager));
            }
            else if (file.isFile() && file.getName().toLowerCase().endsWith(".java")) {
                javaObjects.add(readJavaObject(file, fileManager));
            }
        }
        return javaObjects;
    }


    private JavaFileObject readJavaObject(File file, StandardJavaFileManager fileManager) {
        Iterable<? extends JavaFileObject> javaFileObjects = fileManager.getJavaFileObjects(file);
        Iterator<? extends JavaFileObject> it = javaFileObjects.iterator();
        if (it.hasNext()) {
            return it.next();
        }
        throw new RuntimeException("Could not load " + file.getAbsolutePath() + " java file object");
    }

    public File getClassesDir() {
        return classesDir;
    }

    public void setClassesDir(File classesDir) {
        this.classesDir = classesDir;
    }

    public File getSourceDir() {
        return sourceDir;
    }

    public void setSourceDir(File sourceDir) {
        this.sourceDir = sourceDir;
    }
}
