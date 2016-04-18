package ru.spbau.mit.hw4;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertTrue;

public class Test {
    @org.junit.Test
    public void onePartTest() throws IOException {
        TorrentServer server = new TorrentServer();
        Thread thread = new Thread(server);
        thread.setDaemon(true);
        thread.start();
        TorrentClient client1 = new TorrentClient();
        TorrentClient client2 = new TorrentClient();
        File in = new File("src/main/resourses/from/file.txt");
        File out = new File("src/main/resourses/to/file.txt");
        int id = client1.upload(new PartableFile(in));
        try {
            Thread.sleep(60000);
        } catch (InterruptedException e) {
        }
        client2.download(id, "src/main/resourses/to");
        assertTrue(FileUtils.contentEquals(in, out));
        out.delete();
    }

}
