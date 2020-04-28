package com.github.mars05.jts.swagger;

import com.github.mars05.jts.handler.Comment;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCAnnotation;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.JCTree.JCFieldAccess;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

/**
 * @author yu.xiao
 */
public class ApiHandler extends SwaggerHandler<JCClassDecl> {
    private final static String SWAGGER_CLASS = "io.swagger.annotations.Api";
    private final static String REST_CLASS = "org.springframework.web.bind.annotation.RestController";

    public ApiHandler(Context context, JCCompilationUnit unit) {
        super(context, unit);
    }

    @Override
    public boolean isNeedHandle1(JCClassDecl tree) {
        List<JCAnnotation> annotations = tree.getModifiers().getAnnotations();
        boolean hasRest = false;
        for (JCAnnotation annotation : annotations) {
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
            if (REST_CLASS.equals(type.toString())) {
                hasRest = true;
            }
        }
        return hasRest;
    }

    @Override
    protected void handleComment(Comment comment, JCClassDecl tree) {
        JCFieldAccess select = makeSelect();
        doImport(select);
        tree.getModifiers().annotations = tree.getModifiers()
                .getAnnotations().append(makeAnnotation(select, comment));
    }

    private JCFieldAccess makeSelect() {
        return getMaker().Select(
                getMaker().Ident(getNames().fromString("io.swagger.annotations")),
                getNames().fromString("Api")
        );
    }

    private JCAnnotation makeAnnotation(JCFieldAccess select, Comment comment) {
        JCTree.JCAssign assign = getMaker().Assign(
                getMaker().Ident(getNames().fromString("tags")),
                getMaker().Literal(comment.getDesc())
        );
        List<JCTree.JCExpression> args = List.of(assign);
        return getMaker().Annotation(select, args);
    }
}
