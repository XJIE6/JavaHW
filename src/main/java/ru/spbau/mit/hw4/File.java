package ru.spbau.mit.hw4;

import java.util.ArrayList;

class File {
    private int id;
    private String name;
    private long size;
    ArrayList<Integer> parts;
    File (int id, String name, long size) {
        this.id = id;
        this.name = name;
        this.size = size;
        parts = null;
    }
    String getName() {
        return name;
    }
    long getSize() {
        return size;
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
        return String.valueOf(id) + " " + name + " " + String.valueOf(size);
    }
}