package ru.spbau.mit.hw4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.ServerSocket;
import java.net.Socket;

class ClientSid implements Runnable {
    private ServerSocket serverSocket;
    private class Handler implements Runnable{
        DataInputStream in;
        DataOutputStream out;
        public Handler(Socket connection) throws IOException {
            in = new DataInputStream(connection.getInputStream());
            out = new DataOutputStream(connection.getOutputStream());
        }

        @Override
        public void run() {
            try {
                int id;
                switch(in.readByte()) {
                    case 1:
                        id = in.readInt();
                        for (File file : TorrentClient.getFiles()) {
                            if (file.getId() == id) {
                                out.writeInt(file.getParts().size());
                                for (int number : file.getParts()) {
                                    out.writeInt(number);
                                }
                                break;
                            }
                        }
                        break;
                    case 2:
                        id = in.readInt();
                        int part = in.readInt();
                        File file = null;
                        for(File f : TorrentClient.getFiles()) {
                            if (f.getId() == id) {
                                file = f;
                                break;
                            }
                        }
                        RandomAccessFile f = new RandomAccessFile(file.getName(), "rw");
                        byte[] buffer = new byte[1 << 20];
                        int len = f.read(buffer, (1 << 20) * part, (1 << 20));
                        out.write(buffer);
                        break;
                }
            } catch (IOException e) {
            }
        }
    }
    public ClientSid() throws IOException {
        serverSocket = new ServerSocket(0);
    }
    public int getPort() {
        return serverSocket.getLocalPort();
    }
    @Override
    public void run() {
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
