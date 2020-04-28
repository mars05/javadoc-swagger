package com.github.mars05.jts;

import com.github.mars05.jts.swagger.SwaggerASTVisitor;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;

/**
 * HandlerCombiner
 *
 * @author yu.xiao
 */
final class HandlerCombiner {
    /**
     * handle
     *
     * @param unit 编译单元
     */
    public void handle(Context context, JCCompilationUnit unit) {
        if (isSwagger(unit)) {
            handleSwagger(context, unit);
        }
    }

    private boolean isSwagger(JCCompilationUnit unit) {
        return true;
    }

    private void handleSwagger(Context context, JCCompilationUnit unit) {
        unit.accept(new SwaggerASTVisitor(context, unit));
    }
}
