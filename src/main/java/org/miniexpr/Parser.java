package org.miniexpr;

import java.util.List;

import org.miniexpr.nodes.BinaryNode;
import org.miniexpr.nodes.ConditionalNode;
import org.miniexpr.nodes.UnaryNode;

/**
 * 递归下降解析器（Recursive Descent Parser）。
 * <p>
 * 解析器基于从 Lexer 获取的 Token 列表进行
 * 递归下降式解析。语法大致如下（简化描述）：
 * <p>
 * conditional  := logicalOr ( '?' expression ':' expression )?
 * logicalOr    := logicalAnd ( '||' logicalAnd )*
 * logicalAnd   := equality ( '&&' equality )*
 * equality     := relational ( ('==' | '!=') relational )*
 * relational   := additive ( ('>' | '>=' | '<' | '<=') additive )*
 * additive     := multiplicative ( ('+' | '-') multiplicative )*
 * multiplicative := unary ( ('*' | '/' | '%') unary )*
 * unary        := ( '!' | '+' | '-' ) unary | primary
 * primary      := NUMBER | STRING | IDENTIFIER | '(' expression ')'
 * <p>
 * 解析过程会在构造对应的 AST 节点时传入操作符位置（op.pos），以便于在运行时
 * 抛出带有精确字符索引的 ParseException。
 */
public class Parser {
    private final List<Token> tokens;
    private int idx = 0;

    public Parser(Lexer lexer) throws ParseException {
        this.tokens = lexer.tokenize();
    }

    private Token peek() {
        return tokens.get(idx);
    }

    private Token next() {
        Token t = tokens.get(idx);
        if (t.type != TokenType.EOF) idx++;
        return t;
    }

    private boolean match(TokenType... types) {
        Token t = peek();
        for (TokenType ty : types) {
            if (t.type == ty) {
                next();
                return true;
            }
        }
        return false;
    }

    private Token consume(TokenType type, String errMsg) throws ParseException {
        Token t = peek();
        if (t.type == type) return next();
        throw new ParseException(errMsg, t.pos);
    }

    public Node parseExpression() throws ParseException {
        Node n = parseConditional();
        Token t = peek();
        if (t.type != TokenType.EOF) {
            throw new ParseException("Unexpected token after expression: " + t.text, t.pos);
        }
        return n;
    }

    // conditional: logicalOr ( '?' expression ':' expression )?
    private Node parseConditional() throws ParseException {
        Node cond = parseLogicalOr();
        if (match(TokenType.QUESTION)) {
            Node trueExpr = parseConditional(); // allow nested conditionals on true branch
            consume(TokenType.COLON, "Expected ':' in conditional expression");
            Node falseExpr = parseConditional();
            return new ConditionalNode(cond, trueExpr, falseExpr);
        }
        return cond;
    }

    // logicalOr: logicalAnd ( '||' logicalAnd )*
    private Node parseLogicalOr() throws ParseException {
        Node left = parseLogicalAnd();
        while (match(TokenType.OR)) {
            Token op = tokens.get(idx - 1);
            Node right = parseLogicalAnd();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // logicalAnd: equality ( '&&' equality )*
    private Node parseLogicalAnd() throws ParseException {
        Node left = parseEquality();
        while (match(TokenType.AND)) {
            Token op = tokens.get(idx - 1);
            Node right = parseEquality();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // equality: relational ( ('==' | '!=') relational )*
    private Node parseEquality() throws ParseException {
        Node left = parseRelational();
        while (match(TokenType.EQ, TokenType.NE)) {
            Token op = tokens.get(idx - 1);
            Node right = parseRelational();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // relational: additive ( ('>' | '>=' | '<' | '<=') additive )*
    private Node parseRelational() throws ParseException {
        Node left = parseAdditive();
        while (match(TokenType.GT, TokenType.GE, TokenType.LT, TokenType.LE)) {
            Token op = tokens.get(idx - 1);
            Node right = parseAdditive();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // additive: multiplicative ( ('+' | '-') multiplicative )*
    private Node parseAdditive() throws ParseException {
        Node left = parseMultiplicative();
        while (match(TokenType.PLUS, TokenType.MINUS)) {
            Token op = tokens.get(idx - 1);
            Node right = parseMultiplicative();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // multiplicative: unary ( ('*' | '/' | '%') unary )*
    private Node parseMultiplicative() throws ParseException {
        Node left = parseUnary();
        while (match(TokenType.STAR, TokenType.SLASH, TokenType.PERCENT)) {
            Token op = tokens.get(idx - 1);
            Node right = parseUnary();
            left = new BinaryNode(left, op.type, right, op.pos);
        }
        return left;
    }

    // unary: ( '!' | '+' | '-' ) unary | primary
    private Node parseUnary() throws ParseException {
        if (match(TokenType.NOT, TokenType.PLUS, TokenType.MINUS)) {
            Token op = tokens.get(idx - 1);
            Node operand = parseUnary();
            return new UnaryNode(op.type, operand, op.pos);
        }
        return parsePrimary();
    }

    // primary: NUMBER | STRING | '(' expression ')'
    private Node parsePrimary() throws ParseException {
        Token t = peek();
        if (match(TokenType.NUMBER)) {
            try {
                if (t.text.contains(".")) {
                    return new org.miniexpr.nodes.NumberNode(Double.parseDouble(t.text), t.pos); // 小数
                } else {
                    return new org.miniexpr.nodes.NumberNode(Long.parseLong(t.text), t.pos); // 整数
                }
            } catch (NumberFormatException ex) {
                throw new ParseException("Invalid number literal", t.pos);
            }
        }
        if (match(TokenType.STRING)) {
            return new org.miniexpr.nodes.StringNode(t.text, t.pos);
        }

        if (match(TokenType.IDENTIFIER)) {
            // 标识符直接作为字符串处理（无需引号），用于比较和三元运算
            return new org.miniexpr.nodes.StringNode(t.text, t.pos);
        }

        if (match(TokenType.LPAREN)) {
            Node inner = parseConditional();
            Token closing = peek();
            if (!match(TokenType.RPAREN)) {
                throw new ParseException("Expected ')' to close '('", closing.pos);
            }
            return inner;
        }
        throw new ParseException("Unexpected token: " + t.text, t.pos);
    }
}
