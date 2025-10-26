package org.miniexpr;

public class ParseException extends Exception {
    public final int pos;

    // 可选的全局源码片段（由调用方设置，通常在解析/求值入口处设置）
    private static String source = null;

    public static void setSource(String src) { source = src; }
    public static void clearSource() { source = null; }

    public ParseException(String msg, int pos) {
        super(buildMessage(msg, pos));
        this.pos = pos;
    }

    private static String buildMessage(String msg, int pos) {
        String base = msg + " at index " + pos;
        if (source == null) return base;

        int len = source.length();
        // 保证 pos 在 [0, len-1] 范围内
        int p = Math.max(0, Math.min(pos, Math.max(0, len)));

        // 允许 caret 指向末尾之后的位置（p == len 表示 EOF）
        int left = Math.min(15, p);
        int right = Math.min(15, Math.max(0, len - p));
        int start = Math.max(0, p - left);
        int end = Math.min(len, p + right);

        String snippet = source.substring(start, end);
        int caretIndex = p - start;

        StringBuilder sb = new StringBuilder();
        sb.append(base).append('\n');
        // 当左边被截断时显示省略号
        if (start > 0) sb.append("...");
        sb.append(snippet);
        if (end < len) sb.append("...");
        sb.append('\n');
        for (int i = 0; i < caretIndex; i++) sb.append(' ');
        sb.append('^');
        return sb.toString();
    }
}

