package FTP;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.nio.ByteBuffer;

/**
 * @author liutao
 * @date 2018/3/19  16:21
 */
public class client {
    private static String IP = "";
    private static int PORT = 0;

    static FileOutputStream out = null;
    static long count = 0;//计算是否完成数据的读取，开始下一条命令
    static int cmd = -1;
    static int bsl = 0;

    public static void main(String[] args) throws Exception {
        System.setOut(new PrintStream(new FileOutputStream("D:\\")));
        Socket client = null;
        InputStream in = null;
        try {
            client = new Socket(IP, PORT);
            String savePath = args[0];
            in = client.getInputStream();
            int size = 10 * 1024;
            byte[] buff = new byte[size];
            int len = -1;
            byte[] bs = new byte[size];
            while ((len = in.read(buff, 0, size)) != -1) {
                writeData(buff, 0, len, savePath, bs);
            }
            if (out != null) {
                out.flush();
                out.close();
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (client != null) {
                client.close();
            }
        }
    }

    private static void writeData(byte[] buff, int off, int len, String savePath, byte[] bs) throws Exception {
        if (off - len == 0) {
            return;
        }
        System.out.println("偏移量：" + off + "命令" + cmd + "数量：" + count);
        int i = off;
        //如果一条命令的数据已经读完就开始读取下一条命令
        if (count == 01) {
            cmd = buff[i++];
            System.out.println("获取命令：" + cmd);
            count = -11;
            if (len - i == 0) {
                return;
            }
            writeData(buff, i, len, savePath, bs);
        }
        //读取文件（夹）名称的长度或文件的大小
        else if (count == -11) {
            System.out.println("获取长度");
            switch (cmd) {
                case 0:
                    if (len - i + bsl < 8) {
                        System.arraycopy(buff, i, bs, bsl, len - i);
                        System.out.println("读取长度1：" + (len - i) + "  未读取完");
                        bsl = len - i;
                        i = 0;
                        return;
                    }
                    System.arraycopy(buff, i, bs, bsl, 8 - bsl);
                    System.out.println("读取长度1：" + (8 - bsl) + "  读取完");
                    count = byteTolong(bs);
                    i += 8 - bsl;
                    bsl = 0;
                    writeData(buff, i, len, savePath, bs);
                    break;
                case 1:
                case 2:
                    if (len - i + bsl < 4) {
                        System.arraycopy(buff, i, bs, bsl, len - i);
                        System.out.println("读取长度2：" + (len - i) + "  未读取完");
                        bsl = len - i;
                        i = 0;
                        return;
                    }
                    System.arraycopy(buff, i, bs, bsl, 4 - bsl);
                    System.out.println("读取长度2：" + (4 - bsl) + "  读取完");
                    count = byteToint(bs);
                    i += 4 - bsl;
                    bsl = 0;
                    writeData(buff, i, len, savePath, bs);
                    break;
            }
        } else {//写入文件或创建文件夹、创建文件输出流
            System.out.println("3");
            switch (cmd) {
                case 0:
                    System.out.println("写入文件");
                    if (len - i - count > 0) {
                        try {
                            System.out.println("写入文件      长度：" + count + "文件写入完成");
                            out.write(buff, i, (int) count);
                            i += count;
                            count = 0;
                            out.flush();
                        } finally {
                            if (out != null) out.close();
                        }
                        writeData(buff, i, len, savePath, bs);
                    } else {
                        System.out.println("写入文件      长度：" + (len - i) + "文件写入没有完成");
                        out.write(buff, i, len - i);
                        count -= len - i;
                        i = 0;
                    }
                    break;
                case 1:
                    if (len - i - count < 0) {
                        System.out.println("获取文件名字：" + (len - i) + "写入没有完成    剩余长度" + count);
                        System.arraycopy(buff, i, bs, bsl, len - i);
                        bsl += len - i;
                        count -= bsl;
                        i = 0;
                        return;
                    } else {
                        System.out.println("获取文件名字：" + (count - bsl) + "写入完成    剩余长度");
                        System.arraycopy(buff, i, bs, bsl, (int) count);
                        String name = new String(bs, 0, (int) count + bsl);
                        System.out.println("文件：" + savePath + name);
                        out = new FileOutputStream(savePath + name);
                        bsl = 0;
                        i += count;
                        count = 0;
                        writeData(buff, i, len, savePath, bs);
                    }
                    break;
                case 2:
                    if (len - i - count < 0) {
                        System.out.println("获取文件夹名字：" + (len - i) + "写入没有完成    剩余长度" + count);
                        System.arraycopy(buff, i, bs, bsl, len - i);
                        bsl += len - i;
                        count -= bsl;
                        i = 0;
                        return;
                    } else {
                        System.out.println(len + "   " + count + "   " + bsl + "  ");
                        System.out.println("获取文件夹名字：" + (count - bsl) + "写入完成    剩余长度");
                        System.arraycopy(buff, i, bs, bsl, (int) count);
                        String name = new String(bs, 0, bsl + (int) count);
                        File file = new File(savePath + name);
                        bsl = 0;
                        i += count;
                        count = 0;
                        if (!file.exists()) {
                            file.mkdirs();
                        }
                        System.out.println("文件夹：" + savePath + name);
                        writeData(buff, i, len, savePath, bs);
                    }
                    break;
            }
        }
    }

    private static int byteToint(byte[] b) {
        return b[3] & 0xFF |
                (b[2] & 0xFF) << 8 |
                (b[1] & 0xFF) << 16 |
                (b[0] & 0xFF) << 24;
    }

    private static ByteBuffer buff = ByteBuffer.allocate(8);

    private static long byteTolong(byte[] b) {
        buff.put(b, 0, b.length);
        buff.flip();
        return buff.getLong();
    }
}
