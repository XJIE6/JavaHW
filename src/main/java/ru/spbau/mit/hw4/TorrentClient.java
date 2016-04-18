package ru.spbau.mit.hw4;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class TorrentClient {
    private Sid server;
    private ClientSid client;
    private Socket serverConnection;
    private DataInputStream in;
    private DataOutputStream out;
    ArrayList<PartableFile> files;

    private class ClientSid implements Runnable {
        private ServerSocket serverSocket;

        @Override
        public void run() {
            while (true) {
                Thread thread = null;
                try {
                    thread = new Thread(new Handler(serverSocket.accept()));
                    thread.setDaemon(true);
                    thread.start();
                    System.out.println("new connection");
                } catch (IOException e) {
                }
            }
        }

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
                            System.out.println("sid 1");
                            id = in.readInt();
                            for (PartableFile file : files) {
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
                            System.out.println("sid 2");
                            id = in.readInt();
                            int part = in.readInt();
                            System.out.println("part");
                            System.out.println(part);
                            PartableFile file = null;
                            for(PartableFile f : files) {
                                if (f.getId() == id) {
                                    file = f;
                                    break;
                                }
                            }
                            RandomAccessFile f = new RandomAccessFile(file.getFile(), "rw");
                            byte[] buffer = new byte[1 << 10];
                            f.seek((1 << 10) * part);
                            int len = f.read(buffer, 0, (1 << 10));
                            System.out.println("len");
                            System.out.println(len);
                            out.write(buffer, 0, len);
                            break;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        public ClientSid() throws IOException {
            serverSocket = new ServerSocket(0);
        }
        public int getPort() {
            return serverSocket.getLocalPort();
        }
    }

    private Iterable<PartableFile> list() throws IOException {
        synchronized (serverConnection) {
            ArrayList<PartableFile> result = new ArrayList<>();
            out.writeByte(1);
            int count = in.readInt();
            for (int i = 0; i < count; ++i) {
                result.add(new PartableFile(in.readInt(), in.readUTF(), in.readLong()));
            }
            return result;
        }
    }
    int upload(PartableFile f) throws IOException {
        synchronized (serverConnection) {
            out.writeByte(2);
            out.writeUTF(f.getName());
            out.writeLong(f.getSize());
            files.add(f);
            int id = in.readInt();
            f.setId(id);
            return id;
        }
    }
    void download(int id, String path) throws IOException {
        PartableFile file = null;
        for (PartableFile f : list()) {
            if (f.getId() == id) {
                file = f;
                break;
            }
        }
        files.add(file);
        Iterable<Sid> source = sources(id);
        final PartableFile finalFile = file;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<Integer, ArrayList<Sid>> sids = new HashMap<>();
                ArrayList<Thread> threads = new ArrayList<>();
                for (Sid sid : source) {
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Socket connection = null;
                            DataInputStream in = null;
                            DataOutputStream out = null;
                            try {
                                connection = sid.connect();
                                in = new DataInputStream(connection.getInputStream());
                                out = new DataOutputStream(connection.getOutputStream());
                                out.writeByte(1);
                                out.writeInt(id);
                                int n = in.readInt();
                                for (int i = 0; i < n; ++i) {
                                    int cur = in.readInt();
                                    synchronized (sids) {
                                        if (sids.get(cur) == null) {
                                            sids.put(cur, new ArrayList<>());
                                        }
                                        sids.get(cur).add(sid);
                                    }
                                }
                            } catch (IOException e) {

                            }
                        }
                    });
                    thread.start();
                    threads.add(thread);
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }
                }
                threads.clear();
                System.out.println("info downloaded");

                RandomAccessFile file = null;
                try {
                    finalFile.createFile(path);
                    file = new RandomAccessFile(finalFile.getFile().getPath(), "rw");
                    file.setLength(finalFile.getSize());
                } catch (FileNotFoundException e) {

                } catch (IOException e) {
                }
                for (Map.Entry<Integer, ArrayList<Sid>> part : sids.entrySet()) {
                    //final RandomAccessFile finalF = file;
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            Socket connection = null;
                            DataInputStream in = null;
                            DataOutputStream out = null;
                            for (Sid sid : part.getValue()) {
                                try {
                                    connection = sid.connect();
                                    in = new DataInputStream(connection.getInputStream());
                                    out = new DataOutputStream(connection.getOutputStream());
                                    System.out.println("part");
                                    System.out.println(part.getKey());
                                    out.writeByte(2);
                                    out.writeInt(id);
                                    System.out.println("id");
                                    System.out.println(id);
                                    out.writeInt(part.getKey());
                                    byte[] buffer = new byte[1 << 10];
                                    int len = in.read(buffer);
                                    System.out.println("len");
                                    System.out.println(len);
                                    RandomAccessFile file = new RandomAccessFile(finalFile.getFile().getPath(), "rw");
                                    file.seek((1 << 10) * part.getKey());
                                    file.write(buffer, 0, len);
                                    finalFile.addPart(part.getKey());
                                    System.out.println("part downloaded");
                                    break;
                                } catch (IOException e) {
                                    System.out.println("fail");
                                    e.printStackTrace();
                                }
                            }
                        }
                    });
                    thread.start();
                    threads.add(thread);
                }
                for (Thread thread : threads) {
                    try {
                        thread.join();
                    } catch (InterruptedException e) {
                    }
                }

                System.out.println("file downloaded");
            }
        });
        thread.setDaemon(true);
        thread.start();
    }
    private Iterable<Sid> sources(int id) throws IOException {
        synchronized (serverConnection) {
            ArrayList<Sid> result = new ArrayList<>();
            out.writeByte(3);
            out.writeInt(id);
            int count = in.readInt();
            for (int i = 0; i < count; ++i) {
                byte[] ip = new byte[4];
                for (int j = 0; j < 4; ++j) {
                    ip[j] = in.readByte();
                }
                result.add(new Sid(ip, in.readInt()));
            }
            return result;
        }
    }
    private void setUpdate() {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (serverConnection) {
                    try {
                        out.writeByte(4);
                        out.writeInt(client.getPort());
                        out.writeInt(files.size());
                        for (PartableFile file : files) {
                            out.writeInt(file.getId());
                        }
                        in.readBoolean();
                    } catch (IOException e) {
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

    TorrentClient() throws IOException {
        serverConnection = new Socket("127.0.0.1", 8081);
        in = new DataInputStream(serverConnection.getInputStream());
        out = new DataOutputStream(serverConnection.getOutputStream());
        client = new ClientSid();
        files = new ArrayList<>();
        Thread thread = new Thread(client);
        thread.setDaemon(true);
        thread.start();
        setUpdate();
    }

    public static void main(String[] args) throws IOException {
        TorrentClient client = new TorrentClient();
        try {
            client.read(new DataInputStream(new FileInputStream("client.info")));
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
            try {
                switch (command[0]) {
                    case ("list"):
                        for (PartableFile file : client.list()) {
                            System.out.println(file.toString());
                        }
                        break;
                    case ("upload"):
                        if (command.length < 2) {
                            System.out.println("wrong");
                            continue;
                        }
                        System.out.println(client.upload(new PartableFile(new File(command[1]))));
                        break;
                    case ("download"):
                        if (command.length < 3) {
                            System.out.println("wrong");
                            continue;
                        }
                        client.download(Integer.valueOf(command[1]), command[2]);
                        break;
                    case ("exit"):
                        try {
                            client.write(new DataOutputStream(new FileOutputStream("client.info", false)));
                        } catch (FileNotFoundException e) {
                            System.out.println("can't find file");
                        } catch (IOException e) {
                            System.out.println("can't write file");
                        }
                        System.exit(0);
                    default:
                        System.out.println("wrong");
                }
            }catch (IOException e) {
                System.out.println("error");
            }
        }
    }
}
