package org.miniexpr.nodes;

import org.miniexpr.Node;
import org.miniexpr.ParseException;
import org.miniexpr.TokenType;

/**
 * 二元运算节点：算术 / 比较 / 逻辑 / 字符串拼接（+）
 * opPos 用于错误位置信息（从 Parser 传入 token.pos）
 */
public class BinaryNode implements Node {
    private final Node left, right;
    private final TokenType op;
    private final int opPos;

    public BinaryNode(Node left, TokenType op, Node right, int opPos) {
        this.left = left;
        this.op = op;
        this.right = right;
        this.opPos = opPos;
    }

    @Override
    public Object eval() throws ParseException {
        Object l = left.eval();
        Object r = right.eval();

        switch (op) {
            // 算术
            case PLUS:
                if (l instanceof Number && r instanceof Number) {
                    return numericOp((Number) l, (Number) r, (a, b) -> a + b);
                }
                // 如果任意一方是字符串，做字符串拼接
                if (l instanceof String || r instanceof String) {
                    return String.valueOf(l) + String.valueOf(r);
                }
                // 如果一方是数字另一方是非字符串（例如标识符被解析成 String），也当作字符串拼接
                if (l != null && r != null) {
                    return String.valueOf(l) + String.valueOf(r);
                }
                throw new ParseException("Operator '+' requires numbers or strings", opPos);

            case MINUS:
                return numericOpCheck(l, r, (a, b) -> a - b);
            case STAR:
                return numericOpCheck(l, r, (a, b) -> a * b);
            case SLASH:
                return numericOpCheck(l, r, (a, b) -> {
                    if (b == 0.0) throw new ArithmeticException("Division by zero");
                    return a / b;
                });
            case PERCENT:
                return numericOpCheck(l, r, (a, b) -> a % b);

            // 比较
            case GT:
                return compareNumeric(l, r, (a, b) -> a > b);
            case GE:
                return compareNumeric(l, r, (a, b) -> a >= b);
            case LT:
                return compareNumeric(l, r, (a, b) -> a < b);
            case LE:
                return compareNumeric(l, r, (a, b) -> a <= b);
            case EQ:
                return equalsOp(l, r);
            case NE:
                return !((Boolean) equalsOp(l, r));

            // 逻辑
            case AND:
                return toBool(l) && toBool(r);
            case OR:
                return toBool(l) || toBool(r);

            default:
                throw new ParseException("Unsupported binary operator: " + op, opPos);
        }
    }



    // 辅助：尝试把任意对象当 Number 并计算，若不是 Number 则抛错
    private Object numericOpCheck(Object l, Object r, DoubleBinary opFunc) throws ParseException {
        if (!(l instanceof Number) || !(r instanceof Number)) {
            throw new ParseException("Numeric operator requires numeric operands", opPos);
        }
        return numericOp((Number) l, (Number) r, opFunc);
    }

    // 真正的数值运算：返回 Long（如果输入都是整数并且结果为整数），否则返回 Double
    private Object numericOp(Number lnum, Number rnum, DoubleBinary opFunc) throws ParseException {
        double la = toDouble(lnum);
        double ra = toDouble(rnum);
        double res;
        try {
            res = opFunc.apply(la, ra);
        } catch (ArithmeticException ae) {
            throw new ParseException(ae.getMessage(), opPos);
        }

        // --- 自动提升精度逻辑 ---
        boolean lFloat = (lnum instanceof Float || lnum instanceof Double);
        boolean rFloat = (rnum instanceof Float || rnum instanceof Double);

        // 如果任意一边是浮点数，则强制返回 Double
        if (lFloat || rFloat) {
            return Double.valueOf(res);
        }

        boolean lInt = isIntegral(lnum);
        boolean rInt = isIntegral(rnum);
        boolean resInt = isIntegral(res);

        if (lInt && rInt && resInt) {
            return Long.valueOf((long) res);
        } else {
            return Double.valueOf(res);
        }
    }

    private Boolean compareNumeric(Object l, Object r, DoubleCompare cmp) throws ParseException {
        if (l instanceof Number && r instanceof Number) {
            return cmp.compare(toDouble((Number) l), toDouble((Number) r));
        }
        // 使用右操作数的起始位置来指示报错字符（比如标识符的位置），若右节点没有位置信息则回退到运算符位置
        int errPos = opPos;
        if (right instanceof Node) {
            try {
                errPos = ((Node) right).pos();
            } catch (Throwable ignored) {}
        }
        throw new ParseException("Relational operators require numeric operands", errPos);
    }

    private Boolean equalsOp(Object l, Object r) {
        // 两边都是数字 -> 数值比较
        if (l instanceof Number && r instanceof Number) {
            double dl = toDouble((Number) l);
            double dr = toDouble((Number) r);
            return Double.compare(dl, dr) == 0;
        }
        // 其他情况：按字符串表示比较（保持宽松规则）
        return String.valueOf(l).equals(String.valueOf(r));
    }

    private boolean toBool(Object o) {
        if (o instanceof Boolean) return (Boolean) o;
        if (o instanceof Number) return !isZero((Number) o);
        if (o instanceof String) return !((String) o).isEmpty();
        return o != null;
    }

    private double toDouble(Number n) {
        return n.doubleValue();
    }

    private boolean isIntegral(Number n) {
        // 如果是 Long/Integer/Short -> true
        if (n instanceof Long || n instanceof Integer || n instanceof Short || n instanceof Byte) return true;
        // 如果是 Double/Float -> 检查是否为整数值
        double d = n.doubleValue();
        return Double.isFinite(d) && d == Math.floor(d);
    }

    private boolean isIntegral(double d) {
        return Double.isFinite(d) && d == Math.floor(d);
    }

    private boolean isZero(Number n) {
        return n.doubleValue() == 0.0;
    }

    private interface DoubleBinary { Double apply(Double a, Double b); }
    private interface DoubleCompare { boolean compare(Double a, Double b); }

    @Override
    public int pos() { return opPos; }
}
