package ru.spbau.mit.hw3;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by YuryKravchenko on 14/03/16.
 */
public class Client{

    Socket socket;
    DataInputStream in;
    DataOutputStream out;

    public Client() throws IOException {
        socket = new Socket("127.0.0.1", 12345);
        in = new DataInputStream(socket.getInputStream());
        out = new DataOutputStream(socket.getOutputStream());
    }

    public String List(String path) throws IOException {
        out.writeInt(1);
        out.writeUTF(path);
        int count = in.readInt();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; ++i) {
            builder.append(in.readUTF()).append(' ').append(in.readBoolean()).append('\n');
        }
        return builder.toString();
    }

    public String Get(String s) throws IOException {
        out.writeInt(2);
        out.writeUTF(s);
        Long size = in.readLong();
        byte[] file = new byte[size.intValue()];
        in.read(file, 0, size.intValue());
        return file.toString();
    }

}
