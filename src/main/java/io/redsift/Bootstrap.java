package io.redsift;

import nanomsg.Nanomsg;
import nanomsg.reqrep.RepSocket;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        this.socket.setSocketOpt(Nanomsg.SocketOption.NN_RCVMAXSIZE, -1);

        this.socket.setSocketOpt(Nanomsg.SocketOption.NN_RCVTIMEO, -1);
        this.socket.setSocketOpt(Nanomsg.SocketOption.NN_SNDTIMEO, -1);

        this.socket.connect(this.addr);
        System.out.println("Connected to " + this.addr);

        try {
            while (true) {
                byte[] req = this.socket.recvBytes();
                ComputeRequest computeReq = Protocol.fromEncodedMessage(req);
                //System.out.println("Received " + computeReq.toString());
                long start = System.nanoTime();
                Object ret = this.compute.invoke(null, computeReq);
                long end = System.nanoTime();
                double t = (end - start) / Math.pow(10, 9);
                double[] diff = new double[2];
                diff[0] = Math.floor(t);
                diff[1] = (t - diff[0]) * Math.pow(10, 9);
                //System.out.println("diff=" + t + " " + diff[0] + " " + diff[1]);
                this.socket.send(Protocol.toEncodedMessage(ret, diff));
            }
        } catch (Exception e) {
            Throwable cause = e.getCause();
            if (cause == null) {
                cause = e;
            }
            StringWriter stackWriter = new StringWriter();
            cause.printStackTrace(new PrintWriter(stackWriter));

            System.out.println("Thread " + threadName + " interrupted." + cause);
            cause.printStackTrace();
            String message = cause.toString();
            try {
                byte[] errBytes = Protocol.toErrorBytes(message, stackWriter.toString());
                this.socket.send(errBytes);
            } catch (Exception e1) {
                // do nothing
                System.out.println("Error sending error, how meta! " + e1);
            }
        }
        System.out.println("Thread " + threadName + " exiting.");
        this.socket.close();
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

            List<Thread> threads = new ArrayList<Thread>();
            List<RepSocket> sockets = new ArrayList<RepSocket>();

            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();

            for (String n : args) {
                int i = Integer.parseInt(n);
                System.out.println("");
                //System.out.println("n: " + n + " i: " + i);
                SiftJSON.Dag.Node node = init.sift.dag.nodes[i];

                if (node.implementation == null || (node.implementation.java == null &&
                        node.implementation.scala == null && node.implementation.clojure == null)) {
                    throw new Exception("Requested to install a non-Java, non-Scala or non-Clojure node at index " + n);
                }

                SiftJSON.Dag.Node.Implementation.ImplFile implFile = node.implementation.implFile();

                System.out.println("Running node: " + node.description + " : " + implFile.file);

                if (!implFile.file.contains(".jar")) {
                    throw new Exception("Node not installed, bailing out!");
                }

                File jarFile = new File(init.SIFT_ROOT, implFile.file);
                URL[] jars = new URL[]{jarFile.toURI().toURL()};
                if (node.implementation.clojure != null) {
                    File clojureJARFile = new File(Init.clojureJARPath());
                    URL clojureJARURL = clojureJARFile.toURI().toURL();
                    jars = new URL[]{jarFile.toURI().toURL(), clojureJARURL};
                }

                ClassLoader classloader = new URLClassLoader(jars,
                        //ClassLoader.getSystemClassLoader().getParent()); NOTE: If this is used we'll end up with a mismatch below.
                        currentClassLoader);

                Class nodeClass = classloader.loadClass(implFile.className);

                Method compute = null;

                if (implFile.impl.equals("clj")) {
                    compute = nodeClass.getMethod("invokeStatic", Object.class);
                } else {
                    compute = nodeClass.getMethod("compute", ComputeRequest.class);
                }

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
            System.err.println("Error running bootstrap: " + ex.toString());
            ex.printStackTrace();
            System.exit(1);
        }
    }

}
