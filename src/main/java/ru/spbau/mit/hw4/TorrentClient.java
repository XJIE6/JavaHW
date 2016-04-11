package ru.spbau.mit.hw4;

import java.io.*;
import java.net.Socket;
import java.util.*;

public class TorrentClient {
    private static Sid server;
    private static ClientSid client;
    private static Socket serverConnection;
    private static DataInputStream in;
    private static DataOutputStream out;
    private static ArrayList<File> files;
    private static Iterable<File> list() throws IOException {
        synchronized (serverConnection) {
            ArrayList<File> result = new ArrayList<>();
            out.writeByte(1);
            int count = in.readInt();
            for (int i = 0; i < count; ++i) {
                result.add(new File(in.readInt(), in.readUTF(), in.readLong()));
            }
            return result;
        }
    }
    private static int upload(File f) throws IOException {
        synchronized (serverConnection) {
            out.writeByte(2);
            out.writeUTF(f.getName());
            out.writeLong(f.getSize());
            return in.readInt();
        }
    }
    private static void download(int id) throws IOException {
        File file = null;
        for (File f : list()) {
            if (f.getId() == id) {
                file = f;
                break;
            }
        }
        files.add(file);
        HashMap<Integer, ArrayList<Sid>> sids = new HashMap<>();
        ArrayList<Thread> threads = new ArrayList<>();
        for (Sid sid : sources(id)) {
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
        RandomAccessFile f = null;
        f = new RandomAccessFile(file.getName(), "rw");
        for (Map.Entry<Integer, ArrayList<Sid>> part : sids.entrySet()) {
            final File finalFile = file;
            final RandomAccessFile finalF = f;
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
                            out.writeByte(2);
                            out.writeInt(id);
                            out.writeInt(part.getKey());
                            byte[] buffer = new byte[1 << 20];
                            int len = in.read(buffer);
                            finalF.write(buffer, (1 << 20) * part.getKey(), len);
                            finalFile.addPart(part.getKey());
                        } catch (IOException e) {
                            continue;
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
    }
    private static Iterable<Sid> sources(int id) throws IOException {
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
                result.add(new Sid(ip, in.readShort()));
            }
            return result;
        }
    }
    private static void setUpdate() {
        new Timer(true).schedule(new TimerTask() {
            @Override
            public void run() {
                synchronized (serverConnection) {
                    try {
                        out.writeByte(4);
                        out.writeShort(client.getPort());
                        out.writeInt(files.size());
                        for (File file : files) {
                            out.writeInt(file.getId());
                        }
                        in.readBoolean();
                    } catch (IOException e) {
                    }

                }
            }
        }, 0, 300000);
    }
    static ArrayList<File> getFiles() {
        return files;
    }
    public static void main(String[] args) {
        try {
            client = new ClientSid();
        } catch (IOException e) {
        }
        Thread thread = new Thread(client);
        thread.setDaemon(true);
        thread.start();

        setUpdate();

        Scanner in = new Scanner(System.in);
        while (true) {
            String[] command = in.nextLine().split(" ");
            if (command.length == 0) {
                System.out.println("wrong");
            }
            try {
                switch (command[0]) {
                    case ("list"):
                        for (File file : list()) {
                            System.out.println(file.toString());
                        }
                        break;
                    case ("upload"):
                        if (command.length < 3) {
                            System.out.println("wrong");
                        }
                        System.out.println(upload(new File(0, command[1], Long.valueOf(command[2]))));
                        break;
                    case ("download"):
                        if (command.length < 2) {
                            System.out.println("wrong");
                        }
                        download(Integer.valueOf(command[1]));
                        break;
                    case ("exit"):
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
