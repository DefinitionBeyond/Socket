package SocketServerPool;

import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author liutao
 * @date 2018/3/19  16:03
 */
public class Server {
    public static void main(String[] args) {
        ServerSocket serverSocket = null;
        Socket socket = null;
        Socket socket1 = null;
        try {
            serverSocket = new ServerSocket(12345);//设置服务器发送信息端口
            boolean flag = true;
            while (flag) {
                //服务端和客户端建立连接
                socket = serverSocket.accept();
                System.out.println("成功建立连接！");
                new Thread(new ServerThread(socket)).start();

                //服务端和客户端建立连接
                socket1 = serverSocket.accept();
                System.out.println("成功建立连接！");
                new Thread(new ServerThread(socket1)).start();

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
