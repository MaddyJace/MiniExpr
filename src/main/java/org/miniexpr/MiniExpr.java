package org.miniexpr;

import java.util.Map;

public class MiniExpr {

    private static final ThreadLocal<Map<String, Object>> context = new ThreadLocal<>();

    public static void setContext(Map<String, Object> ctx) { context.set(ctx); }
    public static void clearContext() { context.remove(); }
    public static Map<String, Object> getContext() { return context.get(); }

    /**
     * 入口方法：计算表达式（无变量上下文）
     */
    public static Object eval(String expr) throws ParseException {
        return eval(expr, null);
    }

    /**
     * 入口方法：计算表达式，提供变量上下文
     */
    public static Object eval(String expr, Map<String, Object> vars) throws ParseException {
        try {
            ParseException.setSource(expr);
            setContext(vars);
            Lexer lexer = new Lexer(expr);
            Parser parser = new Parser(lexer);
            Node node = parser.parseExpression();
            return node.eval();
        } finally {
            ParseException.clearSource();
            clearContext();
        }
    }

    public static void main(String[] args) {
        try {
            MiniExpr.eval("1 >= 0 && yes == 'yes'");
            MiniExpr.eval("1 + 1 >= 2 ? yes : no");
            MiniExpr.eval("(1 + 4) * 5 >= (100 * 100) ? '(1 + 4) * 5 = ' + (1 + 4) * 5 : no");
            MiniExpr.eval("(1 + 4) * 5 <= (100 * 1000) ? 'yes: (1 + 4) * 5 = ' + (1 + 4) * 5: no");
        } catch (ParseException e) {
            System.out.println(e.getMessage
                    ());
        }
    }
}
