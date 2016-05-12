package io.redsift;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class Install {

    public static void main(String args[]) throws Exception {
        try {
            System.out.println("Install: " + Arrays.toString(args));

            Install.installComputeJAR();

            Init init = new Init(args);
            String computeJARPath = Init.computeJARPath();
            //System.out.println("computeJARPath=" + computeJARPath);
            JSONObject jsonObject = Install.getSiftJSON(init);
            JSONObject dagObject = (JSONObject) jsonObject.get("dag");
            JSONArray dagNodes = (JSONArray) dagObject.get("nodes");
            for (String n : args) {
                int i = Integer.parseInt(n);
                //System.out.println("n: " + n + " i: " + i);
                SiftJSON.Dag.Node node = init.sift.dag.nodes[i];
                JSONObject dagNode = (JSONObject) dagNodes.get(i);
                JSONObject dagImplementation = (JSONObject) dagNode.get("implementation");

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
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile(), init);

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.java, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite java attribute.
                    node.implementation.java = implFile.file + ";" + implFile.className;
                    dagImplementation.put("java", implFile.file + ";" + implFile.className);
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
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile(), init);

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.scala, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite scala attribute.
                    node.implementation.scala = implFile.file + ";" + implFile.className;
                    dagImplementation.put("scala", implFile.file + ";" + implFile.className);
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
                        File classesFile = Install.compile(n, node.implementation.clojure, implFile,
                                implementationFile.getPath(), computeJARPath, implementationFile.getParentFile(), init);

                        // Create JAR
                        String jarName = Install.createJAR(n, node.implementation.clojure, implFile, classesFile, init);

                        implFile.file = jarName;
                    }
                    // Rewrite clojure attribute.
                    node.implementation.clojure = implFile.file + ";" + implFile.className;
                    dagImplementation.put("clojure", implFile.file + ";" + implFile.className);
                    System.out.println("Rewrote JSON: " + node.implementation.clojure);
                }
            }

            //Init.mapper.writeValue(new File(init.SIFT_ROOT, init.SIFT_JSON), init.sift);
            Install.saveSiftJSON(jsonObject, init);
        } catch (Exception ex) {
            System.err.println("Error running install: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

    private static boolean executeCommand(String[] cmdarray, File dir,
                                         SiftJSON.Dag.Node.Implementation.ImplFile implFile) {
        Process p;
        int exitCode = 1;
        try {
            p = Runtime.getRuntime().exec(cmdarray, null, dir);

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String inLine = "";
            while ((inLine = in.readLine()) != null) {
                System.out.println(inLine);
            }
            in.close();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            reader.close();

            exitCode = p.waitFor();
        } catch (Exception e) {
            System.out.println("Exception executing command!" + cmdarray.toString());
            e.printStackTrace();
        }

        System.out.println("Process exited with code " + exitCode);
        return exitCode == 0;

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

        String[] toolCmds = new String[]{"mvn", "clean"};
        if (implFile.sbt != null) {
            toolCmds = new String[]{"sbt", "package", "--error"};
        } else if (implFile.lein != null) {
            toolCmds = new String[]{"lein", "uberjar"};
        }

        boolean success = executeCommand(toolCmds, toolFile, implFile);
        if (!success) {
            throw new Exception("Error building with " + toolName + " " + n + " (" + impl + ")");
        }

        if (implFile.maven != null) {
            String[] toolCmds1 = new String[]{"mvn", "install"};
            boolean success1 = executeCommand(toolCmds1, toolFile, implFile);
            if (!success1) {
                throw new Exception("Error building with " + toolName + " " + n + " (" + impl + ")");
            }
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
                                String computeJARPath, File parentFile, Init init) throws Exception {
        File classesFile = new File(parentFile.getPath(), "classes/" + implFile.impl);
        //System.out.println(classesFile.getPath() + " exists: " + classesFile.exists() + " implPath=" + implPath);
        if (!classesFile.exists()) {
            //System.out.println("Making dir " + classesFile.getPath());
            classesFile.mkdirs();
        }

        String[] cmds = new String[]{implFile.impl + "c", "-nowarn", "-d", "classes/" + implFile.impl,
                "-cp", computeJARPath, implPath};
        File cmdDir = parentFile;
        if (implFile.impl.equals("clj")) {
            //java -cp /Users/deepakp/workspace/containers/sandbox-java/target/compute.jar:/Users/deepakp/workspace/containers/sandbox-clojure/test:/Users/deepakp/workspace/containers/sandbox-clojure/target/clojure-1.8.0.jar
            // -Dclojure.compile.path=/Users/deepakp/workspace/containers/sandbox-clojure/test/classes clojure.lang.Compile server.node1
            String basePath = implFile.file;
            String className = implFile.className;
            className = className.replace("$compute", "");
            basePath = basePath.replace(".clj", "");
            basePath = basePath.replace(className.replace(".", "/"), "");
            cmdDir = new File(init.SIFT_ROOT, basePath);
            cmds = new String[]{"java", "-cp", computeJARPath + ":" + cmdDir.getPath() + ":" + Init.clojureJARPath(),
                    "-Dclojure.compile.path=" + classesFile.getPath(), "clojure.lang.Compile", className};
            //System.out.println("basePath= " + basePath);
            //System.out.println("cmds= " + Arrays.toString(cmds));
        }

        boolean success = executeCommand(cmds, cmdDir, implFile);

        if (!success) {
            throw new Exception("Error compiling Node " + n + " (" + impl + ")");
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

        boolean success = executeCommand(jarCmds, classesFile, implFile);
        if (!success) {
            throw new Exception("Error creating jar for Node " + n + " (" + impl + ")");
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


    private static JSONObject getSiftJSON(Init init) throws Exception {

        JSONParser parser = new JSONParser();

        Object obj = parser.parse(new FileReader(new File(init.SIFT_ROOT, init.SIFT_JSON).getPath()));

        JSONObject jsonObject = (JSONObject) obj;

        return jsonObject;
    }

    private static void saveSiftJSON(JSONObject jsonObject, Init init) throws Exception {
        FileWriter file = new FileWriter(new File(init.SIFT_ROOT, init.SIFT_JSON).getPath());
        file.write(jsonObject.toJSONString());
        file.flush();
        file.close();
    }

    private static void installComputeJAR() {
        Process p;
        try {
            p = Runtime.getRuntime().exec("mvn install:install-file -Dfile=/usr/bin/redsift/compute.jar -DgroupId=io.redsift -DartifactId=compute -Dversion=1.0 -Dpackaging=jar");

            BufferedReader in = new BufferedReader(
                    new InputStreamReader(p.getInputStream()));
            String inLine = "";
            while ((inLine = in.readLine()) != null) {
                System.out.println(inLine);
            }
            in.close();

            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.err.println(line);
            }
            reader.close();

        } catch (Exception e) {
            System.out.println("Exception executing installComputeJAR command!");
            e.printStackTrace();
        }
    }
}
