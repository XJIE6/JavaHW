package ru.spbau.mit.hw3;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by YuryKravchenko on 14/03/16.
 */
public class Server implements Runnable {

    ServerSocket socket;
    DataInputStream in;
    DataOutputStream out;

    public Server() throws IOException {
        socket = new ServerSocket(12345);
    }

    @Override
    public void run() {
        try {
            Socket socket = this.socket.accept();
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("connection error");
            return;
        }
        try {
            while (true) {
                int cmd = in.readInt();
                String path = in.readUTF();
                switch (cmd) {
                    case 1:
                        if (!new File(path).exists()) {
                            out.writeInt(0);
                            break;
                        }
                        out.writeInt(new File(path).listFiles().length);
                        for (File f : new File(path).listFiles()) {
                            out.writeUTF(f.getName());
                            out.writeBoolean(f.isDirectory());
                        }
                        out.flush();
                        break;
                    case 2:
                        if (!new File(path).exists() || new File(path).isDirectory()) {
                            out.writeInt(0);
                            break;
                        }
                        DataInputStream file = new DataInputStream(new FileInputStream(path));
                        Long length = new File(path).length();
                        out.writeLong(length);
                        byte[] b = new byte[length.intValue()];
                        file.read(b);
                        out.write(b);
                        out.flush();
                        break;
                }
            }
        } catch (IOException e) {
            System.out.println("connection failed");
            return;
        }
    }
}
