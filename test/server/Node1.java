package server;

import io.redsift.ComputeRequest;
import io.redsift.ComputeResponse;
import java.nio.charset.StandardCharsets;

public class Node1 {
    public static ComputeResponse compute(ComputeRequest req) throws Exception {
        System.out.println("Node1.java: " + req.toString());
        System.out.println(req.get[0].data);
        System.out.println(req.get[0].data[0].key);
        System.out.println(new String(req.get[0].data[0].value, StandardCharsets.UTF_8));
        ComputeResponse res = new ComputeResponse("bucket", "key", "value", 0);
        return res;
    }

}
