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
    protected static final String cyrillicLower =
            "\u0430\u0431\u0432\u0433\u0434\u0435\u0436\u0437\u0438" +
                    "\u043a\u043b\u043c\u043d\u043e\u043f\u0440\u0441\u0441\u0443" +
                    "\u0444\u0445\u0446\u0447\u0448\u0449\u044b\u044d\u044e\u044f";

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
