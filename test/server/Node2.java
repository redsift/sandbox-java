package server;

import io.redsift.ComputeRequest;
import io.redsift.ComputeResponse;

public class Node2 {
    public static ComputeResponse[] compute(ComputeRequest req) throws Exception {
        System.out.println("Node2.java: " + req.toString());
        ComputeResponse res = new ComputeResponse("bucket", "key", "value", 0);
        return new ComputeResponse[]{res};
    }
}
