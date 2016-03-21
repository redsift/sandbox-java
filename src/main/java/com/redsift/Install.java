package com.redsift;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
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

                String implPath = implFile.getPath();
                //System.out.println("implPath=" + implPath);
                // Compile
                String err = executeCommand(new String[]{"javac", "-nowarn", "-classpath", selfJARPath, implPath}, null);
                if (err != null && err.length() > 0) {
                    throw new Exception("Error compiling Node " + n + " (" + node.implementation.java + "): " + err);
                }

                System.out.println("Compiled node");

                // JARify. jar cvf Node.jar Node.class
                List<String> jarCmds = node.implementation.jarCommand(javaFile);
                String workDir = jarCmds.get(jarCmds.size() - 1);
                jarCmds.remove(jarCmds.size() - 1);
                err = null;
                err = executeCommand(jarCmds.toArray(new String[0]), new File(init.SIFT_ROOT, workDir));
                if (err != null && err.length() > 0) {
                    throw new Exception("Error creating jar for Node " + n + " (" + node.implementation.java + "): " + err);
                }

                System.out.println("Created JAR");

                node.implementation.java = javaFile.file.replace(".java", ".jar") + ";" + javaFile.className;
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
}
