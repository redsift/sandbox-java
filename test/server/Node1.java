package server;

import java.util.HashMap;
import java.util.Map;

public class Node1 {
    public static Map<String, Object> compute(Map<String, Object> got) throws Exception {
        System.out.println("Node1.java" + got.toString());
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", "bucket");
        ret.put("key", "key");
        ret.put("value", "value");
        return ret;
    }

}
