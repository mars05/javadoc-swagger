package com.github.mars05.jts.util;

import com.sun.tools.javac.tree.JCTree;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * ASTUtils
 *
 * @author yu.xiao
 */
public class ASTUtils {
    public static final Map<String, JCTree.JCCompilationUnit> COMPILATION_UNIT_MAP = new HashMap<>();
    public static final Map<String, JCTree.JCClassDecl> CLASS_MAP = new HashMap<>();

    public static Set<JCTree.JCCompilationUnit> getUnits() {
        return new HashSet<>(COMPILATION_UNIT_MAP.values());
    }
}
