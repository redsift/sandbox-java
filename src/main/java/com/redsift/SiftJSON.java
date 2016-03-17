package com.redsift;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SiftJSON {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Dag {
        @JsonIgnoreProperties(ignoreUnknown = true)
        public static class Node {
            @JsonIgnoreProperties(ignoreUnknown = true)
            public static class Implementation {
                public String java;

                public Implementation(@JsonProperty("java")String java) {
                    System.out.println("Init Implementation: " + java);
                    this.java = java;
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
