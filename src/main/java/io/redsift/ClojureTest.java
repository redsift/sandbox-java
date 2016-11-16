package io.redsift;

import clojure.java.api.Clojure;
import clojure.lang.IFn;

/**
 * Created by deepakp on 16/11/2016.
 */

public class ClojureTest {
    public static void main(String[] args) throws Exception {
        IFn require = Clojure.var("clojure.core", "require");

        System.out.println("LOADING clojure.string");
        require.invoke(Clojure.read("clojure.string"));

        IFn version = Clojure.var("clojure.core", "clojure-version");
        System.out.println("LOADING clojure.core.version: " + version.invoke());

        IFn trim = Clojure.var("clojure.string", "trim");
        System.out.println("LOADING clojure.string.trim: " + trim.invoke(" hello "));

        IFn trimNewline = Clojure.var("clojure.string", "trim-newline");
        System.out.println("LOADING clojure.string.trim-newline: " + trimNewline.invoke("test\n\r"));

        IFn replaceFirst = Clojure.var("clojure.string", "replace-first");
        System.out.println("LOADING clojure.string.replace-first: " + replaceFirst.invoke("swap first two words", "first", "second"));

        IFn lastIndex = Clojure.var("clojure.string", "last-index-of");
        System.out.println("LOADING clojure.string.last-index-of: " + lastIndex.invoke("hello", "ell"));

        IFn leapYear = Clojure.var("clojure.instant", "leap-year?");
        System.out.println("LOADING clojure.instant.leap-year?" + leapYear.invoke(2001));

        IFn blank = Clojure.var("clojure.string", "blank?");
        System.out.println("LOADING clojure.string.blank?: " + blank.invoke(""));

        IFn odd = Clojure.var("clojure.core", "odd?");
        System.out.println("LOADING clojure.core.odd?: " + odd.invoke(2));

        IFn startsWith = Clojure.var("clojure.string", "starts-with?");
        System.out.println("LOADING clojure.core.starts-with?: " + startsWith.invoke("hello", "hell"));
    }
}
