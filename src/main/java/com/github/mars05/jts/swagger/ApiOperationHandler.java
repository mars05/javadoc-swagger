package com.github.mars05.jts.swagger;

import com.github.mars05.jts.handler.Comment;
import com.github.mars05.jts.util.ASTUtils;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.*;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;

import java.util.Arrays;

/**
 * @author yu.xiao
 */
public class ApiOperationHandler extends SwaggerHandler<JCMethodDecl> {
    private final static String SWAGGER_CLASS = "io.swagger.annotations.ApiOperation";
    private final static String[] REST_CLASS = new String[]{
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping",
            "org.springframework.web.bind.annotation.RequestMapping",
    };

    private ApiParamHandler apiParamHandler;
    private Context context;

    public ApiOperationHandler(Context context, JCCompilationUnit unit) {
        super(context, unit);
        this.context = context;
        this.apiParamHandler = new ApiParamHandler(context, unit);

    }

    @Override
    public boolean isNeedHandle1(JCMethodDecl tree) {
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
            if (Arrays.asList(REST_CLASS).contains(type.toString())) {
                hasRest = true;
            }
        }
        return hasRest;
    }

    @Override
    protected void handleComment(Comment comment, JCMethodDecl tree) {
        JCFieldAccess select = makeSelect();
        doImport(select);
        tree.getModifiers().annotations = tree.getModifiers()
                .getAnnotations().append(makeAnnotation(select, comment));
        // 处理入参
        List<JCVariableDecl> params = tree.params;
        if (params != null) {
            for (JCVariableDecl param : params) {
                apiParamHandler.handleAST(comment.getParam(param.getName().toString()), param);
                // ApiModel处理
                if (param.sym != null && param.sym.type != null) {
                    JCCompilationUnit apiModelUnit = ASTUtils.COMPILATION_UNIT_MAP.get(param.sym.type.toString());
                    if (apiModelUnit != null) {
                        ApiModelHandler apiModelHandler = new ApiModelHandler(context, apiModelUnit);
                        ApiModelPropertyHandler propertyHandler = new ApiModelPropertyHandler(context, apiModelUnit);
                        apiModelUnit.accept(new TreeTranslator() {
                            @Override
                            public void visitClassDef(JCClassDecl jcClassDecl) {
                                super.visitClassDef(jcClassDecl);
                                apiModelHandler.handleAST(jcClassDecl);
                            }

                            @Override
                            public void visitMethodDef(JCMethodDecl jcMethodDecl) {
                                super.visitMethodDef(jcMethodDecl);
                                propertyHandler.methodHandler.handleAST(jcMethodDecl);
                            }

                            @Override
                            public void visitVarDef(JCVariableDecl jcVariableDecl) {
                                super.visitVarDef(jcVariableDecl);
                                propertyHandler.fieldHandler.handleAST(jcVariableDecl);
                            }
                        });
                    }
                }
            }
        }
        // 处理出参
        JCExpression restype = tree.restype;
        if (restype != null && restype.type != null) {
            JCCompilationUnit apiModelUnit = ASTUtils.COMPILATION_UNIT_MAP.get(restype.type.toString());
            if (apiModelUnit != null) {
                ApiModelHandler apiModelHandler = new ApiModelHandler(context, apiModelUnit);
                ApiModelPropertyHandler propertyHandler = new ApiModelPropertyHandler(context, apiModelUnit);
                apiModelUnit.accept(new TreeTranslator() {
                    @Override
                    public void visitClassDef(JCClassDecl jcClassDecl) {
                        super.visitClassDef(jcClassDecl);
                        apiModelHandler.handleAST(jcClassDecl);
                    }

                    @Override
                    public void visitMethodDef(JCMethodDecl jcMethodDecl) {
                        super.visitMethodDef(jcMethodDecl);
                        propertyHandler.methodHandler.handleAST(jcMethodDecl);
                    }

                    @Override
                    public void visitVarDef(JCVariableDecl jcVariableDecl) {
                        super.visitVarDef(jcVariableDecl);
                        propertyHandler.fieldHandler.handleAST(jcVariableDecl);
                    }
                });
            }
        }
    }

    private JCFieldAccess makeSelect() {
        return getMaker().Select(
                getMaker().Ident(getNames().fromString("io.swagger.annotations")),
                getNames().fromString("ApiOperation")
        );
    }

    private JCAnnotation makeAnnotation(JCFieldAccess select, Comment comment) {
        JCTree.JCAssign assign = getMaker().Assign(
                getMaker().Ident(getNames().fromString("value")),
                getMaker().Literal(comment.getDesc())
        );
        List<JCTree.JCExpression> args = List.of(assign);
        return getMaker().Annotation(select, args);
    }
}
