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
            } else if (valObj.getClass().isArray() || valObj.getClass().equals(Map.class)) {
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

        } else if (data.getClass().equals(Map.class)) {
            out.add(Protocol.b64Encode((Map<String, Object>) data));
        } else if (data.getClass().equals(List.class)) {
            for (Map<String, Object> m : (List<Map<String, Object>>) data) {
                out.add(Protocol.b64Encode(m));
            }
        } else {
            throw new Exception("node implementation has to return Map<String, Object>, List<Map<String, Object>> or null");
        }

        Map<String, Object> m = new HashMap<String, Object>();
        m.put("out", out);
        m.put("result", diff);
        return Init.mapper.writeValueAsBytes(m);
    }

    public static Map<String, Object> fromEncodedMessage(byte[] bytes) throws Exception {
        Map<String, Object> m = Init.mapper.readValue(bytes, Map.class);

        System.out.println("fromEncodedMessage" + m.toString());

        Object inObj = m.get("in");
        Object withObj = m.get("with");
        List<Object> inWith = new ArrayList<Object>();
        if (inObj != null) {
            inWith.add(inObj);
        }
        if (withObj != null) {
            inWith.add(withObj);
        }
        for (Object obj : inWith) {
            Object dataObj = ((Map<String, Object>) obj).get("data");
            if (dataObj != null) {
                List<Map<String, Object>> data = (List<Map<String, Object>>) dataObj;
                for (Map<String, Object> dataItem : data) {
                    Protocol.b64Decode(dataItem);
                }
            }
        }
        Object lookupObj = m.get("lookup");
        if (lookupObj != null) {
            List<Map<String, Object>> lookup = (List<Map<String, Object>>) lookupObj;
            for (Map<String, Object> lookupItem : lookup) {
                Object dataObj = lookupItem.get("data");
                if (dataObj != null) {
                    Map<String, Object> data = (Map<String, Object>) dataObj;
                    Protocol.b64Decode(data);
                }
            }
        }
        return m;
    }
}
