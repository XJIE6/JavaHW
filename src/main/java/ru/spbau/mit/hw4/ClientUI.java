package ru.spbau.mit.hw4;
import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import javax.swing.*;
import javax.swing.text.JTextComponent;

public class ClientUI extends JFrame {
    TorrentClient client;
    Box eastBox;
    Box centerBox;

    ClientUI() {
        super("main");
        try {
            client = new TorrentClient();
        } catch (IOException e) {
            return;
        }
        try {
            client.read(new DataInputStream(new FileInputStream("client.info")));
        } catch (IOException e) {
        }

        setBounds(100, 100, 700, 700);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        eastBox = Box.createVerticalBox();
        getContentPane().add(eastBox, BorderLayout.EAST);

        centerBox = Box.createVerticalBox();
        getContentPane().add(centerBox, BorderLayout.CENTER);

        Box northBox = Box.createHorizontalBox();
        getContentPane().add(northBox, BorderLayout.NORTH);

        JButton button = new JButton("upload");
        button.addActionListener((e) -> upload());
        northBox.add(button);

        button = new JButton("refresh list");
        button.addActionListener((e) -> evallist());
        northBox.add(button);

        button = new JButton("exit");
        button.addActionListener((e) -> {
            try {
                client.write(new DataOutputStream(new FileOutputStream("client.info", false)));
            } catch (IOException e1) {
            }
            System.exit(0);
        });
        northBox.add(button);

        evallist();

        setVisible(true);
    }

    private void download(PartableFile file) {
        System.out.print(client.files.size());

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

        JProgressBar pb = new JProgressBar(0, (int) Math.ceil(file.getSize() / (1.0 * TorrentClient.size)));
        centerBox.add(new JTextField(file.toString()));
        centerBox.add(pb);
        centerBox.revalidate();

        client.files.add(file);

        ArrayList<Thread> threads = new ArrayList<>();
        for (Map.Entry<Integer, ArrayList<Sid>> part : sids.entrySet()) {
            Thread thread = new Thread(() -> {
                if (client.downloadPart(part.getKey(), part.getValue(), file)) {
                    synchronized (pb) {
                        pb.setValue(pb.getValue() + 1);
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


    private void evallist() {
        Iterable<PartableFile> files = null;
        try {
            files = client.list();
        } catch (IOException e) {
            return;
        }
        eastBox.removeAll();
        for (PartableFile f : files) {
            eastBox.add(new JTextField(f.toString()));
            JButton button = new JButton("download");
            button.addActionListener(e -> download(f));
            eastBox.add(button);
        }
        eastBox.revalidate();
    }

    private void upload() {
        JFrame frame = new JFrame("chose file");
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
        frame.setVisible(true);
        int result = fileChooser.showOpenDialog(frame);
        frame.add(fileChooser);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                client.upload(new PartableFile(fileChooser.getSelectedFile()));
            } catch (IOException e) {
            }
            System.out.println("Selected file: " + fileChooser.getSelectedFile().getAbsolutePath());
        }
        frame.setVisible(false);
    }

    public static void main(String[] args) throws IOException {
        ClientUI ui = new ClientUI();
    }
}