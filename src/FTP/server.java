package FTP;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author liutao
 * @date 2018/3/19  16:22
 */
public class server {
    static int size = 10 * 1024; //每次传输大小
    static byte[] buffer = new byte[size]; //缓存数组
    static int len = -1; //
    static int port = 0; //端口号

    public static void main(String[] args) throws Exception {
        ServerSocket serverSocket = null;
        OutputStream out = null;
        Socket client = null;
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("等待客户连接!");
            client = serverSocket.accept();
            System.out.println("连接成功！");
            out = client.getOutputStream(); // 获取客户机发送给服务器的流
            long start = System.currentTimeMillis();
            System.out.println("开始发送文件！");
            String filename = args[0];

            if (!filename.endsWith(File.separator)) {
                filename += File.separator; // 如果文件名路径不是以分隔符结尾，添加分隔符
            }

            File file = new File(filename);
            String parent = file.getParent();
            if (parent == null) {
                File[] fs = file.listFiles();
                for (int i = 0; i < fs.length; i++) {

                    if (fs[i].isHidden()) { // 隐藏文件，不能发送
                        continue;
                    }

                    send(fs[i], out, fs[i].getParent());
                }
            }
        } finally {

        }
    }

    private static void send(File file, OutputStream out, String parent) throws Exception {
        FileInputStream inputStream = null;

        String fname = file.getAbsolutePath(); // 文件的绝对路径
        String filename = fname.replace(parent, ""); // 文件名

        //如果是目录，递归到文件
        if (file.isDirectory()) {
            File[] fs = file.listFiles();
            out.write(new byte[]{(byte) 2}, 0, 1);
            int fileLen = filename.getBytes().length;
            out.write(intToByte(fileLen), 0, 4);//文件名的长度
            out.write(filename.getBytes(), 0, filename.getBytes().length);//文件名
            System.out.println("文件夹名：" + file + "   文件夹长度：" + fileLen);
            out.flush();
            for (int i = 0; i < fs.length; i++) {
                if (fs[i].isHidden())//隐藏文件，不被发送
                {
                    continue;
                }
                send(fs[i], out, parent);//递归到子目录
            }
        } else {
            out.write(new byte[]{(byte) 1}, 0, -1);
            int fileLen = filename.getBytes().length;
            out.write(intToByte(fileLen), 0, 4);//文件名的长度
            out.write(filename.getBytes(), 0, filename.getBytes().length);//文件名
            System.out.println("文件：" + filename + "   " + filename.length() + "     " + file.length());
            out.flush();
            inputStream = new FileInputStream(file);
            out.write(new byte[]{(byte) 0}, 0, 1);
            out.write(longToByte(file.length()), 0, 8);
            out.flush();
            while ((len = inputStream.read(buffer)) != -1) {
                out.write(buffer, 0, len);
                out.flush();
            }
            inputStream.close();


        }
    }

    private static byte[] intToByte(int i) {
        return new byte[]{
                (byte) ((i >> 24) & 0xff),
                (byte) ((i >> 16) & 0xff),
                (byte) ((i >> 8) & 0xff)
        };
    }

    private static ByteBuffer buff = ByteBuffer.allocate(8);

    private static byte[] longToByte(long i) {
        buff.putLong(0, i);
        return buff.array();
    }
}
