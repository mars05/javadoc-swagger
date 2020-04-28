package com.github.mars05.jts.swagger;

import com.github.mars05.jts.handler.Handler;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

/**
 * SwaggerASTVisitor
 *
 * @author yu.xiao
 */
public class SwaggerASTVisitor extends TreeTranslator {

    private Handler<JCTree.JCClassDecl> apiHandler;
    private Handler<JCTree.JCMethodDecl> apiOperationHandler;

    public SwaggerASTVisitor(Context context, JCCompilationUnit unit) {
        apiHandler = new ApiHandler(context, unit);
        apiOperationHandler = new ApiOperationHandler(context, unit);
    }

    @Override
    public void visitClassDef(JCTree.JCClassDecl tree) {
        super.visitClassDef(tree);
        apiHandler.handleAST(tree);
    }

    @Override
    public void visitMethodDef(JCTree.JCMethodDecl tree) {
        super.visitMethodDef(tree);
        apiOperationHandler.handleAST(tree);
    }
}
