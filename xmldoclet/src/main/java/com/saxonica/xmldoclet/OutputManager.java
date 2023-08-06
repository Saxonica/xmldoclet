package com.saxonica.xmldoclet;

import javax.lang.model.element.Element;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

public class OutputManager {
    private static final String ROOT = "xmldoclet";
    private final String destinationDir;
    private final String outputFilename;
    private PrintStream outputStream = null;

    public OutputManager(String destinationDir) {
        this(destinationDir, null);
    }

    public OutputManager(String destinationDir, String outputFilename) {
        this.destinationDir = destinationDir;
        this.outputFilename = outputFilename;
    }

    public void beginOutput() {
        if (outputFilename != null) {
            try {
                File outputFile = new File(destinationDir + "/" + outputFilename);
                File outputDir = outputFile.getParentFile();
                outputDir.mkdirs();
                outputStream = new PrintStream(outputFile);
                outputStream.printf("<%s>%n", ROOT);
                admin(outputStream);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }

    public void endOutput() {
        if (outputStream != null) {
            outputStream.printf("</%s>%n", ROOT);
            outputStream.close();
            outputStream = null;
        }
    }

    public void output(Element elem, String xml) {
        if (outputStream != null) {
            outputStream.println(xml);
            return;
        }

        final String filename;
        switch (elem.getKind()) {
            case PACKAGE:
                //System.err.printf("Scanning package: %s%n", elem);
                filename = destinationDir + "/" + elem.toString().replace(".", "/") + "/package.xml";
                break;
            case CLASS:
            case INTERFACE:
            case ENUM:
                //System.err.printf("Scanning class: %s%n", elem);
                filename = destinationDir + "/" + elem.toString().replace(".", "/") + ".xml";
                break;
            default:
                System.err.println("Unexpected element to scan: " + elem);
                filename = null;
                break;
        }

        if (filename == null) {
            return;
        }

        try {
            File outputFile = new File(destinationDir + "/" + outputFilename);
            File outputDir = outputFile.getParentFile();
            outputDir.mkdirs();
            PrintStream output = new PrintStream(outputFile);
            output.printf("<%s>%n", ROOT);
            admin(output);
            output.println(xml);
            output.printf("</%s>%n", ROOT);
            output.close();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void admin(PrintStream output) {
        output.printf("<admin created='%s' version='%s' schemaVersion='%s' hash='%s'/>%n",
                BuildConfig.PUB_DATE, BuildConfig.VERSION, BuildConfig.SCHEMA_VERSION,
                BuildConfig.PUB_HASH);
    }

}
