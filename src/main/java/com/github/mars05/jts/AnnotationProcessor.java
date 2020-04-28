package com.github.mars05.jts;

import com.github.mars05.jts.util.ASTUtils;
import com.github.mars05.jts.util.ClassLoader;
import com.github.mars05.jts.util.Permit;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacFiler;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.tree.TreeTranslator;
import com.sun.tools.javac.util.Context;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.JavaFileManager;
import javax.tools.StandardLocation;
import java.lang.reflect.Field;
import java.util.Set;

/**
 * 注解处理器
 *
 * @author yu.xiao
 */
@SupportedAnnotationTypes("*")
public class AnnotationProcessor extends AbstractProcessor {
    private ProcessingEnvironment processingEnv;
    private JavacProcessingEnvironment javacProcessingEnv;
    private JavacFiler javacFiler;
    private Trees trees;
    private TreeMaker treeMaker;
    private HandlerCombiner combiner = new HandlerCombiner();
    private Context context;

    @Override
    public synchronized void init(ProcessingEnvironment procEnv) {
        super.init(procEnv);
        this.processingEnv = procEnv;
        this.javacProcessingEnv = getJavacProcessingEnvironment(procEnv);
        this.javacFiler = getJavacFiler(procEnv.getFiler());
        trees = Trees.instance(javacProcessingEnv);
        context = this.javacProcessingEnv.getContext();
        context.put(ClassLoader.class, new ClassLoader(resolveParent()));
        treeMaker = TreeMaker.instance(context);
    }

    private java.lang.ClassLoader resolveParent() {
        return context.get(JavaFileManager.class).getClassLoader(StandardLocation.CLASS_PATH);
    }

    /**
     * This class casts the given processing environment to a JavacProcessingEnvironment. In case of
     * gradle incremental compilation, the delegate ProcessingEnvironment of the gradle wrapper is returned.
     */
    public JavacProcessingEnvironment getJavacProcessingEnvironment(Object procEnv) {
        if (procEnv instanceof JavacProcessingEnvironment) {
            return (JavacProcessingEnvironment) procEnv;
        }

        // try to find a "delegate" field in the object, and use this to try to obtain a JavacProcessingEnvironment
        for (Class<?> procEnvClass = procEnv.getClass(); procEnvClass != null; procEnvClass = procEnvClass.getSuperclass()) {
            try {
                return getJavacProcessingEnvironment(tryGetDelegateField(procEnvClass, procEnv));
            } catch (final Exception e) {
                // delegate field was not found, try on superclass
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Can't get the delegate of the gradle IncrementalProcessingEnvironment. Apidoc won't work.");
        return null;
    }

    /**
     * This class returns the given filer as a JavacFiler. In case the filer is no
     * JavacFiler (e.g. the Gradle IncrementalFiler), its "delegate" field is used to get the JavacFiler
     * (directly or through a delegate field again)
     */
    public JavacFiler getJavacFiler(Object filer) {
        if (filer instanceof JavacFiler) {
            return (JavacFiler) filer;
        }

        // try to find a "delegate" field in the object, and use this to check for a JavacFiler
        for (Class<?> filerClass = filer.getClass(); filerClass != null; filerClass = filerClass.getSuperclass()) {
            try {
                return getJavacFiler(tryGetDelegateField(filerClass, filer));
            } catch (final Exception e) {
                // delegate field was not found, try on superclass
            }
        }

        processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                "Can't get a JavacFiler from " + filer.getClass().getName() + ". Apidoc won't work.");
        return null;
    }

    private Object tryGetFileManagerField(Class<?> delegateClass, Object instance) throws Exception {
        Field field = null;
        try {
            field = Permit.getField(delegateClass, "fileManager");
        } catch (NoSuchFieldException e) {
        }
        try {
            field = Permit.getField(delegateClass, "baseSJFM");
        } catch (NoSuchFieldException e) {
        }
        if (field == null) {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.WARNING,
                    "tryGetFileManagerField from " + delegateClass.getClass().getName() + ". Apidoc won't work.");
        }
        return field.get(instance);
    }

    private Object tryGetDelegateField(Class<?> delegateClass, Object instance) throws Exception {
        return Permit.getField(delegateClass, "delegate").get(instance);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.values()[SourceVersion.values().length - 1];
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return super.getSupportedAnnotationTypes();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (roundEnv.processingOver()) {
            return false;
        }
        if (annotations == null || annotations.isEmpty()) {
            return false;
        }
        for (Element element : roundEnv.getRootElements()) {
            JCCompilationUnit unit = toUnit(element);
            if (unit == null) {
                continue;
            }
            unit.accept(new TreeTranslator() {
                @Override
                public void visitClassDef(JCTree.JCClassDecl jcClassDecl) {
                    super.visitClassDef(jcClassDecl);
                    if (jcClassDecl.sym != null) {
                        ASTUtils.COMPILATION_UNIT_MAP.put(jcClassDecl.sym.toString(), unit);
                        ASTUtils.CLASS_MAP.put(jcClassDecl.sym.toString(), jcClassDecl);
                    }
                }
            });
        }
        ASTUtils.getUnits().forEach(unit -> combiner.handle(context, unit));
        return false;
    }

    private JCCompilationUnit toUnit(Element element) {
        TreePath path = null;
        if (trees != null) {
            try {
                path = trees.getPath(element);
            } catch (NullPointerException ignore) {
            }
        }
        if (path == null) {
            return null;
        }
        return (JCCompilationUnit) path.getCompilationUnit();
    }

}
