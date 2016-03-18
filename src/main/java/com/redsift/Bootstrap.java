package com.redsift;

import nanomsg.reqrep.RepSocket;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class NodeThread extends Thread {
    private Thread t;
    private String threadName;
    private Method compute;
    private RepSocket socket;

    NodeThread(String name, RepSocket socket, Method compute) {
        this.threadName = name;
        this.socket = socket;
        this.compute = compute;
        System.out.println("Creating " + threadName);
    }

    public void run() {
        System.out.println("Running " + threadName);
        try {
            while (true) {
                String req = socket.recvString();
                System.out.println("Received " + req);
                Class<?> retType = compute.getReturnType();
                long start = System.nanoTime();
                Object ret = compute.invoke(null);
                long end = System.nanoTime();
                double t = (end - start) / Math.pow(10, 9);
                double[] diff = new double[2];
                diff[0] = Math.floor(t);
                diff[1] = (t - diff[0]) * Math.pow(10, 9);
                System.out.println("diff=" + t + " " + diff[0] + " " + diff[1]);
                socket.send("hi");
            }
        } catch (Exception e) {
            System.out.println("Thread " + threadName + " interrupted." + e);
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

    public static void main(String args[]) throws Exception {

        System.out.println("Bootstrap: " + Arrays.toString(args));

        Init init = new Init(args);

        List<Thread> threads = new ArrayList<Thread>();
        List<RepSocket> sockets = new ArrayList<RepSocket>();

        for (String n : args) {
            int i = Integer.parseInt(n);
            System.out.println("");
            System.out.println("n: " + n + " i: " + i);
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
            ClassLoader classloader = new URLClassLoader(new URL[]{jarFile.toURI().toURL()},
                    ClassLoader.getSystemClassLoader().getParent());

            Class mainClass = classloader.loadClass(javaFile.className);
            @SuppressWarnings("unchecked")
            Method compute = mainClass.getMethod("compute", (Class[]) null);

            final RepSocket socket = new RepSocket();
            socket.setRecvTimeout(-1);
            socket.setSendTimeout(-1);

            String addr = "ipc://" + init.IPC_ROOT + "/" + n + ".sock";
            socket.connect(addr);
            System.out.println("Connected to " + addr);

            NodeThread nodeThread = new NodeThread(n, socket, compute);
            nodeThread.setContextClassLoader(classloader);
            threads.add(nodeThread);
            sockets.add(socket);
            nodeThread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        for (RepSocket socket : sockets) {
            socket.close();
        }
    }

}
