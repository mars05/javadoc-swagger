package com.github.mars05.jts.swagger;

import com.github.mars05.jts.handler.Comment;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCVariableDecl;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * @author yu.xiao
 */
public class ApiParamHandler extends SwaggerHandler<JCVariableDecl> {
    private final static String SWAGGER_CLASS = "io.swagger.annotations.ApiParam";

    public ApiParamHandler(Context context, JCTree.JCCompilationUnit unit) {
        super(context, unit);
    }

    @Override
    protected boolean isNeedHandle1(JCVariableDecl tree) {
        List<JCTree.JCAnnotation> annotations = tree.getModifiers().getAnnotations();
        for (JCTree.JCAnnotation annotation : annotations) {
            Type type = annotation.type;
            if (type == null) {
                if (annotation.annotationType instanceof JCTree.JCFieldAccess) {
                    if (SWAGGER_CLASS.equals(annotation.annotationType.toString())) {
                        return false;
                    }
                }
                continue;
            }
            if (SWAGGER_CLASS.equals(type.toString())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void handleComment(Comment comment, JCVariableDecl tree) {

    }

    @Override
    public void handleAST(JCVariableDecl tree) {
    }

    final void handleAST(Comment.Param param, JCVariableDecl tree) {
        if (null == tree || !isNeedHandle(tree)
                || null == param) {
            return;
        }
        JCTree.JCFieldAccess select = makeSelect();
        doImport(select);
        tree.getModifiers().annotations = tree.getModifiers()
                .getAnnotations().append(makeAnnotation(select, param.getParamDesc()));
    }

    private JCTree.JCFieldAccess makeSelect() {
        return getMaker().Select(
                getMaker().Ident(getNames().fromString("io.swagger.annotations")),
                getNames().fromString("ApiParam")
        );
    }

    private JCTree.JCAnnotation makeAnnotation(JCTree.JCFieldAccess select, String paramDesc) {
        JCTree.JCAssign assign = getMaker().Assign(
                getMaker().Ident(getNames().fromString("value")),
                getMaker().Literal(paramDesc)
        );
        List<JCTree.JCExpression> args = List.of(assign);
        return getMaker().Annotation(select, args);
    }
}
