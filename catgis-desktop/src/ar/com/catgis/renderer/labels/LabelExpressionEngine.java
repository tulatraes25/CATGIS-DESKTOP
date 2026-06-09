package ar.com.catgis.renderer.labels;

import org.geotools.api.feature.simple.SimpleFeature;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Point;

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
        FUNCTION, COMMA, LPAREN, RPAREN, CONCAT, IDENTIFIER,
        LOGICAL_AND
    }

    private record Token(TokenType type, String value, int position, int arity) {
        Token(TokenType type, String value, int position) { this(type, value, position, 0); }
    }

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

            // 2-char logical: &&, ||
            if (i + 1 < len) {
                String twoChar = expr.substring(i, i + 2);
                if (twoChar.equals("&&")) {
                    tokens.add(new Token(TokenType.LOGICAL_AND, "&&", i));
                    i += 2;
                    continue;
                }

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
                    // Count top-level commas to determine arity
                    int parenDepth = 0;
                    int commas = 0;
                    for (int k = j + 1; k < len; k++) {
                        char pk = expr.charAt(k);
                        if (pk == '(') parenDepth++;
                        else if (pk == ')') {
                            if (parenDepth == 0) break;
                            parenDepth--;
                        } else if (pk == ',' && parenDepth == 0) commas++;
                    }
                    tokens.add(new Token(TokenType.FUNCTION, ident, start, commas + 1));
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
            case "&&", "||" -> 1;
            case "==", "!=" -> 2;
            case ">", "<", ">=", "<=" -> 3;
            case "+", "-" -> 4;
            case "*", "/" -> 5;
            default -> 1; // CONCAT and others
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
                case OPERATOR, COMPARATOR, CONCAT, LOGICAL_AND -> {
                    while (!stack.isEmpty() && (stack.peek().type() == TokenType.OPERATOR
                            || stack.peek().type() == TokenType.COMPARATOR
                            || stack.peek().type() == TokenType.CONCAT
                            || stack.peek().type() == TokenType.LOGICAL_AND)
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
                case LOGICAL_AND -> {
                    Object b = stack.pop();
                    Object a = stack.pop();
                    stack.push(toBoolean(a) && toBoolean(b));
                }

                case FUNCTION -> {
                    String func = t.value().toLowerCase();
                    int argCount = t.arity();

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
                        case "sqrt" -> { stack.push(Math.sqrt(toDouble(stack.pop()))); }
                        case "log" -> { stack.push(Math.log(toDouble(stack.pop()))); }
                        case "log10" -> { stack.push(Math.log10(toDouble(stack.pop()))); }
                        case "exp" -> { stack.push(Math.exp(toDouble(stack.pop()))); }
                        case "sin" -> { stack.push(Math.sin(Math.toRadians(toDouble(stack.pop())))); }
                        case "cos" -> { stack.push(Math.cos(Math.toRadians(toDouble(stack.pop())))); }
                        case "tan" -> { stack.push(Math.tan(Math.toRadians(toDouble(stack.pop())))); }
                        case "pow" -> { double expVal = toDouble(stack.pop()); stack.push(Math.pow(toDouble(stack.pop()), expVal)); }
                        case "mod" -> { double divisor = toDouble(stack.pop()); stack.push(toDouble(stack.pop()) % divisor); }
                        case "left" -> { int n = toInt(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.length() <= n ? s : s.substring(0, n)); }
                        case "right" -> { int n = toInt(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.length() <= n ? s : s.substring(s.length() - n)); }
                        case "len" -> { stack.push((double) stringValue(stack.pop()).length()); }
                        case "coalesce" -> {
                            Object b = stack.pop();
                            Object a = stack.pop();
                            if (a != null && !stringValue(a).isEmpty()) stack.push(a);
                            else if (b != null && !stringValue(b).isEmpty()) stack.push(b);
                            else stack.push("");
                        }
                        case "concat" -> {
                            StringBuilder sb = new StringBuilder();
                            for (int k = 0; k < argCount; k++) sb.insert(0, stringValue(stack.pop()));
                            stack.push(sb.toString());
                        }
                        case "x" -> { stack.push(extractX(feature)); }
                        case "y" -> { stack.push(extractY(feature)); }

                        // === String functions (new) ===
                        case "contains" -> { String search = stringValue(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.contains(search)); }
                        case "startswith" -> { String prefix = stringValue(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.toLowerCase().startsWith(prefix.toLowerCase())); }
                        case "endswith" -> { String suffix = stringValue(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.toLowerCase().endsWith(suffix.toLowerCase())); }
                        case "repeat" -> { int n = toInt(stack.pop()); String s = stringValue(stack.pop()); stack.push(s.repeat(Math.max(0, Math.min(n, 1000)))); }
                        case "reverse" -> { String s = stringValue(stack.pop()); stack.push(new StringBuilder(s).reverse().toString()); }
                        case "capitalize" -> { String s = stringValue(stack.pop()); stack.push(s.isEmpty() ? s : s.substring(0, 1).toUpperCase() + s.substring(1)); }
                        case "titlecase" -> { String s = stringValue(stack.pop()); stack.push(titleCase(s)); }
                        case "pad" -> { int len = toInt(stack.pop()); String s = stringValue(stack.pop()); stack.push(padRight(s, len)); }
                        case "strip" -> { String s = stringValue(stack.pop()); stack.push(s.strip()); }
                        case "count" -> { String search = stringValue(stack.pop()); String s = stringValue(stack.pop()); stack.push((double) countOccurrences(s, search)); }
                        case "indexof" -> { String search = stringValue(stack.pop()); String s = stringValue(stack.pop()); stack.push((double) s.indexOf(search)); }
                        case "nvl" -> { Object def = stack.pop(); Object val = stack.pop(); stack.push(val != null && !stringValue(val).isEmpty() ? val : def); }
                        case "nullto" -> { Object def = stack.pop(); Object val = stack.pop(); stack.push(val != null ? val : def); }

                        // === Math functions (new) ===
                        case "atan" -> { stack.push(Math.toDegrees(Math.atan(toDouble(stack.pop())))); }
                        case "asin" -> { stack.push(Math.toDegrees(Math.asin(toDouble(stack.pop())))); }
                        case "acos" -> { stack.push(Math.toDegrees(Math.acos(toDouble(stack.pop())))); }
                        case "atan2" -> { double y = toDouble(stack.pop()); stack.push(Math.toDegrees(Math.atan2(toDouble(stack.pop()), y))); }
                        case "degrees" -> { stack.push(Math.toDegrees(toDouble(stack.pop()))); }
                        case "radians" -> { stack.push(Math.toRadians(toDouble(stack.pop()))); }
                        case "sign" -> { stack.push(Math.signum(toDouble(stack.pop()))); }
                        case "clamp" -> { double hi = toDouble(stack.pop()); double lo = toDouble(stack.pop()); stack.push(Math.max(lo, Math.min(hi, toDouble(stack.pop())))); }
                        case "hypot" -> { double y = toDouble(stack.pop()); stack.push(Math.hypot(toDouble(stack.pop()), y)); }
                        case "pi" -> { stack.push(Math.PI); }
                        case "e" -> { stack.push(Math.E); }
                        case "random" -> { stack.push(Math.random()); }
                        case "ln" -> { stack.push(Math.log(toDouble(stack.pop()))); }

                        // === Date functions (new) ===
                        case "now" -> { stack.push(new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new java.util.Date())); }
                        case "year" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.YEAR)); }
                        case "month" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1); }
                        case "day" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH)); }
                        case "hour" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)); }
                        case "minute" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.MINUTE)); }
                        case "second" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.SECOND)); }
                        case "weekday" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_WEEK)); }
                        case "dayofyear" -> { stack.push((double) java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_YEAR)); }

                        // === Aggregation functions (new) ===
                        case "sum" -> { double sum = 0; for (int k = 0; k < argCount; k++) sum += toDouble(stack.pop()); stack.push(sum); }
                        case "avg" -> { double sum = 0; for (int k = 0; k < argCount; k++) sum += toDouble(stack.pop()); stack.push(argCount > 0 ? sum / argCount : 0); }

                        // === Logic functions (new) ===
                        case "not" -> { stack.push(!toBoolean(stack.pop())); }
                        case "isnull" -> { stack.push(stack.peek() == null); }
                        case "isempty" -> { stack.push(stringValue(stack.peek()).isEmpty()); }
                        case "isnumeric" -> { try { Double.parseDouble(stringValue(stack.peek())); stack.push(true); } catch (Exception e) { stack.push(false); } }

                        // === Geometry functions (new) ===
                        case "centroid" -> { Geometry geom = extractGeometry(feature); stack.push(geom != null ? geom.getCentroid().getCoordinate().x + "," + geom.getCentroid().getCoordinate().y : "0,0"); }
                        case "centroid_x" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? g.getCentroid().getCoordinate().x : 0.0); }
                        case "centroid_y" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? g.getCentroid().getCoordinate().y : 0.0); }
                        case "area" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? g.getArea() : 0.0); }
                        case "length" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? g.getLength() : 0.0); }
                        case "perimeter" -> { Geometry g = extractGeometry(feature); stack.push(g != null && g.getDimension() == 2 ? g.getBoundary().getLength() : (g != null ? g.getLength() : 0.0)); }
                        case "numcoordinates" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? (double) g.getCoordinates().length : 0.0); }
                        case "dimension" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? (double) g.getDimension() : -1.0); }
                        case "isvalid" -> { Geometry g = extractGeometry(feature); stack.push(g != null && g.isValid()); }
                        case "geomisempty" -> { Geometry g = extractGeometry(feature); stack.push(g == null || g.isEmpty()); }
                        case "geometrytype" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? g.getGeometryType() : "None"); }
                        case "numgeometries" -> { Geometry g = extractGeometry(feature); stack.push(g != null ? (double) g.getNumGeometries() : 0.0); }
                        case "distance" -> { double y2 = toDouble(stack.pop()); double x2 = toDouble(stack.pop()); Geometry ga = extractGeometry(feature); Point p = new GeometryFactory().createPoint(new Coordinate(x2, y2)); stack.push(ga != null ? ga.distance(p) : Double.MAX_VALUE); }

                        default ->
                            throw new ExpressionException("Unknown function: " + t.value());
                    }
                }
            }
        }

        if (stack.isEmpty()) return "";
        return stack.pop();
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

    private static boolean toBoolean(Object o) {
        if (o instanceof Boolean b) return b;
        if (o instanceof Number n) return n.doubleValue() != 0;
        if (o instanceof String s) return !s.trim().isEmpty() && !"false".equalsIgnoreCase(s.trim()) && !"0".equals(s.trim());
        return o != null;
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

    private static double extractX(SimpleFeature feature) {
        Geometry g = extractGeometry(feature);
        if (g != null) return g.getCoordinate().getX();
        return 0;
    }

    private static double extractY(SimpleFeature feature) {
        Geometry g = extractGeometry(feature);
        if (g != null) return g.getCoordinate().getY();
        return 0;
    }

    private static String titleCase(String s) {
        if (s == null || s.isEmpty()) return s;
        StringBuilder sb = new StringBuilder();
        boolean capitalizeNext = true;
        for (char c : s.toCharArray()) {
            if (Character.isWhitespace(c)) {
                sb.append(c);
                capitalizeNext = true;
            } else if (capitalizeNext) {
                sb.append(Character.toUpperCase(c));
                capitalizeNext = false;
            } else {
                sb.append(Character.toLowerCase(c));
            }
        }
        return sb.toString();
    }

    private static String padRight(String s, int len) {
        if (s == null) s = "";
        return String.format("%-" + len + "s", s);
    }

    private static int countOccurrences(String s, String search) {
        if (s == null || search == null || search.isEmpty()) return 0;
        int count = 0;
        int idx = 0;
        while ((idx = s.indexOf(search, idx)) >= 0) {
            count++;
            idx += search.length();
        }
        return count;
    }

    // --- Exception ---

    public static final class ExpressionException extends RuntimeException {
        public ExpressionException(String message) { super(message); }
        public ExpressionException(String message, Throwable cause) { super(message, cause); }
    }
}



