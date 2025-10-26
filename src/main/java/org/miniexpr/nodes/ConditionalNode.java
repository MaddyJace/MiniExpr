package org.miniexpr.nodes;

import org.miniexpr.Node;
import org.miniexpr.ParseException;

/**
 * 三元运算节点： condition ? trueExpr : falseExpr
 */
public class ConditionalNode implements Node {
    private final Node cond, trueExpr, falseExpr;

    public ConditionalNode(Node cond, Node trueExpr, Node falseExpr) {
        this.cond = cond;
        this.trueExpr = trueExpr;
        this.falseExpr = falseExpr;
    }

    @Override
    public Object eval() throws ParseException {
        Object c = cond.eval();
        boolean cb = toBool(c);
        return cb ? trueExpr.eval() : falseExpr.eval();
    }

    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0.0;
        if (o instanceof String) return !((String) o).isEmpty();
        return o != null;
    }

    @Override
    public int pos() { return cond.pos(); }
}
