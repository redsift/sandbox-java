package server;

import com.redsift.ComputeRequest;
import com.redsift.ComputeResponse;
import java.nio.charset.StandardCharsets;

public class Node1 {
    public static ComputeResponse compute(ComputeRequest req) throws Exception {
        System.out.println("Node1.java" + req.toString());
        System.out.println(req.lookup[0].data.key);
        System.out.println(new String(req.lookup[0].data.value, StandardCharsets.UTF_8));
        ComputeResponse res = new ComputeResponse("bucket", "key", "value", 0);
        return res;
    }

}
