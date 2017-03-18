package com.kxw.nio.heartbeat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

/**
 * @author songfeng
 * @version 1.0
 * @category com.feng.test.longconnection
 * @since 2015-10-24
 * <a href='http://songfeng-123.iteye.com/blog/2251775'>@link</a>
 */
public class Client {
    private Socket socket;

    private String ip;

    private int port;

    private String id;

    DataOutputStream dos;

    DataInputStream dis;

    public Client(String ip, int port, String id) {
        try {
            this.ip = ip;
            this.port = port;
            this.id = id;
            this.socket = new Socket(ip, port);
            //this.socket.setKeepAlive(true);
            dos = new DataOutputStream(socket.getOutputStream());
            dis = new DataInputStream(socket.getInputStream());
            new Thread(new heartThread()).start();
            new Thread(new MsgThread()).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMsg(Object content) {
        try {
            int len = ObjectToByte(content).length;

            ByteBuffer dataLenBuf = ByteBuffer.allocate(4);
            dataLenBuf.order(ByteOrder.LITTLE_ENDIAN);
            dataLenBuf.putInt(0, len);
            dos.write(dataLenBuf.array(), 0, 4);
            dos.flush();
            dos.write(ObjectToByte(content));
            dos.flush();
        } catch (Exception e) {
            e.printStackTrace();
            closeSocket();
        }
    }

    public void closeSocket() {
        try {
            socket.close();
            dos.close();
            dis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static byte[] ObjectToByte(Object obj) {
        byte[] bytes = null;
        try {
            // object to bytearray
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);
            bytes = bo.toByteArray();
            bo.close();
            oo.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static Object ByteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // bytearray to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);
            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return obj;
    }

    class heartThread implements Runnable {
        @Override
        public void run() {
            while (true) {
                try {
                    Thread.sleep(1000);
                    long time = System.currentTimeMillis();
                    //System.out.println("client send:" + time);
                    sendMsg("Client" + id + "," + time);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class MsgThread implements Runnable {
        @Override
        public void run() {
            int temp;
            while (true) {
                try {
                    if (socket.getInputStream().available() > 0) {
                        byte[] bytes = new byte[1024];
                        int len = 0;
                        while ((char) (temp = dis.read()) != '\n') {
                            bytes[len] = (byte) temp;
                            len++;
                        }
                        System.out.println(ByteToObject(bytes));
                    }
                } catch (Exception e) {
                    closeSocket();
                }
            }
        }
    }

    public static void main(String[] args) {
        Client client1 = new Client("127.0.0.1", 55555, "1");
        client1.sendMsg(new Pojo("songfeng", 26, new ArrayList<String>()));
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Client client2 = new Client("127.0.0.1", 55555, "2");
    }
}