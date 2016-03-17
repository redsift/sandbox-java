package com.redsift;

import java.io.File;
import java.util.Arrays;

public class Install {

    public static void main(String args[]) throws Exception {
 
        System.out.println("Install: " + Arrays.toString(args));

        Init init = new Init(args);

        for (String n: args) {
            int i = Integer.parseInt(n);
            System.out.println("n: " + n + " i: " + i);
            SiftJSON.Dag.Node node = init.sift.dag.nodes[i];
            System.out.println(node);

            if (node.implementation == null || node.implementation.java == null) {
                throw new Exception("Requested to install a non-Java node at index " + n);
            }

            System.out.println("Installing node: " + node.description + " : " + node.implementation.java);
            File implFile = new File(init.SIFT_ROOT, node.implementation.java);
            if (!implFile.exists()) {
                throw new Exception("Implementation at index " + n + " (" + node.implementation.java + ") does not exist!");
            }
        }
    }

}
