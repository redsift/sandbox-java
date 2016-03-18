package serverplain;

import java.util.HashMap;
import java.util.Map;

public class Node3 {

    public static Map<String, Object> compute(Map<String, Object> got) throws Exception {
        //Thread.currentThread().sleep(40000);
        System.out.println("serverplain Node3.java" + got.toString());
        Map<String, Object> ret = new HashMap<String, Object>();
        ret.put("name", "bucket");
        ret.put("key", "key");
        ret.put("value", "value");
        return ret;
    }

}
