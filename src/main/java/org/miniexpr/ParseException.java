package org.miniexpr;

public class ParseException extends Exception {
    public final int pos;

    // 可选的线程本地源码片段（由调用方设置，通常在解析/求值入口处设置）
    // 使用 ThreadLocal 以保证多线程并发时的安全
    private static final ThreadLocal<String> source = new ThreadLocal<>();

    public static void setSource(String src) { source.set(src); }
    public static void clearSource() { source.remove(); }

    public ParseException(String msg, int pos) {
        super(buildMessage(msg, pos));
        this.pos = pos;
    }

    private static String buildMessage(String msg, int pos) {
        // 对外显示使用 1-based 索引，更友好；内部仍保留 pos 为 0-based
        String base = msg + " at index " + (pos + 1);
        String sourceStr = source.get();
        if (sourceStr == null) return base;

        int len = sourceStr.length();

        // 保证 pos 在 [0, len] 范围内（允许 pos == len 表示 EOF，caret 在末尾之后）
        int p = Math.max(0, Math.min(pos, Math.max(0, len)));

        // 允许 caret 指向末尾之后的位置（p == len 表示 EOF）
        int left = Math.min(15, p);
        int right = Math.min(15, Math.max(0, len - p));
        int start = Math.max(0, p - left);
        int end = Math.min(len, p + right);

        String snippet = sourceStr.substring(start, end);
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