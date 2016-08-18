package com.kxw.nio.heartbeat;

import org.apache.commons.lang3.ArrayUtils;

import java.io.*;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author songfeng
 * @version 1.0
 * @category com.feng.test.longconnection
 * @since 2015-10-24
 * <a href='http://songfeng-123.iteye.com/blog/2251775'>@link</a>
 */
public class Server {
    private Map<String, Long> heatTimeMap = new HashMap<String, Long>();

    public Server(int port) {
        Selector selector = null;
        ServerSocketChannel serverChannel = null;
        try {
            //获取一个ServerSocket通道
            serverChannel = ServerSocketChannel.open();
            serverChannel.configureBlocking(false);
            serverChannel.socket().bind(new InetSocketAddress(port));
            //获取通道管理器
            selector = Selector.open();
            //将通道管理器与通道绑定，并为该通道注册SelectionKey.OP_ACCEPT事件，
            //只有当该事件到达时，Selector.select()会返回，否则一直阻塞。
            serverChannel.register(selector, SelectionKey.OP_ACCEPT);

            while (selector.select() > 0) {
                //选择注册过的io操作的事件
                Iterator<SelectionKey> it = selector.selectedKeys().iterator();
                while (it.hasNext()) {
                    SelectionKey readyKey = it.next();
                    //删除已选key，防止重复处理
                    it.remove();
                    if (readyKey.isAcceptable()) {
                        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) readyKey.channel();
                        SocketChannel socketChannel = serverSocketChannel.accept();

                        socketChannel.configureBlocking(false);
                        // 连接成功后，注册接收服务器消息的事件
                        socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                    } else if (readyKey.isReadable()) {
                        SocketChannel socketChannel = (SocketChannel) readyKey.channel();

                        Object obj = receiveData(socketChannel);
                        String msg = "Server back:";
                        if (obj instanceof String) {
                            String id = obj.toString().split(",")[0];
                            if (heatTimeMap.get(id) != null
                                    && System.currentTimeMillis() - heatTimeMap.get(id) > 5000) {
                                socketChannel.socket().close();
                            } else {
                                heatTimeMap.put(id, System.currentTimeMillis());
                            }
                            long time = System.currentTimeMillis();
                            msg += time + "\n";
                            sendData(socketChannel, msg);
                        } else if (obj instanceof Pojo) {
                            msg += ((Pojo) obj).getName() + "\n";
                            sendData(socketChannel, msg);
                        }

                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                selector.close();
                if (serverChannel != null) {
                    serverChannel.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Object receiveData(SocketChannel socketChannel) {
        Object obj = null;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteBuffer intBuffer = ByteBuffer.allocate(4);
        ByteBuffer objBuffer = ByteBuffer.allocate(1024);
        int size = 0;
        int sum = 0;
        int objlen = 0;
        byte[] bytes = null;
        try {
            while ((size = socketChannel.read(intBuffer)) > 0) {
                intBuffer.flip();
                bytes = new byte[size];
                intBuffer.get(bytes);
                baos.write(bytes);
                intBuffer.clear();
                if (bytes.length == 4) {
                    objlen = bytesToInt(bytes, 0);
                }
                if (objlen > 0) {
                    byte[] objByte = new byte[0];
                    while (sum != objlen) {
                        size = socketChannel.read(objBuffer);
                        if (size > 0) {
                            objBuffer.flip();
                            bytes = new byte[size];
                            objBuffer.get(bytes, 0, size);
                            baos.write(bytes);
                            objBuffer.clear();
                            objByte = ArrayUtils.addAll(objByte, bytes);
                            sum += bytes.length;
                        }
                    }
                    obj = ByteToObject(objByte);
                    break;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return obj;
    }

    private static void sendData(SocketChannel socketChannel, Object obj) {
        byte[] bytes = ObjectToByte(obj);

        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        try {
            socketChannel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * byte数组中取int数值，本方法适用于(低位在前，高位在后)的顺序。
     *
     * @param ary    byte数组
     * @param offset 从数组的第offset位开始
     * @return int数值
     */
    public static int bytesToInt(byte[] ary, int offset) {
        int value;
        value = (int) ((ary[offset] & 0xFF)
                | ((ary[offset + 1] << 8) & 0xFF00)
                | ((ary[offset + 2] << 16) & 0xFF0000)
                | ((ary[offset + 3] << 24) & 0xFF000000));
        return value;
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
            //e.printStackTrace();
        }
        return obj;
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

    public static void main(String[] args) {
        Server server = new Server(55555);
    }
}