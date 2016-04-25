/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gui.conf_screen;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Jean-Pierre Le Rouzic
 */
public class FileCopy {

    public static void filecopy(String sourceFileName, String destDirectory) {
        File source = new File(sourceFileName);
        File destination = new File(destDirectory + File.separatorChar + source.getName());
        File directory = new File(destDirectory);
        if (!directory.exists()) {
            try {
                System.out.println("creating new destination directory " + destDirectory);
                directory.mkdirs();
            } catch (Exception e) {
                System.err.println("could not create the destination directory " + destDirectory);
            }
        }
        FileChannel in = null;
        FileChannel out = null;
        try {
            if (source.isDirectory()) {
                File newDirectory = new File(destination.getAbsolutePath());
                newDirectory.createNewFile();
                for (File file : source.listFiles()) {
                    if (!file.getName().startsWith(".svn")) {
                        filecopy(file.getAbsolutePath(), destDirectory);
                    }
                }
            } else {
                in = new FileInputStream(source).getChannel();
                out = new FileOutputStream(destination).getChannel();
                long size = in.size();
                MappedByteBuffer buf = in.map(FileChannel.MapMode.READ_ONLY, 0, size);
                out.write(buf);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
}
