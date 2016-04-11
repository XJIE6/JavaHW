package ru.spbau.mit.hw4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class TorrentServer {
    private static ServerSocket serverSocket;
    private static HashMap<Integer, Set<Sid>> sids;
    private static ArrayList<File> files;
    private static class Handler implements Runnable{
        DataInputStream in;
        DataOutputStream out;
        byte[] ip;
        public Handler(Socket connection) throws IOException {
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());
            ip = connection.getInetAddress().getAddress();
        }
        @Override
        public void run() {
            try {
                switch(in.readByte()) {
                    case 1:
                        out.writeInt(files.size());
                        for (File file : files) {
                            out.writeInt(file.getId());
                            out.writeUTF(file.getName());
                            out.writeLong(file.getSize());
                        }
                        break;
                    case 2:
                        File file = new File(files.size(), in.readUTF(), in.readLong());
                        files.add(file);
                        break;
                    case 3:
                        int id = in.readInt();
                        out.writeInt(sids.get(id).size());
                        for (Sid sid : sids.get(id)) {
                            for (byte b : sid.ip()) {
                                out.writeByte(b);
                            }
                            out.writeShort(sid.getPort());
                        }
                        break;
                    case 4:
                        Sid sid = new Sid(ip, in.readShort());
                        int count = in.readInt();
                        for (int i = 0; i < count; ++i) {
                            sids.get(in.readInt()).add(sid);
                        }
                        break;
                }
            } catch (IOException e) {
            }
        }
    };
    public static void main(String[] args) {
        try {
            serverSocket = new ServerSocket(8081);
        } catch (IOException e) {
        }
        while (true) {
            try {
                Thread thread = new Thread(new Handler(serverSocket.accept()));
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
            }
        }
    }
}
