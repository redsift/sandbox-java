package server.inner;

import java.util.ArrayList;
import java.util.List;
import com.redsift.ComputeRequest;
import com.redsift.ComputeResponse;

public class Node2 {
    public static List<ComputeResponse> compute(ComputeRequest req) throws Exception {
        System.out.println("Inner Node2.java" + req.toString());
        List<ComputeResponse> ret = new ArrayList<ComputeResponse>();
        ComputeResponse res = new ComputeResponse("bucket", "key", "value", 0);
        ret.add(res);
        ComputeResponse res1 = new ComputeResponse("bucket1", "key1", "value1", 0);
        ret.add(res1);
        return ret;
    }
}
