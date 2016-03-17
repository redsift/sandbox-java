package com.redsift;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

public class Bootstrap {

    public static void main(String args[]) throws MalformedURLException, ClassNotFoundException, NoSuchMethodException,
            IllegalAccessException, InvocationTargetException {
 
    /*
    Use System.out.println() to print on console.
    */
        System.out.println("Bootstrap: " + Arrays.toString(args));

        String HOME = "file:///Users/deepakp/workspace/containers/sandbox-java/test/server";

        // add the classes dir and each jar in lib to a List of URLs.
        //List urls = new ArrayList();
        //urls.add(new File(HOME).toURI().toURL());

        // feed your URLs to a URLClassLoader!
        ClassLoader classloader =
                new URLClassLoader(
                        new URL[]{
                                new URL(
                                        "file:///Users/deepakp/workspace/containers/sandbox-java/test/server/"
                                )
                        },
                        ClassLoader.getSystemClassLoader().getParent());

        // relative to that classloader, find the main class
        // you want to bootstrap, which is the first cmd line arg
        Class mainClass = classloader.loadClass("Node1");
        @SuppressWarnings("unchecked")
        Method compute = mainClass.getMethod("compute",
                (Class[]) null);

        // well-behaved Java packages work relative to the
        // context classloader.  Others don't (like commons-logging)
        Thread.currentThread().setContextClassLoader(classloader);
        compute.invoke(null);
    }

}
