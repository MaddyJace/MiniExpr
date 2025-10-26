package org.miniexpr;

public class MiniExpr {

    /**
     * 入口方法：计算表达式
     */
    public static Object eval(String expr) throws ParseException {
        try {
            ParseException.setSource(expr);
            Lexer lexer = new Lexer(expr);
            Parser parser = new Parser(lexer);
            Node node = parser.parseExpression();
            return node.eval();
        } finally {
            ParseException.clearSource();
        }
    }

    public static void main(String[] args) {
        try {
//            System.out.println(MiniExpr.eval("1 + 2 * 3")); // 7.0
//            System.out.println(MiniExpr.eval("(3 + 5) * 2 > 10 ? yes : no")); // yes
//            System.out.println(MiniExpr.eval("a + b")); // ab
//            System.out.println(MiniExpr.eval("3 == 3.0")); // true
//            System.out.println(MiniExpr.eval("x == x && 1 > 0")); // true
            System.out.println(MiniExpr.eval("1 > 0 & 10 > 1")); // false (short-circuit isn't implemented — full evaluation)
        } catch (ParseException e) {
            System.out.println(e.getMessage());
        }
    }
}
