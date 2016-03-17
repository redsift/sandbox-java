package com.redsift;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Bootstrap {

    public static void main(String args[]) throws Exception {

        System.out.println("Bootstrap: " + Arrays.toString(args));

        Init init = new Init(args);

        for (String n : args) {
            int i = Integer.parseInt(n);
            System.out.println("");
            System.out.println("n: " + n + " i: " + i);
            SiftJSON.Dag.Node node = init.sift.dag.nodes[i];

            if (node.implementation == null || node.implementation.java == null) {
                throw new Exception("Requested to run a non-Java node at index " + n);
            }

            System.out.println("Running node: " + node.description + " : " + node.implementation.java);
            SiftJSON.Dag.Node.Implementation.JavaFile javaFile = node.implementation.javaFile();
            if (!javaFile.file.contains(".jar")) {
                throw new Exception("Node not installed, bailing out!");
            }

            File jarFile = new File(init.SIFT_ROOT, javaFile.file);
            // feed your URLs to a URLClassLoader!
            ClassLoader classloader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()},
                    ClassLoader.getSystemClassLoader().getParent());

            // relative to that classloader, find the main class
            // you want to bootstrap, which is the first cmd line arg
            Class mainClass = classloader.loadClass(javaFile.className);
            @SuppressWarnings("unchecked")
            Method compute = mainClass.getMethod("compute", (Class[]) null);

            // well-behaved Java packages work relative to the
            // context classloader.  Others don't (like commons-logging)
            Thread.currentThread().setContextClassLoader(classloader);
            compute.invoke(null);
        }
        
    }

}
