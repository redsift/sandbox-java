package com.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;

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
                public String java = null;
                public String scala = null;
                public String clojure = null;

                public Implementation(@JsonProperty("java") String java, @JsonProperty("scala") String scala,
                                      @JsonProperty("clojure") String clojure) {
                    this.java = java;
                    this.scala = scala;
                    this.clojure = clojure;
                }

                public ImplFile implFile() {
                    ImplFile implFile = new ImplFile();

                    String impl = this.java;
                    implFile.impl = "java";
                    if (this.scala != null) {
                        impl = this.scala;
                        implFile.impl = "scala";
                    } else if (this.clojure != null) { // Clojure
                        impl = this.clojure;
                        implFile.impl = "clj";
                    }

                    String[] strs = impl.split(";");
                    implFile.file = strs[0];
                    if (strs.length == 2) {
                        implFile.className = strs[1];
                    } else {
                        String className = impl.replace("/", ".");
                        className = className.replace("." + implFile.impl, "");
                        className = className.replace(";", "");
                        implFile.className = className;
                    }

                    // Check for maven project
                    if (implFile.impl.equals("java")) {
                        if (implFile.file.contains("src/main/java/")) {
                            String[] mstrs = implFile.file.split("src/main/java/");
                            String mavenFile = mstrs[0];
                            String mavenClassName = mstrs[1];
                            mavenClassName = mavenClassName.replace("/", ".");
                            mavenClassName = mavenClassName.replace(".java", "");
                            mavenClassName = mavenClassName.replace(";", "");

                            implFile.className = mavenClassName;
                            implFile.maven = new ImplFile.MavenTool();
                            implFile.maven.path = mavenFile;
                            implFile.buildTool = true;
                        }
                    } else if (implFile.impl.equals("scala")) { // Check for sbt Scala
                        if (implFile.file.contains("src/main/scala/")) {
                            String[] mstrs = implFile.file.split("src/main/scala/");
                            String sbtFile = mstrs[0];
                            String sbtClassName = mstrs[1];
                            sbtClassName = sbtClassName.replace("/", ".");
                            sbtClassName = sbtClassName.replace(".scala", "");
                            sbtClassName = sbtClassName.replace(";", "");

                            implFile.className = sbtClassName;
                            implFile.sbt = new ImplFile.SbtTool();
                            implFile.sbt.path = sbtFile;
                            implFile.buildTool = true;
                        }
                    } else { // Clojure
                        if (!implFile.className.endsWith("$compute")) {
                            implFile.className += "$compute";
                        }
                        if (implFile.file.contains("src/")) {
                            int lastIndex = implFile.file.lastIndexOf("src/");
                            String sbtFile = implFile.file.substring(0, lastIndex);
                            String sbtClassName = implFile.file.substring(lastIndex + "src/".length());
                            sbtClassName = sbtClassName.replace("/", ".");
                            sbtClassName = sbtClassName.replace(".clj", "");
                            sbtClassName = sbtClassName.replace(";", "");
                            // TODO: - vs _

                            implFile.className = sbtClassName;
                            implFile.lein = new ImplFile.LeinTool();
                            implFile.lein.path = sbtFile;
                            implFile.buildTool = true;
                        }
                    }

                    return implFile;
                }

                public static class ImplFile {
                    public String impl;
                    public String file;
                    public String className;
                    public Boolean buildTool = false;
                    public MavenTool maven = null;
                    public SbtTool sbt = null;
                    public LeinTool lein = null;

                    public static class MavenTool {
                        public String path;
                    }

                    public static class SbtTool {
                        public String path;
                    }

                    public static class LeinTool {
                        public String path;
                    }
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
    public Boolean DRY = false;
    public SiftJSON sift;

    public Init(String args[]) throws Exception {
        if (args.length <= 0) {
            throw new Exception("No nodes to execute");
        }

        String SIFT_ROOT = System.getenv("SIFT_ROOT");
        String SIFT_JSON = System.getenv("SIFT_JSON");
        String IPC_ROOT = System.getenv("IPC_ROOT");
        Boolean DRY = System.getenv("DRY") != null && System.getenv("DRY").equals("true");

        if (SIFT_ROOT == null || SIFT_ROOT.equals("")) {
            throw new Exception("Environment SIFT_ROOT not set");
        }

        File file = new File(SIFT_ROOT);
        if (!file.isAbsolute()) {
            throw new Exception("Environment SIFT_ROOT '" + SIFT_ROOT + "' must be absolute");
        }

        if (SIFT_JSON == null || SIFT_JSON.equals("")) {
            throw new Exception("Environment SIFT_JSON not set");
        }

        if (IPC_ROOT == null || IPC_ROOT.equals("")) {
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

    public static String selfJARPath() {
        String selfJarPath = Init.class.getResource('/' + Init.class.getName().replace('.', '/') + ".class").getPath().replace("file:", "");
        return selfJarPath.split("!")[0];
    }

    public static String computeJARPath() {
        String selfJARPath = Init.selfJARPath();
        File selfJARFile = new File(selfJARPath);
        String jarParentFile = selfJARFile.getParent();
        return new File(jarParentFile, "compute.jar").getPath();
    }

    public static String clojureJARPath() {
        String selfJARPath = Init.selfJARPath();
        File selfJARFile = new File(selfJARPath);
        String jarParentFile = selfJARFile.getParent();
        return new File(jarParentFile, "clojure.jar").getPath();
    }
}
