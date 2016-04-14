package com.redsift;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

public class Protocol {
    public static ComputeResponse encodeValue(ComputeResponse data) throws Exception {
        Object valObj = data.value;
        if (valObj != null) {
            if (valObj.getClass().equals(String.class)) {
                data.value = ((String) valObj).getBytes();
            } else if (valObj instanceof byte[]) { // no-op
            } else {
                try {
                    data.value = Init.mapper.writeValueAsBytes(valObj);
                } catch (Exception e) {
                    throw new Exception("unsupported data type");
                }
            }
        }
        return data;
    }

    public static byte[] toEncodedMessage(Object data, double[] diff) throws Exception {
        //System.out.println("data=" + data + " " + data.getClass());
        List<ComputeResponse> out = new ArrayList<ComputeResponse>();
        if (data == null) {

        } else if (data.getClass().equals(ComputeResponse.class)) {
            out.add(Protocol.encodeValue((ComputeResponse) data));
        } else if (data instanceof ComputeResponse[]) {
            for (ComputeResponse d : (ComputeResponse[]) data) {
                out.add(Protocol.encodeValue(d));
            }
        } else if (data.getClass().equals(ArrayList.class)) {
            for (ComputeResponse d : (List<ComputeResponse>) data) {
                out.add(Protocol.encodeValue(d));
            }
        } else {
            throw new Exception("node implementation has to return ComputeResponse, ComputeResponse[], List<ComputeResponse> or null");
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
        //System.out.println("fromEncodedMessage: " + computeReq.toString());

        return computeReq;
    }
}
