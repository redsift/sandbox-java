package server;

import java.util.HashMap;
import java.util.Map;
import com.redsift.ComputeRequest;
import java.nio.charset.StandardCharsets;

public class Node1 {
    public static Map<String, Object> compute(ComputeRequest req) throws Exception {
        System.out.println("Node1.java" + req.toString());
        System.out.println(req.lookup[0].data.key);
        System.out.println(new String(req.lookup[0].data.value, StandardCharsets.UTF_8));
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", "bucket");
        ret.put("key", "key");
        ret.put("value", "value");
        return ret;
    }

}
