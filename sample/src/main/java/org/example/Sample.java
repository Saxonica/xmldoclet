package org.example;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Sample application
 * <p>This application exists just to test the various features of the xmldoclet.</p>
 * @since 0.1.0
 * @author <a href="mailto:norm@saxonica.com">Norm Tovey-Walsh</a>
 */
public class Sample {
    public static void main(String[] args) {
        SampleRuntime runtime = new SampleRuntime();
        runtime.run();
    }

    public void throwsOne() throws IOException {
        try {
            File f = new File("/tmp/not-likely");
            FileInputStream fis = new FileInputStream(f);
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }

    /**
     * Throws two exceptions, but only documents one.
     * @throws IOException if the file isn't found
     */
    public void throwsTwo() throws IOException, IllegalArgumentException {
        try {
            File f = new File("/tmp/not-likely");
            FileInputStream fis = new FileInputStream(f);
            if (fis == null) {
                throw new IllegalArgumentException("Can't have a null file");
            }
        } catch (FileNotFoundException ex) {
            throw ex;
        }
    }

}
