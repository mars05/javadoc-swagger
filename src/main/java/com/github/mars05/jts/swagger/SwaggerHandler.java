package com.github.mars05.jts.swagger;

import com.github.mars05.jts.handler.CommentHandler;
import com.github.mars05.jts.util.ClassLoader;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCImport;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import java.util.ArrayList;

/**
 * @author yu.xiao
 */
public abstract class SwaggerHandler<T extends JCTree> extends CommentHandler<T> {
    private final static String SWAGGER_CLASS = "io.swagger.annotations.Api";

    private ClassLoader classLoader;

    public SwaggerHandler(Context context, JCTree.JCCompilationUnit unit) {
        super(context, unit);
        this.classLoader = context.get(ClassLoader.class);
    }

    @Override
    protected boolean isNeedHandle(T tree) {
        return classLoader.isExistClass(SWAGGER_CLASS) && isNeedHandle1(tree);
    }

    protected abstract boolean isNeedHandle1(T tree);

    protected void doImport(JCTree.JCFieldAccess select) {
        JCImport jcImport = getMaker().Import(select, false);
        java.util.List<JCTree> trees = new ArrayList<>(getUnit().defs);
        if (!trees.contains(jcImport)) {
            trees.add(importIndex(), jcImport);
        }
        getUnit().defs = List.from(trees);
    }

}
