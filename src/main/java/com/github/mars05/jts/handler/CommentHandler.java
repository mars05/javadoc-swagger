package com.github.mars05.jts.handler;

import com.sun.tools.javac.tree.DCTree;
import com.sun.tools.javac.tree.DCTree.DCDocComment;
import com.sun.tools.javac.tree.DCTree.DCText;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 源码注释文档处理器
 *
 * @author yu.xiao
 */
public abstract class CommentHandler<T extends JCTree> extends Handler<T> {
    public CommentHandler(Context context, JCCompilationUnit unit) {
        super(context, unit);
    }

    @Override
    public void handleAST(T tree) {
        if (null == tree || !isNeedHandle(tree)
                || !getUnit().docComments.hasComment(tree)) {
            return;
        }
        DCDocComment commentTree = getUnit().docComments.getCommentTree(tree);
        Comment comment = new Comment();
        comment.setDesc(extractDesc(commentTree));
        comment.setParams(extractParams(commentTree));
        this.handleComment(comment, tree);
    }

    private String extractDesc(DCDocComment commentTree) {
        List<DCTree> firstSentence = commentTree.firstSentence;
        if (firstSentence != null && firstSentence.size() > 0) {
            DCTree dcTree = firstSentence.get(0);
            if (dcTree instanceof DCText) {
                return ((DCText) dcTree).text;
            }
        }
        return "";
    }

    private List<Comment.Param> extractParams(DCDocComment commentTree) {
        List<Comment.Param> params = new ArrayList<>();
        List<DCTree> tags = commentTree.tags;
        if (tags != null) {
            for (DCTree tag : tags) {
                if (tag instanceof DCTree.DCParam) {
                    DCTree.DCParam dcParam = (DCTree.DCParam) tag;
                    Comment.Param param = new Comment.Param();
                    param.setParamName(String.valueOf(dcParam.getName()));
                    List<DCTree> description = dcParam.description;
                    if (description == null || description.size() <= 0) {
                        continue;
                    }
                    DCTree dcTree = description.get(0);
                    if (dcTree instanceof DCText) {
                        DCText desc = (DCText) dcTree;
                        param.setParamDesc(desc.text);
                    }
                    if (StringUtils.isNotBlank(param.getParamName())
                            && StringUtils.isNotBlank(param.getParamDesc())) {
                        params.add(param);
                    }
                }
            }
        }
        return params;
    }

    /**
     * handleComment
     *
     * @param comment comment
     * @param tree    被注释的ast节点
     */
    protected abstract void handleComment(Comment comment, T tree);
}
