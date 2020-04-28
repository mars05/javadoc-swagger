package com.github.mars05.jts.handler;

import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * 源码注释文档
 *
 * @author yu.xiao
 */
@Data
@Accessors(chain = true)
public class Comment {
    private String desc;
    private List<String> authors;
    private List<Param> params;
    private String returnDesc;
    private String version;
    private String since;
    private String date;

    public Param getParam(String paramName) {
        if (params != null) {
            for (Param param : params) {
                if (StringUtils.equals(param.getParamName(), paramName)) {
                    return param;
                }
            }
        }
        return null;
    }

    @Data
    public static class Param {
        private String paramName;
        private String paramDesc;
    }
}
