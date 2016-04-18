package ru.spbau.mit.hw4;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class PartableFile {
    private int id;
    private File file = null;
    private String name = null;
    private long size;
    ArrayList<Integer> parts = new ArrayList<>();
    PartableFile(File file) {
        this.file = file;
        name = file.getName();
        size = file.length();
        for (int i = 0; i * (1 << 20) < file.length(); ++i) {
            parts.add(i);
        }
    }
    PartableFile(int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
    }

    PartableFile(DataInputStream in) throws IOException {
        id = in.readInt();
        if (in.readBoolean()) {
            file = new File(in.readUTF());
            name = file.getName();
            size = file.length();
        }
        else {
            name = in.readUTF();
            size = in.readLong();
        }
        int count = in.readInt();
        for (int i = 0; i < count; ++i) {
            parts.add(in.readInt());
        }
    }

    void write(DataOutputStream out) throws IOException {
        out.writeInt(id);
        if (file != null) {
            out.writeBoolean(true);
            out.writeUTF(file.getPath());
        }
        else {
            out.writeBoolean(false);
            out.writeUTF(name);
            out.writeLong(size);
        }
        out.writeInt(parts.size());
        for (Integer i : parts) {
            out.writeInt(i);
        }
    }

    void setId(int id) {
        this.id = id;
    }
    String getName() {
        return name;
    }
    long getSize() {
        return size;
    }
    File getFile() {
        return file;
    }
    void createFile(String path) throws IOException {
        file = new File(path + "//" + name);
        file.getParentFile().mkdirs();
        file.createNewFile();
    }
    int getId() {
        return id;
    }
    ArrayList<Integer> getParts() {
        return parts;
    }
    void addPart(int number) {
        parts.add(number);
    }
    public String toString() {
        return String.valueOf(id) + " " + getName() + " " + String.valueOf(getSize());
    }
}