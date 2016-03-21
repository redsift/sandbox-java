package com.redsift;

import nanomsg.reqrep.RepSocket;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

class NodeThread extends Thread {
    public RepSocket socket;
    private Thread t;
    private String threadName;
    private Method compute;
    private String addr;

    NodeThread(String name, String addr, Method compute) {
        this.threadName = name;
        this.addr = addr;
        this.compute = compute;
        System.out.println("Creating " + threadName);
    }

    public void run() {
        System.out.println("Running " + threadName);
        this.socket = new RepSocket();
        this.socket.setRecvTimeout(-1);
        this.socket.setSendTimeout(-1);

        this.socket.connect(this.addr);
        System.out.println("Connected to " + this.addr);

        try {
            while (true) {
                byte[] req = socket.recvBytes();
                ComputeRequest computeReq = Protocol.fromEncodedMessage(req);
                System.out.println("Received " + computeReq.toString());
                long start = System.nanoTime();
                Object ret = compute.invoke(null, computeReq);
                long end = System.nanoTime();
                double t = (end - start) / Math.pow(10, 9);
                double[] diff = new double[2];
                diff[0] = Math.floor(t);
                diff[1] = (t - diff[0]) * Math.pow(10, 9);
                //System.out.println("diff=" + t + " " + diff[0] + " " + diff[1]);
                socket.send(Protocol.toEncodedMessage(ret, diff));
            }
        } catch (Exception e) {
            System.out.println("Thread " + threadName + " interrupted." + e);
            e.printStackTrace();
        }
        System.out.println("Thread " + threadName + " exiting.");
    }

    public void start() {
        System.out.println("Starting " + threadName);
        if (t == null) {
            t = new Thread(this, threadName);
            t.start();
        }
    }

}

public class Bootstrap {

    public static void main(String args[]) {

        try {
            System.out.println("Bootstrap: " + Arrays.toString(args));

            Init init = new Init(args);
            File selfJARFile = new File(init.selfJARPath());
            URL selfJARURL = selfJARFile.toURI().toURL();

            List<Thread> threads = new ArrayList<Thread>();
            List<RepSocket> sockets = new ArrayList<RepSocket>();

            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

            for (String n : args) {
                int i = Integer.parseInt(n);
                System.out.println("");
                //System.out.println("n: " + n + " i: " + i);
                SiftJSON.Dag.Node node = init.sift.dag.nodes[i];

                if (node.implementation == null || node.implementation.java == null) {
                    throw new Exception("Requested to run a non-Java node at index " + n);
                }

                System.out.println("Running node: " + node.description + " : " + node.implementation.java);
                SiftJSON.Dag.Node.Implementation.JavaFile javaFile = node.implementation.javaFile();
                if (!javaFile.file.contains(".jar")) {
                    throw new Exception("Node not installed, bailing out!");
                }

                File jarFile = new File(init.SIFT_ROOT, javaFile.file);
                ClassLoader classloader = new URLClassLoader(new URL[]{jarFile.toURI().toURL(), selfJARURL},
                        //ClassLoader.getSystemClassLoader().getParent()); NOTE: If this is used we'll end up with a mismatch below.
                        currentClassLoader);

                Class nodeClass = classloader.loadClass(javaFile.className);

                Method compute = nodeClass.getMethod("compute", ComputeRequest.class);

                if (init.DRY) {
                    return;
                }

                String addr = "ipc://" + init.IPC_ROOT + "/" + n + ".sock";

                NodeThread nodeThread = new NodeThread(n, addr, compute);
                nodeThread.setContextClassLoader(classloader);
                threads.add(nodeThread);
                sockets.add(nodeThread.socket);
                nodeThread.start();
            }

            for (Thread thread : threads) {
                thread.join();
                // If any thread exits then something went wrong. Bail out.
                System.out.println("Node thread exited!");
                //System.exit(1);
            }

            for (RepSocket socket : sockets) {
                //socket.close();
            }
        } catch (Exception ex) {
            System.out.println("Error running bootstrap: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
