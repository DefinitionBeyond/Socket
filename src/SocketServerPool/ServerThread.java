package SocketServerPool;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

/**
 * @author liutao
 * @date 2018/3/19  16:08
 */
public class ServerThread implements Runnable {
    private Socket socket;

    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        PrintStream printStream = null;
        BufferedReader bufferedReader = null;
        try {
            //获取socket输出流
            printStream = new PrintStream(socket.getOutputStream());
            //获取socket输入流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            boolean flag = true;
            while (flag) {
                String data = bufferedReader.readLine();
                if (data == null || data.equals("")) {
                    flag = false;
                    System.out.println("接收不到正确数据，退出程序！");
                } else if (data.equalsIgnoreCase("exit")) {
                    flag = false;
                    System.out.println("正常关闭，退出程序！");
                } else
                    printStream.println(data);
            }
            bufferedReader.close();
            printStream.close();
            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
