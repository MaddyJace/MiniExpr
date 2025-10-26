package org.miniexpr.nodes;

import org.miniexpr.Node;
import org.miniexpr.ParseException;
import org.miniexpr.TokenType;

/**
 * 一元运算： !, +, -
 */
public class UnaryNode implements Node {
    private final TokenType op;
    private final Node operand;
    private final int pos;

    public UnaryNode(TokenType op, Node operand, int pos) {
        this.op = op;
        this.operand = operand;
        this.pos = pos;
    }

    @Override
    public Object eval() throws ParseException {
        Object v = operand.eval();
        switch (op) {
            case NOT:
                return !toBool(v);
            case PLUS:
                if (v instanceof Number) {
                    // +x 返回 x（保持整数/浮点）
                    return v;
                }
                throw new ParseException("Unary + requires numeric operand", pos);
            case MINUS:
                if (v instanceof Number) {
                    Number n = (Number) v;
                    if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) {
                        return Long.valueOf(-n.longValue());
                    } else {
                        return Double.valueOf(-n.doubleValue());
                    }
                }
                throw new ParseException("Unary - requires numeric operand", pos);
            default:
                throw new ParseException("Unknown unary operator " + op, pos);
        }
    }

    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return ((Number) o).doubleValue() != 0.0;
        if (o instanceof String) return !((String) o).isEmpty();
        return o != null;
    }

    @Override
    public int pos() { return pos; }
}
