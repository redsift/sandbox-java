package com.redsift;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Install {

    public static void main(String args[]) throws Exception {
        try {
            System.out.println("Install: " + Arrays.toString(args));

            Init init = new Init(args);
            String selfJARPath = init.selfJARPath();
            //System.out.println("selfJARPath=" + selfJARPath);

            for (String n : args) {
                int i = Integer.parseInt(n);
                System.out.println("");
                System.out.println("n: " + n + " i: " + i);
                SiftJSON.Dag.Node node = init.sift.dag.nodes[i];

                if (node.implementation == null || (node.implementation.java == null && node.implementation.scala == null)) {
                    throw new Exception("Requested to install a non-Java or non-Scala node at index " + n);
                }

                SiftJSON.Dag.Node.Implementation.ImplFile implFile = node.implementation.implFile();

                System.out.println("Installing node: " + node.description + " : " + implFile.file);

                if (implFile.file.contains(".jar")) {
                    System.out.println("Already installed, skipping.");
                    continue;
                }

                File implementationFile = new File(init.SIFT_ROOT, implFile.file);
                if (!implementationFile.exists()) {
                    throw new Exception("Implementation at index " + n + " (" + implFile.file + ") does not exist!");
                }

                if (implFile.impl.equals("java")) {
                    if (implFile.maven) {
                        System.out.println("Found maven project at " + implFile.mavenPath + "pom.xml");
                        // mvn package
                        String jarName = Install.runMaven(n, node, implFile, init);

                        // Rewrite java attribute.
                        implFile.file = jarName;
                        node.implementation.java = jarName + ";" + implFile.className;
                    } else {
                        // Compile
                        File classesFile = Install.compile(n, node.implementation.java, implFile,
                                implementationFile.getPath(), selfJARPath, implementationFile.getParentFile());

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.java, implFile, classesFile, init);

                        // Rewrite scala attribute.
                        implFile.file = jarName;
                        node.implementation.java = jarName + ";" + implFile.className;
                    }
                } else { // Scala
                    // Compile
                    File classesFile = Install.compile(n, node.implementation.scala, implFile,
                            implementationFile.getPath(), selfJARPath, implementationFile.getParentFile());

                    // Create JAR
                    String jarName = Install.createJAR(n, node.implementation.scala, implFile, classesFile, init);

                    // Rewrite scala attribute.
                    implFile.file = jarName;
                    node.implementation.scala = jarName + ";" + implFile.className;
                }

                System.out.println("Rewrote JSON: " + implFile.file);
            }

            Init.mapper.writeValue(new File(init.SIFT_ROOT, init.SIFT_JSON), init.sift);
        } catch (Exception ex) {
            System.err.println("Error running install: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static String executeCommand(String[] cmdarray, File dir) {

        StringBuffer output = new StringBuffer();

        Process p;
        try {
            p = Runtime.getRuntime().exec(cmdarray, null, dir);
            p.waitFor();
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));

            String line = "";
            while ((line = reader.readLine()) != null) {
                output.append(line + "\n");
            }

        } catch (Exception e) {
            System.out.println("Exception executing command!" + cmdarray.toString());
            e.printStackTrace();
        }
        String out = output.toString();

        // This is to avoid the "Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF8" message in stderr.
        String ignoreStr = "Picked up JAVA_TOOL_OPTIONS: -Dfile.encoding=UTF8\n";
        if (out.equals(ignoreStr)) {
            out = "";
        }
        return out;

    }

    private static String runMaven(String n, SiftJSON.Dag.Node node,
                                 SiftJSON.Dag.Node.Implementation.ImplFile javaFile, Init init) throws Exception {
        File mavenFile = new File(init.SIFT_ROOT, javaFile.mavenPath);
        File mavenOutputDir = new File(mavenFile.getPath(), "target");

        String err = executeCommand(new String[]{"mvn", "package"}, mavenFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error building with Maven " + n + " (" + node.implementation.java + "): " + err);
        }

        System.out.println("Maven build success");

        File[] directoryListing = mavenOutputDir.listFiles();
        String jarName = null;
        long jarSize = 0;
        for (File fileName : directoryListing) {
            if (fileName.getPath().contains(".jar")) {
                if (jarName == null) {
                    jarName = fileName.getPath();
                    jarSize = fileName.length();
                } else {
                    if (fileName.length() > jarSize) {
                        jarName = fileName.getPath();
                        jarSize = fileName.length();
                    }
                }
            }
        }

        jarName = jarName.replace(init.SIFT_ROOT, "");
        if (jarName.charAt(0) == '/') {
            jarName = jarName.substring(1);
        }

        return jarName;
    }

    private static File compile(String n, String impl,
                                     SiftJSON.Dag.Node.Implementation.ImplFile implFile, String implPath,
                                     String selfJARPath, File parentFile) throws Exception {
        File classesFile = new File(parentFile.getPath(), "classes/" + implFile.impl);
        //System.out.println(classesFile.getPath() + " exists: " + classesFile.exists() + " implPath=" + implPath);
        if (!classesFile.exists()) {
            //System.out.println("Making dir " + classesFile.getPath());
            classesFile.mkdirs();
        }

        String err = executeCommand(new String[]{implFile.impl + "c", "-nowarn", "-d", "classes/" + implFile.impl, "-classpath", selfJARPath,
                implPath}, parentFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error compiling Node " + n + " (" + impl + "): " + err);
        }

        System.out.println("Compiled node");
        return classesFile;
    }

    private static String createJAR(String n, String impl,
                                      SiftJSON.Dag.Node.Implementation.ImplFile scalaFile,
                                       File classesFile, Init init) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("jar");
        args.add("cvf");
        String classFile = scalaFile.className.replace(".", "/");
        classFile += ".class";
        String jarFile = classFile.replace(".class", ".jar");
        String classFile1 = classFile.replace(".class", "$.class");
        //System.out.println("user file=" + scalaFile.file);
        //System.out.println("user classFile=" + classFile);
        //System.out.println("user classFile1=" + classFile1);
        //System.out.println("user jarFile=" + jarFile);
        //System.out.println("user className=" + scalaFile.className);

        String[] jarCmds = new String[]{"jar", "cvf", jarFile, classFile + (scalaFile.impl.equals("scala") ? " " + classFile1 : "")};

        String err = executeCommand(jarCmds, classesFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error creating jar for Node " + n + " (" + impl + "): " + err);
        }

        System.out.println("Created JAR");

        File fullJarFile = new File(classesFile.getPath(), jarFile);
        String jarName = fullJarFile.getPath();
        jarName = jarName.replace(init.SIFT_ROOT, "");
        if (jarName.charAt(0) == '/') {
            jarName = jarName.substring(1);
        }
        return jarName;
    }
}
