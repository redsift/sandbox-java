package com.redsift;

import nanomsg.reqrep.ReqSocket;

/**
 * Created by deepakp on 18/03/2016.
 */
public class Test {
    public static void main(String[] args) throws Exception {
        for (String n : args) {
            final ReqSocket socket = new ReqSocket();
            String addr = "ipc://" + "/tmp" + "/" + n + ".sock";
            socket.setRecvTimeout(-1);
            socket.setSendTimeout(-1);

            socket.bind(addr);

            System.out.println("Sending to " + addr);
            socket.send("hello");

            String res = socket.recvString();
            System.out.println("Received " + res);
            socket.close();
        }

        //Thread.currentThread().sleep(1000000);
    }
}
