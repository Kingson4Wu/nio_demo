package com.ibm.io.aio;

import java.io.IOException;
import java.util.concurrent.ExecutionException;

import org.junit.Test;


public class AIODemoTest {

    @Test
    public void testServer() throws IOException, InterruptedException {
        SimpleServer server = new SimpleServer(9021);
        Thread.sleep(10000);//由于是异步操作，所以睡眠一定时间，以免程序很快结束
    }

    @Test
    public void testClient() throws IOException, InterruptedException, ExecutionException {
        SimpleClientClass client = new SimpleClientClass("localhost", 9021);
        client.write((byte) 11);
    }

    public static void main(String[] args) {
        AIODemoTest demoTest = new AIODemoTest();
        try {
            demoTest.testServer();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            demoTest.testClient();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }

}