package ru.spbau.mit.hw4;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class ClientUI extends JFrame {
    TorrentClient client;
    Box eastBox;
    Box box;

    ClientUI() throws IOException {
        super("main");
        client = new TorrentClient();
        setBounds(100, 100, 700, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        eastBox = Box.createVerticalBox();
        getContentPane().add(eastBox, BorderLayout.EAST);

        JButton button = new JButton("upload");
        button.addActionListener((e) -> {
            try {
                upload();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        getContentPane().add(button, BorderLayout.WEST);

        button = new JButton("refresh list");
        button.addActionListener((e) -> {
            try {
                evallist();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        });
        getContentPane().add(button, BorderLayout.NORTH);
        setVisible(true);

        box = Box.createVerticalBox();
        getContentPane().add(box, BorderLayout.CENTER);
        evallist();
    }

    private void download(PartableFile file) {
        HashMap<Integer, ArrayList<Sid>> sids = null;
        try {
            sids = client.getSids(file.getId());
        } catch (IOException e) {
            return;
        }

        JFrame frame = new JFrame("chose folder");
        JFileChooser directoryChooser = new JFileChooser();
        directoryChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        directoryChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        frame.setVisible(true);
        frame.add(directoryChooser);
        if (directoryChooser.showOpenDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                file.createFile(directoryChooser.getSelectedFile().getPath());
                RandomAccessFile rAFile = new RandomAccessFile(file.getFile().getPath(), "rw");
                rAFile.setLength(file.getSize());
            }catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                return;
            }
        }
        else {
            return;
        }
        frame.setVisible(false);

        JProgressBar pb = new JProgressBar(0, (int) Math.floor(file.getSize() / (1.0 * TorrentClient.size)));


        box.add(new JTextField(file.toString()));
        box.add(pb);
        box.revalidate();




        client.files.add(file);



        ArrayList<Thread> threads = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Sid>> part : sids.entrySet()) {
            Thread thread = new Thread(() -> {
                client.downloadPart(part.getKey(), part.getValue(), file);
                synchronized (pb) {
                    pb.setValue(pb.getValue() + 1);
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


    private void evallist() throws IOException {
        eastBox.removeAll();
        for (PartableFile f : client.list()) {
            eastBox.add(new JTextField(f.toString()));
            JButton button = new JButton("download");
            button.addActionListener(e -> download(f));
            eastBox.add(button);
        }
        eastBox.revalidate();
    }

    private void upload() throws IOException {
        JFrame frame = new JFrame("chose file");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        frame.setVisible(true);
        int result = fileChooser.showOpenDialog(frame);
        frame.add(fileChooser);
        if (result == JFileChooser.APPROVE_OPTION) {
            client.upload(new PartableFile(fileChooser.getSelectedFile()));
            System.out.println("Selected file: " + fileChooser.getSelectedFile().getAbsolutePath());
        }
        frame.setVisible(false);
    }

    public static void main(String[] args) throws IOException {
        ClientUI ui = new ClientUI();
    }
}