package ru.spbau.mit.hw4;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Before;

import static org.junit.Assert.assertTrue;

public class Test {

    @Before
    public void setUp() throws Exception {
        TorrentServer server = new TorrentServer();
        Thread thread = new Thread(server);
        thread.setDaemon(true);
        thread.start();
    }

    @org.junit.Test
    public void onePartTest() throws IOException {
        TorrentClient client1 = new TorrentClient();
        TorrentClient client2 = new TorrentClient();
        File in = new File("src/main/resourses/from/file.txt");
        File out = new File("src/main/resourses/to/file.txt");
        int id = client1.upload(new PartableFile(in));
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        client2.download(id, "src/main/resourses/to");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        assertTrue(FileUtils.contentEquals(in, out));
    }
    @org.junit.Test
    public void twoPartsTest() throws IOException {
        TorrentClient client1 = new TorrentClient();
        TorrentClient client2 = new TorrentClient();
        TorrentClient client3 = new TorrentClient();
        File in = new File("src/main/resourses/from/file.txt");
        File middle = new File("src/main/resourses/middle/file.txt");
        File out = new File("src/main/resourses/to/file.txt");
        int id = client1.upload(new PartableFile(in));
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        client2.download(id, "src/main/resourses/middle");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        client1.files.get(0).parts.clear();
        client1.files.get(0).parts.add(0);
        client2.files.get(0).parts.clear();
        client2.files.get(0).parts.add(1);
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        client3.download(id, "src/main/resourses/to");
        try {
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        assertTrue(FileUtils.contentEquals(in, out));
        assertTrue(FileUtils.contentEquals(in, middle));
        assertTrue(FileUtils.contentEquals(middle, out));
    }
}
