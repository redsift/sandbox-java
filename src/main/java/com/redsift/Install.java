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

                if (node.implementation == null || node.implementation.java == null) {
                    throw new Exception("Requested to install a non-Java node at index " + n);
                }

                System.out.println("Installing node: " + node.description + " : " + node.implementation.java);
                SiftJSON.Dag.Node.Implementation.JavaFile javaFile = node.implementation.javaFile();
                if (javaFile.file.contains(".jar")) {
                    System.out.println("Already installed, skipping.");
                    continue;
                }

                File implFile = new File(init.SIFT_ROOT, javaFile.file);
                if (!implFile.exists()) {
                    throw new Exception("Implementation at index " + n + " (" + node.implementation.java + ") does not exist!");
                }

                if (javaFile.maven) {
                    System.out.println("Found maven project at " + javaFile.mavenPath + "pom.xml");
                    // mvn package
                    String jarName = Install.runMaven(n, node, javaFile, init);

                    // Rewrite java attribute.
                    node.implementation.java = jarName + ";" + javaFile.className;
                } else {
                    // Compile
                    Install.compileJava(n, node, implFile.getPath(), selfJARPath);

                    // Create JAR
                    Install.createJavaJAR(n, node, javaFile, init);

                    // Rewrite java attribute.
                    node.implementation.java = javaFile.file.replace(".java", ".jar") + ";" + javaFile.className;
                }
                System.out.println("Rewrote JSON: " + node.implementation.java);
            }

            Init.mapper.writeValue(new File(init.SIFT_ROOT, init.SIFT_JSON), init.sift);
        } catch (Exception ex) {
            System.out.println("Error running install: " + ex.toString());
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

    private static void compileJava(String n, SiftJSON.Dag.Node node, String implPath,
                                    String selfJARPath) throws Exception {
        String err = executeCommand(new String[]{"javac", "-nowarn", "-classpath", selfJARPath, implPath}, null);
        if (err != null && err.length() > 0) {
            throw new Exception("Error compiling Node " + n + " (" + node.implementation.java + "): " + err);
        }

        System.out.println("Compiled node");
    }

    private static void createJavaJAR(String n, SiftJSON.Dag.Node node,
                                      SiftJSON.Dag.Node.Implementation.JavaFile javaFile, Init init) throws Exception {
        String workDir = "";
        List<String> args = new ArrayList<String>();
        args.add("jar");
        args.add("cvf");
        String jarFile = javaFile.file.replace(".java", ".jar");
        String classFile = javaFile.file.replace(".java", ".class");
        if (javaFile.userSpecified) {
            workDir = javaFile.file.replace(javaFile.className.replace(".", "/"), "");
            workDir = workDir.replace(".java", "");
            jarFile = jarFile.replace(workDir, "");
            classFile = classFile.replace(workDir, "");
        }
        //System.out.println("user file=" + javaFile.file);
        //System.out.println("user className=" + javaFile.className);

        String[] jarCmds = new String[]{"jar", "cvf", jarFile, classFile};

        String err = executeCommand(jarCmds, new File(init.SIFT_ROOT, workDir));
        if (err != null && err.length() > 0) {
            throw new Exception("Error creating jar for Node " + n + " (" + node.implementation.java + "): " + err);
        }

        System.out.println("Created JAR");
    }

    private static String runMaven(String n, SiftJSON.Dag.Node node,
                                 SiftJSON.Dag.Node.Implementation.JavaFile javaFile, Init init) throws Exception {
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
}
