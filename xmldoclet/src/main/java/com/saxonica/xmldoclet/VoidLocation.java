package com.saxonica.xmldoclet;

import net.sf.saxon.s9api.Location;

public class VoidLocation implements Location {
    private static final VoidLocation INSTANCE = new VoidLocation();

    static public VoidLocation getInstance() {
        return INSTANCE;
    }

    @Override
    public int getColumnNumber() {
        return -1;
    }

    @Override
    public int getLineNumber() {
        return -1;
    }

    @Override
    public String getPublicId() {
        return null;
    }

    @Override
    public String getSystemId() {
        return null;
    }

    @Override
    public Location saveLocation() {
        return this;
    }

    private VoidLocation() {
        // nothing
    }

}
