package ru.spbau.mit.hw3;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by YuryKravchenko on 14/03/16.
 */
public class MainClient {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        Client client = null;
        while (true) {
            String cmd = in.nextLine();
            switch (cmd) {
                case "connect":
                    try {
                        client = new Client();
                    } catch (IOException e) {
                        System.out.println("starting client failed, try again");
                    }
                    break;
                case "executeList":
                    cmd = in.nextLine();
                    try {
                        System.out.println(client.List(cmd));
                    } catch (IOException e) {
                        System.out.println("connection failed");
                    } catch (NullPointerException e) {
                        System.out.println("no connection");
                    }
                    break;
                case "executeGet":
                    cmd = in.nextLine();
                    try {
                        System.out.println(client.Get(cmd));
                    } catch (IOException e) {
                        System.out.println("connection failed");
                    } catch (NullPointerException e) {
                        System.out.println("no connection");
                    }
                    break;
                case "disconnect":
                    return;
            }
        }
    }
}
