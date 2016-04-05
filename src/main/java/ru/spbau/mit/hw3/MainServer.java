package ru.spbau.mit.hw3;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by YuryKravchenko on 14/03/16.
 */
public class MainServer {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        while (true) {
            String cmd = in.nextLine();
            try {
                switch (cmd) {
                    case "start":
                        Thread newThread = new Thread(new Server());
                        newThread.setDaemon(true);
                        newThread.start();
                        break;
                    case "stop":
                        return;
                }
            } catch (IOException e) {
                System.out.println("starting server failed, try again");
                return;
            }
        }
    }
}
