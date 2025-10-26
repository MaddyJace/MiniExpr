package org.miniexpr;

public interface Node {
    Object eval() throws ParseException;

    /** 返回该节点在原始输入字符串中的起始字符索引（用于错误定位） */
    int pos();
}
