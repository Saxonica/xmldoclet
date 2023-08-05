package com.saxonica.xmldoclet;

import com.sun.source.doctree.DocTree;
import com.sun.source.util.DocTreeScanner;

import java.io.PrintStream;

public class ShowDocTrees extends DocTreeScanner<Void, Integer> {
    final PrintStream out;

    ShowDocTrees(PrintStream out) {
        this.out = out;
    }

    @Override
    public Void scan(DocTree t, Integer depth) {
        String indent = "  ".repeat(depth);
        out.println(indent + ">>># "
                + t.getKind() + " "
                + t.toString().replace("\n", "\n" + indent + "#    "));
        return super.scan(t, depth + 1);
    }
}
