package org.miniexpr;

import java.util.ArrayList;
import java.util.List;

/**
 * Lexer (词法分析器)
 * <p>
 * 负责把输入的表达式字符串分解为一系列 Token。每个 Token 带有类型、文本和
 * 在原始输入中的起始字符索引（pos），便于后续解析器与错误定位。
 * <p>
 * 支持的词法元素：数字（整数/浮点）、字符串字面量（双引号）、标识符、运算符与分隔符。
 */
public class Lexer {
    private final String input;
    private int pos = 0;

    public Lexer(String input) {
        this.input = input;
    }

    // 返回当前字符或 '\0' 表示结束
    private char peek() {
        return pos < input.length() ? input.charAt(pos) : '\0';
    }

    // 前进一个字符
    private void next() {
        pos++;
    }

    /**
     * 把整个输入 tokenize 为 Token 列表，最后会追加一个 EOF Token。
     */
    public List<Token> tokenize() throws ParseException {
        List<Token> tokens = new ArrayList<>();
        while (true) {
            char ch = peek();
            if (ch == '\0') {
                tokens.add(new Token(TokenType.EOF, "", pos));
                break;
            } else if (Character.isWhitespace(ch)) {
                next();
            } else if (Character.isDigit(ch)) {
                tokens.add(readNumber());
            } else if (ch == '"' || ch == '\'') {
                tokens.add(readString());
            } else if (Character.isLetter(ch) || ch == '_') {
                tokens.add(readIdentifier());
            } else {
                tokens.add(readSymbol());
            }
        }
        return tokens;
    }

    // 读取标识符或关键字（本项目将标识符作为字符串值）
    private Token readIdentifier() {
        int start = pos;
        while (Character.isLetterOrDigit(peek()) || peek() == '_') {
            next();
        }
        return new Token(TokenType.IDENTIFIER, input.substring(start, pos), start);
    }

    // 读取数字（可能包含一个小数点）
    private Token readNumber() {
        int start = pos;
        boolean hasDot = false;
        while (Character.isDigit(peek()) || peek() == '.') {
            if (peek() == '.') {
                if (hasDot) break; // 避免 "1.2.3"
                hasDot = true;
            }
            next();
        }
        String text = input.substring(start, pos);
        return new Token(TokenType.NUMBER, text, start);
    }

    // 读取双引号字符串字面量，不支持转义（简单实现）
    private Token readString() throws ParseException {
        int start = pos;
        char quote = peek();
        next(); // skip opening quote
        StringBuilder sb = new StringBuilder();
        while (peek() != quote && peek() != '\0') {
            sb.append(peek());
            next();
        }
        if (peek() != quote)
            throw new ParseException("Unterminated string literal", start);
        next(); // skip ending quote
        return new Token(TokenType.STRING, sb.toString(), start);
    }

    // 读取运算符或单字符符号，支持双字符运算符如 "==", "!=", "&&", "||", ">=", "<=" 等
    private Token readSymbol() throws ParseException {
        int start = pos;
        char ch = peek();
        next();

        switch (ch) {
            case '+': return new Token(TokenType.PLUS, "+", start);
            case '-': return new Token(TokenType.MINUS, "-", start);
            case '*': return new Token(TokenType.STAR, "*", start);
            case '/': return new Token(TokenType.SLASH, "/", start);
            case '%': return new Token(TokenType.PERCENT, "%", start);
            case '(': return new Token(TokenType.LPAREN, "(", start);
            case ')': return new Token(TokenType.RPAREN, ")", start);
            case '?': return new Token(TokenType.QUESTION, "?", start);
            case ':': return new Token(TokenType.COLON, ":", start);
            case '!':
                if (peek() == '=') { next(); return new Token(TokenType.NE, "!=", start); }
                return new Token(TokenType.NOT, "!", start);
            case '=':
                if (peek() == '=') { next(); return new Token(TokenType.EQ, "==", start); }
                break;
            case '>':
                if (peek() == '=') { next(); return new Token(TokenType.GE, ">=", start); }
                return new Token(TokenType.GT, ">", start);
            case '<':
                if (peek() == '=') { next(); return new Token(TokenType.LE, "<=", start); }
                return new Token(TokenType.LT, "<", start);
            case '&':
                if (peek() == '&') { next(); return new Token(TokenType.AND, "&&", start); }
                break;
            case '|':
                if (peek() == '|') { next(); return new Token(TokenType.OR, "||", start); }
                break;
        }
        throw new ParseException("Unexpected symbol: " + ch, start);
    }
}
