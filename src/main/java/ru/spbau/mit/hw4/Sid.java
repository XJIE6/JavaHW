package ru.spbau.mit.hw4;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

class Sid {
    private byte[] ip;
    private short port;
    Sid(byte[] ip, short port) {
        this.ip = ip;
        this.port = port;
    }
    byte[] ip() {
        return ip;
    }
    Short getPort() {
        return port;
    }
    Socket connect() throws IOException {
        return new Socket(InetAddress.getByAddress(ip), port);
    }
}
