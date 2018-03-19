package SocketServerPool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author liutao
 * @date 2018/3/19  16:19
 */
public class client {
    public static void main(String[] args) throws Exception {
        try {
            //客户端请求在12345号端口请求连接
            Socket socket = new Socket("10.10.41.110", 12345);
            //socket.setSoTimeout(10000);

            //获取键盘读入数据
            BufferedReader input = new BufferedReader(new InputStreamReader(System.in));

            //获取socket的输出流，发送数据给服务端
            PrintStream out = new PrintStream(socket.getOutputStream());
            //获取socket的输入流，作为接收数据
            BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            boolean falg = true;
            while (falg) {
                String data = input.readLine();
                if (data.equalsIgnoreCase("exit")) falg = false;
                else
                    System.out.println("arrival:" + data);
            }
            input.close();
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
