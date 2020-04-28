package com.github.mars05.jts.handler;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

/**
 * Handler
 *
 * @author yu.xiao
 */
public abstract class Handler<T extends JCTree> {
    private Names names;
    private TreeMaker maker;
    private JCCompilationUnit unit;


    public Handler(Context context, JCCompilationUnit unit) {
        names = Names.instance(context);
        maker = TreeMaker.instance(context);
        this.unit = unit;
    }

    /**
     * handleAST
     *
     * @param tree ast节点
     */
    public abstract void handleAST(T tree);

    /**
     * 判断是否需要进行处理
     *
     * @param tree ast节点
     * @return 是否需要处理
     */
    protected abstract boolean isNeedHandle(T tree);

    protected JCCompilationUnit getUnit() {
        return unit;
    }

    protected Names getNames() {
        return names;
    }

    protected TreeMaker getMaker() {
        return maker;
    }

    protected int importIndex() {
        List<JCTree> defs = getUnit().defs;
        if (defs == null || defs.size() == 0 || defs.get(0) instanceof JCTree.JCImport) {
            return 0;
        }
        return 1;
    }
}
