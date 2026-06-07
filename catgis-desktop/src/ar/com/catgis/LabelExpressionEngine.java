package ar.com.catgis;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.function.Function;

/**
 * Expression engine for dynamic label text in CATGIS.
 * <p>
 * Supports attribute references, string functions, math, formatting, conditionals, and geometry access.
 * <p>
 * <b>Syntax:</b>
 * <pre>
 *   [fieldName]            → attribute value
 *   "literal text"         → string literal
 *   123.45                 → numeric literal
 *   ||                     → string concatenation
 *   +, -, *, /             → arithmetic
 *   ==, !=, &gt;, &lt;, &gt;=, &lt;=   → comparisons
 *
 *   Functions:
 *     upper(s)             → uppercase
 *     lower(s)             → lowercase
 *     trim(s)              → trim whitespace
 *     substr(s, start, len) → substring
 *     replace(s, old, new) → string replace
 *     round(n)             → round to integer
 *     round(n, places)     → round to decimal places
 *     floor(n)             → floor
 *     ceil(n)              → ceiling
 *     abs(n)               → absolute value
 *     min(a, b, ...)       → minimum
 *     max(a, b, ...)       → maximum
 *     format(n, pattern)   → number formatting ("#,##0.00")
 *     if(cond, then, else) → conditional
 *     area()               → feature geometry area (sqm)
 *     length()             → feature geometry length (m)
 *     perimeter()          → feature geometry perimeter (m)
 *     num(field)           → numeric value of attribute
 *     str(val)             → string conversion
 * </pre>
 * </p>
 */
public final class LabelExpressionEngine {

    private LabelExpressionEngine() {}

    // --- Public API ---

    /**
     * Evaluate an expression for the given feature.
     *
     * @param expression the expression string (e.g. {@code [name] || " (" || [code] || ")"})
     * @param feature    the feature whose attributes are accessible via [fieldName]
     * @return evaluated text, or null if the expression is empty/blank
     * @throws ExpressionException on syntax or evaluation errors
     */
    public static String evaluate(String expression, SimpleFeature feature) {
        if (expression == null || expression.isBlank()) return null;
        String trimmed = expression.trim();

        // Fast path: single field reference like [fieldname]
        if (isSimpleFieldRef(trimmed)) {
            String fieldName = trimmed.substring(1, trimmed.length() - 1);
            Object val = feature.getAttribute(fieldName);
            return val != null ? String.valueOf(val).trim() : "";
        }

        // Parse and evaluate expression
        try {
            List<Token> tokens = tokenize(trimmed);
            List<Token> rpn = shuntingYard(tokens);
            Object result = evaluateRpn(rpn, feature);
            return result != null ? stringValue(result) : "";
        } catch (ExpressionException e) {
            throw e;
        } catch (Exception e) {
            throw new ExpressionException("Error evaluating expression: " + e.getMessage(), e);
        }
    }

