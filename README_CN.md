# MiniExpr 项目说明（中文）

## 概述

MiniExpr 是一个小型表达式解析与求值器，采用递归下降解析器实现。支持基本的算术、比较、逻辑运算、三元运算符以及字符串拼接。整体架构清晰，适合嵌入到其他 Java 项目中作为轻量表达式引擎。

## 项目结构

- `src/main/java/org/miniexpr/`
  - `Lexer.java`：词法分析器，将输入字符串拆分为 Token。
  - `Token.java`、`TokenType.java`：Token 数据结构与类型枚举。
  - `Parser.java`：递归下降解析器，将 Token 列表构建为 AST（节点）。
  - `Node.java`：AST 节点接口，所有节点实现 `eval()` 和 `pos()`。
  - `ParseException.java`：自定义异常，支持在异常消息中显示源码上下文并使用 `^` 指示出错列。
  - `MiniExpr.java`：入口，提供 `eval(String expr)` 方法来解析并计算表达式。
  - `nodes/` 包：具体 AST 节点实现
    - `NumberNode.java`：数字字面量节点（Long 或 Double）。
    - `StringNode.java`：字符串/标识符节点。
    - `UnaryNode.java`：一元运算节点（!, +, -）。
    - `BinaryNode.java`：二元运算节点（算术 / 比较 / 逻辑 / 字符串拼接）。
    - `ConditionalNode.java`：三元运算节点（? :）。

生成产物位于 `target/classes`（由编译产出），本仓库将源码放在 `src/main/java`。

## 设计与实现原理

1. 词法分析（Lexer）

   - `Lexer` 按字符遍历输入字符串，把连续的数字识别为 `NUMBER`（支持小数点），双引号包围的文本识别为 `STRING`，字母或下划线开头的连续字母/数字/下划线识别为 `IDENTIFIER`。
   - 运算符和分隔符被识别为相应的 `TokenType`（支持双字符运算符如 `==`, `!=`, `&&`, `||`, `>=`, `<=`）。
   - 每个 `Token` 包含 `type`、`text` 与 `pos`（起始字符索引，0-based），以便后续解析器和错误定位使用。

2. 解析（Parser）

   - 使用递归下降的方式实现语法规则（见 `Parser` 顶部注释）。
   - 每遇到一个操作符，会把该操作符的 `pos` 传给构造产生的 AST 节点（例如 `BinaryNode` 的 `opPos`），这样在运行时发生错误可以准确报告错误位置。
   - 解析器在解析完成后会检查是否已到达 `EOF`，若后面还存在未消费的 token，则抛出 `ParseException`。

3. 抽象语法树（AST）与求值

   - `Node` 接口定义 `Object eval() throws ParseException` 和 `int pos()`。
   - 常见节点：
     - `NumberNode`：包装 Long 或 Double，直接返回数值。
     - `StringNode`：用于表示字符串字面量或标识符（当前实现把标识符当作字符串常量）。
     - `UnaryNode`：一元运算符，遇到类型不匹配时抛出 `ParseException`，异常位置为运算符位置。
     - `BinaryNode`：实现算术、比较、逻辑运算与字符串拼接。比较运算要求数值操作数，否则会抛出 `ParseException`；在比较报错时，会优先使用右操作数的 `pos()` 来定位错误字符（如果右节点提供了位置信息），否则回退到运算符位置。
     - `ConditionalNode`：三元运算，按条件求值（当前没有短路求值的额外优化，求值时会完整计算分支）。

4. 错误定位与异常信息

   - `ParseException` 支持一个静态的 `source`（由 `MiniExpr.eval` 在入口设置），当抛出异常时会把源字符串中出错位置附近的最多左右 15 个字符提取出来，并用 `^` 指向错误索引，从而生成易读的错误上下文片段。例如：

     Relational operators require numeric operands at index 14
     1 > 0 && 10 > a000
                   ^

   - 此外对截断的两端会加省略号 `...`，并且现在支持 pos 指向字符串末尾（EOF），caret 可指向末尾后的插入点。

## 使用示例

在代码中直接调用：

```java
Object result = MiniExpr.eval("1 + 2 * 3"); // 返回数值 7 或 7.0（取决于数字类型）
```

若表达式存在类型错误或语法错误，会抛出 `ParseException`，异常消息包含出错上下文与 `^` 指示。

## 扩展建议

- 标识符当前被视为字符串常量；可扩展为变量绑定（提供一个上下文 map），在 `StringNode` 中查找变量值。
- 支持转义字符串（`\"`, `\\`, `\n` 等）。
- 改进短路求值（尤其是 `&&` 和 `||`）以避免不必要的求值和潜在的运行时错误。
- 将 `ParseException` 的 `source` 改为线程安全（如 `ThreadLocal`）或通过构造函数传递，避免并发问题。
- 增加单元测试覆盖解析、求值与错误定位场景。

## 文件地图（快速参考）

- `Lexer.java` — 词法分析与 token 生成
- `Parser.java` — 递归下降解析器，构建 AST
- `Node.java` — AST 节点接口
- `nodes/*.java` — AST 节点实现（求值逻辑）
- `ParseException.java` — 格式化错误上下文并显示 caret
- `MiniExpr.java` — 入口，设置 `source` 并触发解析/求值

---

如果你需要我把注释改为英文 Javadoc，或把 README_CN.md 转为 README.md（英文），或为每个类生成更详细的 API 文档（例如用 javadoc 生成 HTML），我可以继续处理。
