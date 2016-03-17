package com.redsift;

import java.util.Arrays;

public class Install {

    public static void main(String args[]) throws Exception {
 
    /*
    Use System.out.println() to print on console.
    */
        System.out.println("Install: " + Arrays.toString(args));

        Init init = new Init(args);
    }

}
