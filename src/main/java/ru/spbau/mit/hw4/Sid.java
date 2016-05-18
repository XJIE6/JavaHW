package ru.spbau.mit.hw4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class Sid {
    private byte[] ip;
    private int port;
    Sid(byte[] ip, int port) {
        this.ip = ip;
        this.port = port;
    }
    byte[] ip() {
        return ip;
    }
    int getPort() {
        return port;
    }
    Socket connect() throws IOException {
        return new Socket(InetAddress.getByAddress(ip), port);
    }
}
