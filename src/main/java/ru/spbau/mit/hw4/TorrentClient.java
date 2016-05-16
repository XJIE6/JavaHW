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
    static int time = 3000;
    static int size = 1 << 10;

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
                            id = in.readInt();
                            int part = in.readInt();
                            PartableFile file = null;
                            for(PartableFile f : files) {
                                if (f.getId() == id) {
                                    file = f;
                                    break;
                                }
                            }
                            RandomAccessFile f = new RandomAccessFile(file.getFile(), "rw");
                            byte[] buffer = new byte[size];
                            f.seek((size) * part);
                            int len = f.read(buffer, 0, (size));
                            out.write(buffer, 0, len);
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
    }

    Iterable<PartableFile> list() throws IOException {
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

    HashMap<Integer, ArrayList<Sid>> getSids(int id) throws IOException {
        HashMap<Integer, ArrayList<Sid>> sids = new HashMap<>();
        ArrayList<Thread> threads = new ArrayList<>();
        Iterable<Sid> source = sources(id);
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
        return sids;
    }

    void downloadPart(int partNumber, ArrayList<Sid> sids, PartableFile file) {
        Socket connection = null;
        DataInputStream in = null;
        DataOutputStream out = null;
        for (Sid sid : sids) {
            try {
                connection = sid.connect();
                in = new DataInputStream(connection.getInputStream());
                out = new DataOutputStream(connection.getOutputStream());
                out.writeByte(2);
                out.writeInt(file.getId());
                out.writeInt(partNumber);
                byte[] buffer = new byte[1 << 10];
                int len = in.read(buffer);
                RandomAccessFile rAFile = new RandomAccessFile(file.getFile().getPath(), "rw");
                rAFile.seek((1 << 10) * partNumber);
                rAFile.write(buffer, 0, len);
                file.addPart(partNumber);
                break;
            } catch (IOException e) {
            }
        }
    }

    void downloadFile(Map<Integer, ArrayList<Sid>> sids, PartableFile file) {
        ArrayList<Thread> threads = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Sid>> part : sids.entrySet()) {
            Thread thread = new Thread(() -> downloadPart(part.getKey(), part.getValue(), file));
            thread.start();
            threads.add(thread);
        }
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
            }
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

        final PartableFile finalFile = file;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                HashMap<Integer, ArrayList<Sid>> sids = null;
                try {
                    sids = getSids(id);
                } catch (IOException e) {
                    return;
                }
                RandomAccessFile file = null;
                try {
                    finalFile.createFile(path);
                    file = new RandomAccessFile(finalFile.getFile().getPath(), "rw");
                    file.setLength(finalFile.getSize());
                } catch (FileNotFoundException e) {
                } catch (IOException e) {
                }
                downloadFile(sids, finalFile);
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
        }, 0, time);
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

    public static void main(String[] args) {
        TorrentClient client = null;
        try {
            client = new TorrentClient();
        } catch (IOException e) {
            System.out.println("fail");
            System.exit(0);
        }
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