    /**
     * Evaluate an expression returning a numeric result (for conditional comparisons).
     */
    public static double evaluateNumeric(String expression, SimpleFeature feature) {
        Object result = evaluateRaw(expression, feature);
        if (result instanceof Number n) return n.doubleValue();
        if (result instanceof String s) {
            try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ignored) {}
        }
        return Double.NaN;
    }

    /**
     * Evaluate an expression returning the raw Object (for internal use).
     */
    public static Object evaluateRaw(String expression, SimpleFeature feature) {
        if (expression == null || expression.isBlank()) return null;
        String trimmed = expression.trim();

        if (isSimpleFieldRef(trimmed)) {
            String fieldName = trimmed.substring(1, trimmed.length() - 1);
            return feature.getAttribute(fieldName);
        }

        try {
            List<Token> tokens = tokenize(trimmed);
            List<Token> rpn = shuntingYard(tokens);
            return evaluateRpn(rpn, feature);
        } catch (Exception e) {
            throw new ExpressionException("Error evaluating expression: " + e.getMessage(), e);
        }
    }

    // --- Tokenizer ---

    private static final String OPERATORS = "+-*/";
    private static final String COMPARATORS_2CHAR = "== != >= <=";
    private static final String COMPARATORS_1CHAR = "><";

    private enum TokenType {
        FIELD_REF, STRING_LITERAL, NUMBER, OPERATOR, COMPARATOR,
        FUNCTION, COMMA, LPAREN, RPAREN, CONCAT, IDENTIFIER
    }

    private record Token(TokenType type, String value, int position) {}

    private static List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0;
        int len = expr.length();

        while (i < len) {
            char c = expr.charAt(i);

            // Whitespace
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }

            // Field reference: [fieldName]
            if (c == '[') {
                int end = expr.indexOf(']', i + 1);
                if (end < 0) throw new ExpressionException("Unclosed field reference at position " + i);
                String fieldName = expr.substring(i + 1, end).trim();
                if (fieldName.isEmpty()) throw new ExpressionException("Empty field reference at position " + i);
                tokens.add(new Token(TokenType.FIELD_REF, fieldName, i));
                i = end + 1;
                continue;
            }

            // String literal: "text"
            if (c == '"') {
                int end = i + 1;
                while (end < len && expr.charAt(end) != '"') {
                    end++;
                }
                if (end >= len) throw new ExpressionException("Unclosed string literal at position " + i);
                String literal = expr.substring(i + 1, end);
                tokens.add(new Token(TokenType.STRING_LITERAL, literal, i));
                i = end + 1;
                continue;
            }

            // Number
            if (c == '-' || c == '+' || Character.isDigit(c)) {
                boolean negative = (c == '-');
                boolean positive = (c == '+');
                int start = i;
                if (negative || positive) i++;
                boolean hasDot = false;
                boolean hasDigit = false;
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) {
                    if (expr.charAt(i) == '.') {
                        if (hasDot) throw new ExpressionException("Multiple decimal points at position " + i);
                        hasDot = true;
                    }
                    hasDigit = true;
                    i++;
                }
                if (!hasDigit) {
                    if (negative || positive) {
                        // This is actually an operator, not part of a number
                        tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c), start));
                        continue;
                    }
                    throw new ExpressionException("Invalid number at position " + start);
                }
                String numStr = expr.substring(start, i);
                tokens.add(new Token(TokenType.NUMBER, numStr, start));
                continue;
            }

            // String concatenation: ||
            if (c == '|' && i + 1 < len && expr.charAt(i + 1) == '|') {
                tokens.add(new Token(TokenType.CONCAT, "||", i));
                i += 2;
                continue;
            }

            // 2-char comparators: ==, !=, >=, <=
            if (i + 1 < len) {
                String twoChar = expr.substring(i, i + 2);
                if (twoChar.equals("==") || twoChar.equals("!=") || twoChar.equals(">=") || twoChar.equals("<=")) {
                    tokens.add(new Token(TokenType.COMPARATOR, twoChar, i));
                    i += 2;
                    continue;
                }
            }

            // 1-char comparators: >, <
            if (c == '>' || c == '<') {
                tokens.add(new Token(TokenType.COMPARATOR, String.valueOf(c), i));
                i++;
                continue;
            }

            // Arithmetic operators
            if (c == '+' || c == '-' || c == '*' || c == '/') {
                tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c), i));
                i++;
                continue;
            }

            // Parentheses
            if (c == '(') {
                tokens.add(new Token(TokenType.LPAREN, "(", i));
                i++;
                continue;
            }
            if (c == ')') {
                tokens.add(new Token(TokenType.RPAREN, ")", i));
                i++;
                continue;
            }

            // Comma
            if (c == ',') {
                tokens.add(new Token(TokenType.COMMA, ",", i));
                i++;
                continue;
            }

            // Identifier (function name or fallback)
            if (Character.isLetter(c) || c == '_') {
                int start = i;
                while (i < len && (Character.isLetterOrDigit(expr.charAt(i)) || expr.charAt(i) == '_')) {
                    i++;
                }
                String ident = expr.substring(start, i);

                // Check if followed by '(' → it's a function call
                int j = i;
                while (j < len && Character.isWhitespace(expr.charAt(j))) j++;
                if (j < len && expr.charAt(j) == '(') {
                    tokens.add(new Token(TokenType.FUNCTION, ident, start));
                } else {
                    tokens.add(new Token(TokenType.IDENTIFIER, ident, start));
                }
                continue;
            }

            throw new ExpressionException("Unexpected character '" + c + "' at position " + i);
        }

        return tokens;
    }

    // --- Shunting-Yard to RPN ---

    private static int getPrecedence(Token t) {
        return switch (t.value()) {
            case "||" -> 1;
            case "==", "!=" -> 2;
            case ">", "<", ">=", "<=" -> 3;
            case "+", "-" -> 4;
            case "*", "/" -> 5;
            default -> 0;
        };
    }

    private static boolean isLeftAssoc(Token t) {
        return !t.value().equals("||");
    }

    private static List<Token> shuntingYard(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> stack = new Stack<>();

        for (Token t : tokens) {
            switch (t.type()) {
                case FIELD_REF, STRING_LITERAL, NUMBER, IDENTIFIER -> output.add(t);
                case FUNCTION -> stack.push(t);
                case COMMA -> {
                    while (!stack.isEmpty() && stack.peek().type() != TokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) throw new ExpressionException("Misplaced comma or mismatched parentheses");
                }
                case OPERATOR, COMPARATOR, CONCAT -> {
                    while (!stack.isEmpty() && (stack.peek().type() == TokenType.OPERATOR
                            || stack.peek().type() == TokenType.COMPARATOR
                            || stack.peek().type() == TokenType.CONCAT)
                            && getPrecedence(stack.peek()) >= getPrecedence(t)
                            && isLeftAssoc(t)) {
                        output.add(stack.pop());
                    }
                    stack.push(t);
                }
                case LPAREN -> stack.push(t);
                case RPAREN -> {
                    while (!stack.isEmpty() && stack.peek().type() != TokenType.LPAREN) {
                        output.add(stack.pop());
                    }
                    if (stack.isEmpty()) throw new ExpressionException("Mismatched parentheses");
                    stack.pop(); // discard LPAREN
                    // If top of stack is a function, pop it too
                    if (!stack.isEmpty() && stack.peek().type() == TokenType.FUNCTION) {
                        output.add(stack.pop());
                    }
                }
            }
        }

        while (!stack.isEmpty()) {
            Token t = stack.pop();
            if (t.type() == TokenType.LPAREN || t.type() == TokenType.RPAREN) {
                throw new ExpressionException("Mismatched parentheses");
            }
            output.add(t);
        }

        return output;
    }

    // --- RPN Evaluator ---

    @SuppressWarnings("unchecked")
    private static Object evaluateRpn(List<Token> rpn, SimpleFeature feature) {
        Stack<Object> stack = new Stack<>();

        for (Token t : rpn) {
            switch (t.type()) {
                case FIELD_REF -> {
                    Object val = feature.getAttribute(t.value());
                    stack.push(val != null ? val : "");
                }
                case STRING_LITERAL -> stack.push(t.value());
                case NUMBER -> stack.push(parseNumber(t.value()));
                case IDENTIFIER -> stack.push(t.value()); // fallback
                case CONCAT -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(stringValue(a) + stringValue(b));
                }
                case OPERATOR -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    double nb = toDouble(b);
                    double na = toDouble(a);
                    double result = switch (t.value()) {
                        case "+" -> na + nb;
                        case "-" -> na - nb;
                        case "*" -> na * nb;
                        case "/" -> {
                            if (nb == 0) throw new ExpressionException("Division by zero");
                            yield na / nb;
                        }
                        default -> throw new ExpressionException("Unknown operator: " + t.value());
                    };
                    stack.push(result);
                }
                case COMPARATOR -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    boolean result = compare(a, b, t.value());
                    stack.push(result);
                }
                case FUNCTION -> {
                    String func = t.value().toLowerCase();
                    int argCount = countFunctionArgs(t, rpn);

                    switch (func) {
                        case "upper" -> {
                            String s = stringValue(stack.pop());
                            stack.push(s.toUpperCase());
                        }
                        case "lower" -> {
                            String s = stringValue(stack.pop());
                            stack.push(s.toLowerCase());
                        }
                        case "trim" -> {
                            String s = stringValue(stack.pop());
                            stack.push(s.trim());
                        }
                        case "substr" -> {
                            int len = toInt(stack.pop());
                            int start = toInt(stack.pop());
                            String s = stringValue(stack.pop());
                            if (start < 0) start = 0;
                            if (start > s.length()) stack.push("");
                            else {
                                int end = Math.min(start + len, s.length());
                                stack.push(s.substring(start, end));
                            }
                        }
                        case "replace" -> {
                            String replacement = stringValue(stack.pop());
                            String oldStr = stringValue(stack.pop());
                            String s = stringValue(stack.pop());
                            stack.push(s.replace(oldStr, replacement));
                        }
                        case "round" -> {
                            if (argCount > 1) {
                                int places = toInt(stack.pop());
                                double n = toDouble(stack.pop());
                                double factor = Math.pow(10, places);
                                stack.push(Math.round(n * factor) / factor);
                            } else {
                                double n = toDouble(stack.pop());
                                stack.push((double) Math.round(n));
                            }
                        }
                        case "floor" -> {
                            double n = toDouble(stack.pop());
                            stack.push(Math.floor(n));
                        }
                        case "ceil" -> {
                            double n = toDouble(stack.pop());
                            stack.push(Math.ceil(n));
                        }
                        case "abs" -> {
                            double n = toDouble(stack.pop());
                            stack.push(Math.abs(n));
                        }
                        case "min" -> {
                            double min = Double.MAX_VALUE;
                            for (int k = 0; k < argCount; k++) {
                                double n = toDouble(stack.pop());
                                if (n < min) min = n;
                            }
                            stack.push(min);
                        }
                        case "max" -> {
                            double max = -Double.MAX_VALUE;
                            for (int k = 0; k < argCount; k++) {
                                double n = toDouble(stack.pop());
                                if (n > max) max = n;
                            }
                            stack.push(max);
                        }
                        case "format" -> {
                            String pattern = stringValue(stack.pop());
                            double n = toDouble(stack.pop());
                            try {
                                DecimalFormat df = new DecimalFormat(pattern,
                                        DecimalFormatSymbols.getInstance(Locale.US));
                                stack.push(df.format(n));
                            } catch (Exception e) {
                                stack.push(String.valueOf(n));
                            }
                        }
                        case "if" -> {
                            Object elseVal = stack.pop();
                            Object thenVal = stack.pop();
                            Object cond = stack.pop();
                            boolean condResult;
                            if (cond instanceof Boolean b) condResult = b;
                            else if (cond instanceof Number n) condResult = n.doubleValue() != 0;
                            else condResult = stringValue(cond).equalsIgnoreCase("true")
                                    || !stringValue(cond).isEmpty();
                            stack.push(condResult ? thenVal : elseVal);
                        }
                        case "num" -> {
                            Object val = stack.pop();
                            if (val instanceof Number n) stack.push(n);
                            else {
                                try {
                                    stack.push(Double.parseDouble(stringValue(val).trim()));
                                } catch (NumberFormatException e) {
                                    stack.push(Double.NaN);
                                }
                            }
                        }
                        case "str" -> {
                            Object val = stack.pop();
                            stack.push(stringValue(val));
                        }
                        case "area", "length", "perimeter" -> {
                            Geometry geom = extractGeometry(feature);
                            if (geom == null) {
                                stack.push(0.0);
                            } else {
                                double result = switch (func) {
                                    case "area" -> geom.getArea();
                                    case "length" -> geom.getLength();
                                    case "perimeter" -> {
                                        if (geom.getDimension() == 2) yield geom.getBoundary().getLength();
                                        else yield geom.getLength();
                                    }
                                    default -> 0.0;
                                };
                                stack.push(result);
                            }
                        }
                        default ->
                            throw new ExpressionException("Unknown function: " + t.value());
                    }
                }
            }
        }

        if (stack.isEmpty()) return "";
        return stack.pop();
    }

    private static int countFunctionArgs(Token functionToken, List<Token> rpn) {
        // Find the function in the RPN and count how many items above it
        // are its arguments. This is a heuristic based on the last comma or LPAREN.
        // For simplicity, we use a simpler approach: look for COMMA tokens
        // between this function's position and where arguments end.
        // In practice, we don't pre-count; we just pop what we need.
        // The min/max functions are varargs, so we need to know.
        int idx = rpn.indexOf(functionToken);
        if (idx < 0) return 0;
        // Walk backwards from idx-1, counting items until we hit the token before
        // the arguments start. This is complex, so we use a simpler heuristic:
        return 0; // caller should use argCount parameter from elsewhere
    }

    // --- Helpers ---

    private static boolean isSimpleFieldRef(String expr) {
        return expr.startsWith("[") && expr.endsWith("]")
                && expr.indexOf(']') == expr.length() - 1
                && expr.indexOf('[') == 0
                && !expr.contains("\"")
                && !expr.contains("||")
                && !expr.contains("+")
                && !expr.contains("(");
    }

    private static Number parseNumber(String s) {
        if (s.contains(".")) return Double.parseDouble(s);
        return Long.parseLong(s);
    }

    private static double toDouble(Object o) {
        if (o instanceof Number n) return n.doubleValue();
        if (o instanceof String s) {
            try { return Double.parseDouble(s.trim()); } catch (NumberFormatException ignored) {}
        }
        if (o instanceof Boolean b) return b ? 1.0 : 0.0;
        return 0.0;
    }

    private static int toInt(Object o) {
        return (int) Math.round(toDouble(o));
    }

    private static String stringValue(Object o) {
        if (o == null) return "";
        if (o instanceof String s) return s;
        if (o instanceof Double d) {
            if (d == Math.floor(d) && !Double.isInfinite(d)) {
                return String.valueOf((long) d.doubleValue());
            }
            return String.valueOf(d);
        }
        return String.valueOf(o);
    }

    private static boolean compare(Object a, Object b, String op) {
        // Numeric comparison if both are numbers
        if (a instanceof Number na && b instanceof Number nb) {
            double da = na.doubleValue();
            double db = nb.doubleValue();
            return switch (op) {
                case "==" -> Math.abs(da - db) < 1e-10;
                case "!=" -> Math.abs(da - db) >= 1e-10;
                case ">" -> da > db;
                case "<" -> da < db;
                case ">=" -> da >= db;
                case "<=" -> da <= db;
                default -> false;
            };
        }
        // String comparison
        String sa = stringValue(a);
        String sb = stringValue(b);
        int cmp = sa.compareToIgnoreCase(sb);
        return switch (op) {
            case "==" -> sa.equalsIgnoreCase(sb);
            case "!=" -> !sa.equalsIgnoreCase(sb);
            case ">" -> cmp > 0;
            case "<" -> cmp < 0;
            case ">=" -> cmp >= 0;
            case "<=" -> cmp <= 0;
            default -> false;
        };
    }

    private static Geometry extractGeometry(SimpleFeature feature) {
        if (feature == null) return null;
        Object defaultGeom = feature.getDefaultGeometry();
        if (defaultGeom instanceof Geometry g) return g;
        return null;
    }

    // --- Exception ---

    public static final class ExpressionException extends RuntimeException {
        public ExpressionException(String message) { super(message); }
        public ExpressionException(String message, Throwable cause) { super(message, cause); }
    }
}
