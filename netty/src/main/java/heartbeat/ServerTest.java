package heartbeat;


public class ServerTest {

    public static void main(String[] args) throws Exception {
        new NettyTCPServer(9999).start();
    }
}
