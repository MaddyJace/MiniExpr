package org.miniexpr.nodes;


import org.miniexpr.Node;

public class StringNode implements Node {
    private final String value;
    private final int pos;

    public StringNode(String value, int pos) { this.value = value; this.pos = pos; }

    @Override
    public Object eval() { return value; }

    @Override
    public int pos() { return pos; }

}
