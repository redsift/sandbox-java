package com.redsift;

import com.redsift.ComputeRequest;
import com.redsift.ComputeResponse;

public class MavenNode1 {
    public static ComputeResponse compute(ComputeRequest req) throws Exception {
        System.out.println("MavenNode1.java: " + req.toString());
        ComputeResponse res = new ComputeResponse("bucket-maven", "key-maven", "value-maven", 0);
        return res;
    }
}
