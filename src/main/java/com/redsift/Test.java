package com.redsift;

import nanomsg.reqrep.ReqSocket;
import org.adrianwalker.multilinestring.Multiline;

/**
 * Created by deepakp on 18/03/2016.
 */

public class Test {
    /**
{
  "in": {
    "data": [
      {
        "key": "asdf-in",
        "value": "SGVsbG8=",
        "epoch": 0
      }
    ]
  },
  "with": {
    "data": [
      {
        "key": "asdf-with",
        "value": "SGVsbG8=",
        "epoch": 0
      }
    ]
  },
  "lookup": [
    {
      "data": {
        "key": "adks-lookup",
        "value": "SGVsbG8="
      }
    }
  ]
}
 */
    @Multiline
    private static String json;

    public static void main(String[] args) throws Exception {
        for (String n : args) {
            final ReqSocket socket = new ReqSocket();
            String addr = "ipc://" + "/tmp" + "/" + n + ".sock";
            socket.setRecvTimeout(-1);
            socket.setSendTimeout(-1);

            socket.bind(addr);

            System.out.println("Sending to " + addr);
            socket.send(Test.json);

            String res = socket.recvString();
            System.out.println("Received " + res);
            socket.close();
        }

        //Thread.currentThread().sleep(1000000);
    }
}
