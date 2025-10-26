package org.miniexpr.nodes;

import org.miniexpr.Node;

public class NumberNode implements Node {
    private final Object value; // 可以是 Long 或 Double
    private final int pos;

    public NumberNode(Object value, int pos) { this.value = value; this.pos = pos; }

    @Override
    public Object eval() { return value; }

    @Override
    public int pos() { return pos; }

}
