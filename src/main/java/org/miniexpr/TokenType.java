package org.miniexpr;

public enum TokenType {
    IDENTIFIER, NUMBER, STRING,
    PLUS, MINUS, STAR, SLASH, PERCENT,
    GT, GE, LT, LE, EQ, NE,
    AND, OR, NOT,
    QUESTION, COLON,
    LPAREN, RPAREN,
    EOF
}
