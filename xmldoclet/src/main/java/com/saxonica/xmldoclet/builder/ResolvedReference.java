package com.saxonica.xmldoclet.builder;

import java.util.ArrayList;
import java.util.List;

public class ResolvedReference {
    public final String signature;
    public final String packageName;
    public final String className;
    public final String methodName;
    public final String fieldName;

    public final List<String> args;
    public final List<String> resolvedArgs;

    public ResolvedReference(String signature, String pkg, String cls, String meth, String fld) {
        this.signature = signature;
        this.packageName = pkg;
        this.className = cls;
        this.methodName = meth;
        this.fieldName = fld;
        args = new ArrayList<>();
        resolvedArgs = new ArrayList<>();
    }

    protected void addArg(String arg, String resolvedArg) {
        args.add(arg);
        resolvedArgs.add(resolvedArg == null ? arg : resolvedArg);
    }
}
