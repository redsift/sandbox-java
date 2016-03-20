package com.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
class SiftJSON {
    public Dag dag;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dag {
        public Node[] nodes;

        public Dag(@JsonProperty("nodes") Node[] nodes) {
            this.nodes = nodes;
        }

        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Node {
            public String description;
            public Implementation implementation;
            public Node(@JsonProperty("#") String description, @JsonProperty("implementation") Implementation implementation) {
                this.description = description;
                this.implementation = implementation;
            }

            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Implementation {
                public String java;

                public Implementation(@JsonProperty("java") String java) {
                    this.java = java;
                }

                public JavaFile javaFile() {
                    JavaFile javaFile = new JavaFile();

                    String[] strs = this.java.split(";");
                    javaFile.file = strs[0];
                    if (strs.length == 2) {
                        javaFile.className = strs[1];
                        javaFile.userSpecified = true;
                    } else {
                        String className = this.java.replace("/", ".");
                        className = className.replace(".java", "");
                        className = className.replace(";", "");
                        javaFile.className = className;
                    }

                    return javaFile;
                }

                public List<String> jarCommand(JavaFile javaFile) {
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
                    System.out.println("user file=" + javaFile.file);
                    System.out.println("user className=" + javaFile.className);
                    args.add(jarFile);
                    args.add(classFile);
                    args.add(workDir);

                    System.out.println(args.toString());
                    return args;
                }

                public static class JavaFile {
                    public String file;
                    public String className;
                    public Boolean userSpecified = false;
                }

            }
        }
    }
}

public class Init {
    public static ObjectMapper mapper = new ObjectMapper();
    public String[] nodes;
    public String SIFT_ROOT;
    public String SIFT_JSON;
    public String IPC_ROOT;
    public Boolean DRY;
    public SiftJSON sift;

    public Init(String args[]) throws Exception {
        if (args.length <= 0) {
            throw new Exception("No nodes to execute");
        }

        String SIFT_ROOT = System.getenv("SIFT_ROOT");
        String SIFT_JSON = System.getenv("SIFT_JSON");
        String IPC_ROOT = System.getenv("IPC_ROOT");
        Boolean DRY = System.getenv("DRY") == "true";

        if (SIFT_ROOT == null || SIFT_ROOT == "") {
            throw new Exception("Environment SIFT_ROOT not set");
        }

        File file = new File(SIFT_ROOT);
        if (!file.isAbsolute()) {
            throw new Exception("Environment SIFT_ROOT '" + SIFT_ROOT + "' must be absolute");
        }

        if (SIFT_JSON == null || SIFT_JSON == "") {
            throw new Exception("Environment SIFT_JSON not set");
        }

        if (IPC_ROOT == null || IPC_ROOT == "") {
            throw new Exception("Environment IPC_ROOT not set");
        }

        if (DRY) {
            System.out.println("Unit Test Mode");
        }

        SiftJSON sift = mapper.readValue(new File(SIFT_ROOT, SIFT_JSON), SiftJSON.class);
        if (sift == null || sift.dag == null || sift.dag.nodes == null || sift.dag.nodes.length == 0) {
            throw new Exception("Sift does not contain any nodes");
        }

        this.nodes = args;
        this.SIFT_ROOT = SIFT_ROOT;
        this.SIFT_JSON = SIFT_JSON;
        this.IPC_ROOT = IPC_ROOT;
        this.DRY = DRY;
        this.sift = sift;
    }

    public String selfJARPath() {
        String selfJarPath = Init.class.getResource('/'+Init.class.getName().replace('.', '/')+".class").getPath().replace("file:", "");
        selfJarPath = selfJarPath.split("!")[0];
        return selfJarPath;
    }
}
