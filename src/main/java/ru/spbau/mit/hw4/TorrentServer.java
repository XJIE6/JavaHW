package ru.spbau.mit.hw4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TorrentServer implements Runnable{
    private static ServerSocket serverSocket;
    private static HashMap<Integer, Set<Sid>> newSids;
    private static HashMap<Integer, Set<Sid>> oldSids;
    private static ArrayList<PartableFile> files;

    @Override
    public void run() {
        while (true) {
            Thread thread = null;
            try {
                thread = new Thread(new Handler(serverSocket.accept()));
                thread.setDaemon(true);
                thread.start();
            } catch (IOException e) {
            }
        }
    }

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
            while (true) {
                int id;
                try {
                    switch (in.readByte()) {
                        case 1:
                            System.out.println("serv 1");
                            out.writeInt(files.size());
                            for (PartableFile file : files) {
                                out.writeInt(file.getId());
                                out.writeUTF(file.getName());
                                out.writeLong(file.getSize());
                            }
                            break;
                        case 2:
                            System.out.println("serv 2");
                            PartableFile file = new PartableFile(files.size(), in.readUTF(), in.readLong());
                            out.writeInt(files.size());
                            files.add(file);
                            break;
                        case 3:
                            synchronized (oldSids) {
                                System.out.println("serv 3");
                                id = in.readInt();
                                if (oldSids.get(id) == null) {
                                    out.writeInt(0);
                                    break;
                                }
                                out.writeInt(oldSids.get(id).size());
                                for (Sid sid : oldSids.get(id)) {
                                    for (byte b : sid.ip()) {
                                        out.writeByte(b);
                                    }
                                    out.writeInt(sid.getPort());
                                }
                            }
                            break;
                        case 4:
                            synchronized (newSids) {
                                System.out.println("serv 4");
                                int port = in.readInt();
                                Sid sid = new Sid(ip, port);
                                int count = in.readInt();
                                for (int i = 0; i < count; ++i) {
                                    id = in.readInt();
                                    if (newSids.get(id) == null) {
                                        newSids.put(id, new HashSet<>());
                                    }
                                    newSids.get(id).add(sid);
                                }
                                out.writeBoolean(true);
                                break;
                            }
                    }
                } catch (IOException e) {
                }
            }
        }
    };

    private void setClear() {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (oldSids) {
                    synchronized (newSids) {
                        oldSids = newSids;
                        newSids = new HashMap<>();
                    }
                }
            }
        }, 0, 3000);
    }

    void write(DataOutputStream out) throws IOException {
        out.writeInt(files.size());
        for (PartableFile file : files) {
            file.write(out);
        }
    }

    void read(DataInputStream in) throws IOException {
        int count = in.readInt();
        for (int i = 0; i < count; ++i) {
            files.add(new PartableFile(in));
        }
    }

    TorrentServer() {
        try {
            serverSocket = new ServerSocket(8081);
        } catch (IOException e) {
        }
        files = new ArrayList<>();
        newSids = new HashMap<>();
        oldSids = new HashMap<>();
        setClear();
    }
    public static void main(String[] args) {
        TorrentServer server = new TorrentServer();
        try {
            server.read(new DataInputStream(new FileInputStream("server.info")));
        } catch (IOException e) {
            System.out.println("can't read file");
        }
        Scanner in = new Scanner(System.in);
        while (true) {
            String[] command = in.nextLine().split(" ");
            if (command.length == 0) {
                System.out.println("wrong");
                continue;
            }
            switch (command[0]) {
                case "start":
                    new Thread(server).start();
                    break;
                case "exit":
                    try {
                        server.write(new DataOutputStream(new FileOutputStream("server.info", false)));
                    } catch (FileNotFoundException e) {
                        System.out.println("can't find file");
                    } catch (IOException e) {
                        System.out.println("can't write file");
                    }
                    System.exit(0);
                default:
                    System.out.println("wrong");
            }
        }
    }
}
