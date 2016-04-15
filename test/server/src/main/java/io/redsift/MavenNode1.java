package io.redsift;

import io.redsift.ComputeRequest;
import io.redsift.ComputeResponse;

public class MavenNode1 {
    public static ComputeResponse compute(ComputeRequest req) throws Exception {
        System.out.println("MavenNode1.java: " + req.toString());
        ComputeResponse res = new ComputeResponse("bucket-maven", "key-maven", "value-maven", 0);
        return res;
    }
}
