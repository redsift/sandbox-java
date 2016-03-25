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
            String computeJARPath = init.computeJARPath();
            //System.out.println("computeJARPath=" + computeJARPath);

            for (String n : args) {
                int i = Integer.parseInt(n);
                System.out.println("");
                //System.out.println("n: " + n + " i: " + i);
                SiftJSON.Dag.Node node = init.sift.dag.nodes[i];

                if (node.implementation == null || (node.implementation.java == null &&
                        node.implementation.scala == null && node.implementation.clojure == null)) {
                    throw new Exception("Requested to install a non-Java, non-Scala or non-Clojure node at index " + n);
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
                    if (implFile.maven != null) {
                        System.out.println("Found maven project at " + implFile.maven.path + "pom.xml");
                        // mvn package
                        String jarName = Install.runBuildTool(n, node.implementation.java, "Maven", implFile.maven.path,
                                implFile, init);

                        implFile.file = jarName;
                    } else {
                        // Compile
                        File classesFile = Install.compile(n, node.implementation.java, implFile,
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile());

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.java, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite java attribute.
                    node.implementation.java = implFile.file + ";" + implFile.className;
                    System.out.println("Rewrote JSON: " + node.implementation.java);
                } else if (implFile.impl.equals("scala")) { // Scala
                    if (implFile.sbt != null) {
                        System.out.println("Found maven project at " + implFile.sbt.path + "build.sbt");
                        // sbt package
                        String jarName = Install.runBuildTool(n, node.implementation.scala, "SBT", implFile.sbt.path,
                                implFile, init);

                        implFile.file = jarName;
                    } else {
                        // Compile
                        File classesFile = Install.compile(n, node.implementation.scala, implFile,
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile());

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.scala, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite scala attribute.
                    node.implementation.scala = implFile.file + ";" + implFile.className;
                    System.out.println("Rewrote JSON: " + node.implementation.scala);
                } else { // Clojure
                    if (implFile.lein != null) {
                        System.out.println("Found lein project at " + implFile.lein.path + "project.clj");
                        // lein uberjar
                        String jarName = Install.runBuildTool(n, node.implementation.clojure, "Lein", implFile.lein.path,
                                implFile, init);

                        implFile.file = jarName;
                    } else {
                        // Compile
                        File classesFile = Install.compileClojure(n, node.implementation.clojure, implFile,
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile());

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.clojure, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite clojure attribute.
                    node.implementation.clojure = implFile.file + ";" + implFile.className;
                    System.out.println("Rewrote JSON: " + node.implementation.clojure);
                }
            }

            Init.mapper.writeValue(new File(init.SIFT_ROOT, init.SIFT_JSON), init.sift);
        } catch (Exception ex) {
            System.err.println("Error running install: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static String executeCommand(String[] cmdarray, File dir,
                                         SiftJSON.Dag.Node.Implementation.ImplFile implFile) {
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

        if (implFile.lein != null) {
            String ignoreStrLein = "Compiling ";
            int lastIndex = out.lastIndexOf(ignoreStrLein);
            if (lastIndex > 0) {
                out = out.substring(lastIndex);
            }
            if (out.startsWith(ignoreStrLein) && out.split("\\n").length == 1) {
                //System.out.println("Lein output: " + out);
                out = "";
            }
        }
        return out;

    }

    private static String findJarFile(File toolOutputDir, Init init) {
        File[] directoryListing = toolOutputDir.listFiles();
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

        if (jarName != null) {
            jarName = jarName.replace(init.SIFT_ROOT, "");
            if (jarName.charAt(0) == '/') {
                jarName = jarName.substring(1);
            }
        }

        return jarName;
    }

    private static String runBuildTool(String n, String impl, String toolName, String toolPath,
                                 SiftJSON.Dag.Node.Implementation.ImplFile implFile, Init init) throws Exception {
        //System.out.println("runBuildTool: " + toolName + " : " + toolPath + " : " + implFile.file + " : " + implFile.className);
        File toolFile = new File(init.SIFT_ROOT, toolPath);
        File toolOutputDir = new File(toolFile.getPath(), "target");

        String[] toolCmds = new String[]{"mvn", "package"};
        if (implFile.sbt != null) {
            toolCmds = new String[]{"sbt", "package", "--error"};
        } else if (implFile.lein != null) {
            toolCmds = new String[]{"lein", "uberjar"};
        }

        String err = executeCommand(toolCmds, toolFile, implFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error building with " + toolName + " " + n + " (" + impl + "): " + err);
        }

        System.out.println(toolName + " build success");

        String jarName = Install.findJarFile(toolOutputDir, init);

        // Scala: trick to find the jar
        if (jarName == null) {
            File[] directoryListing = toolOutputDir.listFiles();
            for (File fileName : directoryListing) {
                if (fileName.getPath().contains("/target/scala")) {
                    jarName = Install.findJarFile(fileName, init);
                }
            }
        }

        return jarName;
    }

    private static File compile(String n, String impl,
                                     SiftJSON.Dag.Node.Implementation.ImplFile implFile, String implPath,
                                     String computeJARPath, File parentFile) throws Exception {
        File classesFile = new File(parentFile.getPath(), "classes/" + implFile.impl);
        //System.out.println(classesFile.getPath() + " exists: " + classesFile.exists() + " implPath=" + implPath);
        if (!classesFile.exists()) {
            //System.out.println("Making dir " + classesFile.getPath());
            classesFile.mkdirs();
        }

        String err = executeCommand(new String[]{implFile.impl + "c", "-nowarn", "-d", "classes/" + implFile.impl,
                "-classpath", computeJARPath,
                implPath}, parentFile, implFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error compiling Node " + n + " (" + impl + "): " + err);
        }

        System.out.println("Compiled node");
        return classesFile;
    }

    private static File compileClojure(String n, String impl,
                                SiftJSON.Dag.Node.Implementation.ImplFile implFile, String implPath,
                                String computeJARPath, File parentFile) throws Exception {
        File classesFile = new File(parentFile.getPath(), "classes/" + implFile.impl);
        //System.out.println(classesFile.getPath() + " exists: " + classesFile.exists() + " implPath=" + implPath);
        if (!classesFile.exists()) {
            //System.out.println("Making dir " + classesFile.getPath());
            classesFile.mkdirs();
        }

        String err = executeCommand(new String[]{implFile.impl + "c", "-nowarn", "-d", "classes/" + implFile.impl,
                "-classpath", computeJARPath,
                implPath}, parentFile, implFile);
        if (err != null && err.length() > 0) {
            throw new Exception("Error compiling Node " + n + " (" + impl + "): " + err);
        }

        System.out.println("Compiled node");
        return classesFile;
    }

    private static String createJAR(String n, String impl,
                                      SiftJSON.Dag.Node.Implementation.ImplFile implFile,
                                       File classesFile, Init init) throws Exception {
        List<String> args = new ArrayList<String>();
        args.add("jar");
        args.add("cvf");
        String classFile = implFile.className.replace(".", "/");
        classFile += ".class";
        String jarFile = classFile.replace(".class", ".jar");
        String classFile1 = classFile.replace(".class", "*.class");
        //System.out.println("createJAR file = " + implFile.file);
        //System.out.println("createJAR classFile = " + classFile);
        //System.out.println("createJAR jarFile = " + jarFile);
        //System.out.println("createJAR className = " + implFile.className);
        //System.out.println("createJAR classesdir = "+ classesFile);

        String[] jarCmds = new String[]{"jar", "cvf", jarFile, classFile};

        String err = executeCommand(jarCmds, classesFile, implFile);
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
