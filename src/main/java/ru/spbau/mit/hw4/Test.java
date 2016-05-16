package ru.spbau.mit.hw4;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;

import static org.junit.Assert.assertTrue;

public class Test {

    @BeforeClass
    public static void setUp() throws Exception {
        TorrentServer server = new TorrentServer();
        Thread thread = new Thread(server);
        thread.setDaemon(true);
        thread.start();
    }

    @org.junit.Test
    public void onePartTest() throws IOException {
        TorrentClient client1 = new TorrentClient();
        TorrentClient client2 = new TorrentClient();
        File in = new File("src/main/resources/from/file1.txt");
        File out = new File("src/main/resources/to/file1.txt");
        int id = client1.upload(new PartableFile(in));
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        client2.download(id, "src/main/resources/to");
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        assertTrue(FileUtils.contentEquals(in, out));
        out.delete();
    }
    @org.junit.Test
    public void twoPartsTest() throws IOException {
        TorrentClient client1 = new TorrentClient();
        TorrentClient client2 = new TorrentClient();
        TorrentClient client3 = new TorrentClient();
        File in = new File("src/main/resources/from/file2.txt");
        File middle = new File("src/main/resources/middle/file2.txt");
        File out = new File("src/main/resources/to/file2.txt");
        int id = client1.upload(new PartableFile(in));
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        client2.download(id, "src/main/resources/middle");
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        client1.files.get(0).parts.clear();
        client1.files.get(0).parts.add(0);
        client2.files.get(0).parts.clear();
        client2.files.get(0).parts.add(1);
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        client3.download(id, "src/main/resources/to");
        try {
            Thread.sleep(TorrentClient.time * 2);
        } catch (InterruptedException e) {
        }
        assertTrue(FileUtils.contentEquals(in, out));
        assertTrue(FileUtils.contentEquals(in, middle));
        assertTrue(FileUtils.contentEquals(middle, out));
        middle.delete();
        out.delete();
    }
}
