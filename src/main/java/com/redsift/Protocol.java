package com.redsift;

import java.util.*;

public class Protocol {
    public static void b64Decode(Map<String, Object> data) {
        Object valObj = data.get("value");
        if (valObj != null) {
            data.put("value", Base64.getDecoder().decode((String) valObj));
        }
    }

    public static Map<String, Object> b64Encode(Map<String, Object> data) throws Exception {
        Object valObj = data.get("value");
        if (valObj != null) {
            if (valObj.getClass().equals(String.class)) {
                data.put("value", Base64.getEncoder().encodeToString(((String) valObj).getBytes("utf-8")));
            } else if (valObj instanceof byte[]) {
                data.put("value", Base64.getEncoder().encodeToString((byte[]) valObj));
            } else if (valObj.getClass().equals(ArrayList.class) || valObj.getClass().equals(HashMap.class)) {
                data.put("value", Base64.getEncoder().encodeToString(Init.mapper.writeValueAsString(valObj).getBytes("utf-8")));
            } else {
                throw new Exception("unsupported data type");
            }
        }
        return data;
    }

    public static byte[] toEncodedMessage(Object data, double[] diff) throws Exception {
        List<Object> out = new ArrayList<Object>();
        if (data == null) {

        } else if (data.getClass().equals(HashMap.class)) {
            out.add(Protocol.b64Encode((HashMap<String, Object>) data));
        } else if (data.getClass().equals(ArrayList.class)) {
            for (Map<String, Object> m : (List<Map<String, Object>>) data) {
                out.add(Protocol.b64Encode(m));
            }
        } else {
            throw new Exception("node implementation has to return Map<String, Object>, List<Map<String, Object>> or null");
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("out", out);
        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("result", diff);
        m.put("stats", stats);
        return Init.mapper.writeValueAsBytes(m);
    }

    public static ComputeRequest fromEncodedMessage(byte[] bytes) throws Exception {
        ComputeRequest computeReq = Init.mapper.readValue(bytes, ComputeRequest.class);
        System.out.println("fromEncodedMessage: " + computeReq.toString());

        return computeReq;
    }
}
