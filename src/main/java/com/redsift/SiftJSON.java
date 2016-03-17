package com.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiftJSON {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dag {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Node {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Implementation {
                public static class JavaFile {
                    public String file;
                    public String className;
                    public Boolean userSpecified = false;
                }

                public String java;

                public Implementation(@JsonProperty("java")String java) {
                    System.out.println("Init Implementation: " + java);
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

                    System.out.println("file= " + javaFile.file);
                    System.out.println("className= " + javaFile.className);

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
                        System.out.println("user file="+javaFile.file);
                        System.out.println("user className="+javaFile.className);
                        workDir = javaFile.file.replace(javaFile.className.replace(".", "/"), "");
                        workDir = workDir.replace(".java", "");
                        jarFile = jarFile.replace(workDir, "");
                        classFile = classFile.replace(workDir, "");
                    }
                    args.add(jarFile);
                    args.add(classFile);
                    args.add(workDir);

                    System.out.println(args.toString());
                    return args;
                }

            }

            public String description;
            public Implementation implementation;

            public Node(@JsonProperty("#")String description, @JsonProperty("implementation")Implementation implementation) {
                System.out.println("Init Node: " + description + " : " + implementation);
                this.description = description;
                this.implementation = implementation;
            }
        }

        public Node[] nodes;

        public Dag(@JsonProperty("nodes")Node[] nodes) {
            System.out.println("Init Dag: " + nodes);
            this.nodes = nodes;
        }
    }

    public Dag dag;
}
