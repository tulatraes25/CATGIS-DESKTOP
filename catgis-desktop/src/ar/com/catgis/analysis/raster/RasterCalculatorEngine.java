package ar.com.catgis.analysis.raster;

import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

/**
 * Evaluates raster algebraic expressions pixel by pixel.
 * <p>
 * Supports: +, -, *, /, sin(), cos(), sqrt(), log(), abs(), pow(),
 * if(cond, then, else), >, <, ==, !=, >=, <=, AND, OR.
 * </p>
 * <p>
 * Rasters referenced by index starting at 0, or by the names 'a','b','c',...
 * </p>
 */
public final class RasterCalculatorEngine {

    private RasterCalculatorEngine() {}

    public static BufferedImage evaluate(List<RasterSource> sources, String expression) {
        if (sources == null || sources.isEmpty()) return null;
        if (expression == null || expression.isBlank()) return null;

        // Find intersection envelope
        double minX = -Double.MAX_VALUE, minY = -Double.MAX_VALUE;
        double maxX = Double.MAX_VALUE, maxY = Double.MAX_VALUE;
        int width = Integer.MAX_VALUE, height = Integer.MAX_VALUE;

        for (RasterSource src : sources) {
            if (src.envelope != null) {
                minX = Math.max(minX, src.envelope.getMinX());
                minY = Math.max(minY, src.envelope.getMinY());
                maxX = Math.min(maxX, src.envelope.getMaxX());
                maxY = Math.min(maxY, src.envelope.getMaxY());
            }
            if (src.image != null) {
                width = Math.min(width, src.image.getWidth());
                height = Math.min(height, src.image.getHeight());
            }
        }

        if (width <= 0 || height <= 0) return null;

        // Create output raster
        BufferedImage output = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        WritableRaster outRaster = output.getRaster();

        // Get input rasters
        List<Raster> inputRasters = new ArrayList<>();
        for (RasterSource src : sources) {
            if (src.image != null) {
                inputRasters.add(src.image.getRaster());
            }
        }

        if (inputRasters.isEmpty()) return null;

        // Parse expression
        List<Token> tokens;
        try {
            tokens = tokenize(expression);
            tokens = shuntingYard(tokens);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error en la expresion: " + e.getMessage());
        }

        // Evaluate per pixel
        int numBands = inputRasters.get(0).getNumBands();
        double[] pixel = new double[numBands];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                try {
                    double result = evaluatePixel(tokens, inputRasters, x, y, width, height);
                    int val = clampToByte(result);
                    outRaster.setSample(x, y, 0, val);
                    outRaster.setSample(x, y, 1, val);
                    outRaster.setSample(x, y, 2, val);
                    outRaster.setSample(x, y, 3, 255);
                } catch (Exception e) {
                    outRaster.setSample(x, y, 0, 0);
                    outRaster.setSample(x, y, 1, 0);
                    outRaster.setSample(x, y, 2, 0);
                    outRaster.setSample(x, y, 3, 0);
                }
            }
        }

        return output;
    }

    private static double evaluatePixel(List<Token> rpn, List<Raster> rasters,
                                        int x, int y, int w, int h) {
        Stack<Double> stack = new Stack<>();
        double[] pixel = new double[4];

        for (Token t : rpn) {
            switch (t.type) {
                case NUMBER -> stack.push(t.numValue);
                case RASTER_REF -> {
                    int idx = t.intValue;
                    Raster r = idx < rasters.size() ? rasters.get(idx) : null;
                    if (r != null) {
                        r.getPixel(x, y, pixel);
                        stack.push(pixel[0]);
                    } else {
                        stack.push(0.0);
                    }
                }
                case OPERATOR -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    double r = switch (t.value) {
                        case "+" -> a + b;
                        case "-" -> a - b;
                        case "*" -> a * b;
                        case "/" -> b != 0 ? a / b : 0;
                        default -> 0;
                    };
                    stack.push(r);
                }
                case COMPARATOR -> {
                    double b = stack.pop();
                    double a = stack.pop();
                    boolean cmp = switch (t.value) {
                        case ">" -> a > b;
                        case "<" -> a < b;
                        case ">=" -> a >= b;
                        case "<=" -> a <= b;
                        case "==" -> Math.abs(a - b) < 1e-10;
                        case "!=" -> Math.abs(a - b) >= 1e-10;
                        default -> false;
                    };
                    stack.push(cmp ? 255.0 : 0.0);
                }
                case FUNCTION -> {
                    String f = t.value.toLowerCase();
                    switch (f) {
                        case "sin" -> stack.push(Math.sin(stack.pop()));
                        case "cos" -> stack.push(Math.cos(stack.pop()));
                        case "sqrt" -> { double v = stack.pop(); stack.push(v >= 0 ? Math.sqrt(v) : 0); }
                        case "log" -> { double v = stack.pop(); stack.push(v > 0 ? Math.log(v) : 0); }
                        case "abs" -> stack.push(Math.abs(stack.pop()));
                        case "pow" -> {
                            double exp = stack.pop();
                            double base = stack.pop();
                            stack.push(Math.pow(base, exp));
                        }
                        case "exp" -> stack.push(Math.exp(stack.pop()));
                        case "floor" -> stack.push(Math.floor(stack.pop()));
                        case "ceil" -> stack.push(Math.ceil(stack.pop()));
                        case "round" -> stack.push((double) Math.round(stack.pop()));
                        case "if" -> {
                            Object elseVal = stack.pop();
                            Object thenVal = stack.pop();
                            double cond = stack.pop();
                            boolean condResult = Math.abs(cond) > 1e-10;
                            // Use cond directly as result
                            if (condResult) stack.push((Double) thenVal);
                            else stack.push((Double) elseVal);
                        }
                        case "ndvi" -> {
                            double nir = stack.pop();
                            double red = stack.pop();
                            double ndviVal = (nir + red) > 0 ? (nir - red) / (nir + red) : 0;
                            stack.push(ndviVal);
                        }
                        case "ndwi" -> { double nir = stack.pop(); double green = stack.pop(); stack.push((green + nir) > 0 ? (green - nir) / (green + nir) : 0); }
                        case "nbr" -> { double swir = stack.pop(); double nir = stack.pop(); stack.push((nir + swir) > 0 ? (nir - swir) / (nir + swir) : 0); }
                        case "savi" -> { double red = stack.pop(); double nir = stack.pop(); stack.push(((nir - red) / (nir + red + 0.5)) * 1.5); }
                        case "evi" -> { double blue = stack.pop(); double red = stack.pop(); double nir = stack.pop(); double denom = nir + 6*red - 7.5*blue + 1; stack.push(denom != 0 ? 2.5 * (nir - red) / denom : 0); }
                        case "tan" -> stack.push(Math.tan(stack.pop()));
                        case "asin" -> stack.push(Math.asin(Math.max(-1, Math.min(1, stack.pop()))));
                        case "acos" -> stack.push(Math.acos(Math.max(-1, Math.min(1, stack.pop()))));
                        case "atan" -> stack.push(Math.atan(stack.pop()));
                        case "log10" -> stack.push(Math.log10(stack.pop()));
                        case "ln" -> stack.push(Math.log(stack.pop()));
                        case "sign" -> stack.push(Math.signum(stack.pop()));
                        case "mod" -> { double b = stack.pop(); stack.push(stack.pop() % b); }
                        case "min" -> { double b = stack.pop(); double a = stack.pop(); stack.push(Math.min(a, b)); }
                        case "max" -> { double b = stack.pop(); double a = stack.pop(); stack.push(Math.max(a, b)); }
                        case "clamp" -> { double hi = stack.pop(); double lo = stack.pop(); stack.push(Math.max(lo, Math.min(hi, stack.pop()))); }
                        case "pi" -> stack.push(Math.PI);
                        case "e" -> stack.push(Math.E);
                        case "hypot" -> { double y2 = stack.pop(); stack.push(Math.hypot(stack.pop(), y2)); }
                        case "degrees" -> stack.push(Math.toDegrees(stack.pop()));
                        case "radians" -> stack.push(Math.toRadians(stack.pop()));
                        case "x" -> stack.push((double) x / w);
                        case "y" -> stack.push((double) y / h);
                    }
                }
            }
        }

        return stack.isEmpty() ? 0 : stack.pop();
    }

    private static int clampToByte(double val) {
        if (Double.isNaN(val) || Double.isInfinite(val)) return 0;
        int i = (int) Math.round(val);
        return Math.max(0, Math.min(255, i));
    }

    // ─── Tokenizer ─────────────────────────────────────────────────────

    private enum TokenType { NUMBER, RASTER_REF, OPERATOR, COMPARATOR, FUNCTION, LPAREN, RPAREN, COMMA }

    private record Token(TokenType type, String value, double numValue, int intValue) {
        Token(TokenType type, String value) { this(type, value, 0, 0); }
        Token(double num) { this(TokenType.NUMBER, null, num, 0); }
        Token(int refIdx) { this(TokenType.RASTER_REF, null, 0, refIdx); }
        Token(TokenType type, String value, int idx) { this(type, value, 0, idx); }
    }

    private static List<Token> tokenize(String expr) {
        List<Token> tokens = new ArrayList<>();
        int i = 0, len = expr.length();
        String low = expr.toLowerCase();

        while (i < len) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) { i++; continue; }

            // Raster references: a, b, c, d, e, f, 0, 1, 2...
            if (Character.isLetter(c)) {
                int start = i;
                while (i < len && Character.isLetterOrDigit(expr.charAt(i))) i++;
                String word = low.substring(start, i);

                // Check if function
                boolean isFunc = switch (word) {
                    case "sin","cos","tan","sqrt","log","log10","ln","abs","pow","exp",
                         "floor","ceil","round","if","ndvi","ndwi","nbr","savi","evi",
                         "asin","acos","atan","atan2","degrees","radians","sign",
                         "hypot","min","max","clamp","mod","pi","e","random",
                         "x","y" -> true;
                    default -> false;
                };

                if (isFunc) {
                    tokens.add(new Token(TokenType.FUNCTION, word));
                } else if (word.equalsIgnoreCase("a")) {
                    tokens.add(new Token(0));
                } else if (word.equalsIgnoreCase("b")) {
                    tokens.add(new Token(1));
                } else if (word.equalsIgnoreCase("c")) {
                    tokens.add(new Token(2));
                } else if (word.equalsIgnoreCase("d")) {
                    tokens.add(new Token(3));
                } else if (word.equalsIgnoreCase("e")) {
                    tokens.add(new Token(4));
                } else if (word.equalsIgnoreCase("f")) {
                    tokens.add(new Token(5));
                } else if (word.equalsIgnoreCase("x") || word.equalsIgnoreCase("y")) {
                    tokens.add(new Token(TokenType.FUNCTION, word));
                } else {
                    throw new IllegalArgumentException("Identificador desconocido: " + word);
                }
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                int start = i;
                while (i < len && (Character.isDigit(expr.charAt(i)) || expr.charAt(i) == '.')) i++;
                double val = Double.parseDouble(expr.substring(start, i));
                tokens.add(new Token(val));
                continue;
            }

            // 2-char operators
            if (i + 1 < len) {
                String two = expr.substring(i, i + 2);
                if (two.equals(">=") || two.equals("<=") || two.equals("==") || two.equals("!=") || two.equals("||") || two.equals("&&")) {
                    tokens.add(new Token(
                            two.equals("||") || two.equals("&&") ? TokenType.OPERATOR : TokenType.COMPARATOR,
                            two));
                    i += 2;
                    continue;
                }
            }

            // 1-char
            switch (c) {
                case '+','-','*','/' -> tokens.add(new Token(TokenType.OPERATOR, String.valueOf(c)));
                case '>','<' -> tokens.add(new Token(TokenType.COMPARATOR, String.valueOf(c)));
                case '(' -> tokens.add(new Token(TokenType.LPAREN, "("));
                case ')' -> tokens.add(new Token(TokenType.RPAREN, ")"));
                case ',' -> tokens.add(new Token(TokenType.COMMA, ","));
                default -> throw new IllegalArgumentException("Caracter inesperado: " + c);
            }
            i++;
        }
        return tokens;
    }

    private static int getPrec(String op) {
        return switch (op) {
            case "||","&&" -> 1;
            case ">","<",">=","<=","==","!=" -> 2;
            case "+","-" -> 3;
            case "*","/" -> 4;
            default -> 0;
        };
    }

    private static List<Token> shuntingYard(List<Token> tokens) {
        List<Token> output = new ArrayList<>();
        Stack<Token> stack = new Stack<>();

        for (Token t : tokens) {
            switch (t.type) {
                case NUMBER, RASTER_REF -> output.add(t);
                case FUNCTION -> stack.push(t);
                case COMMA -> {
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LPAREN)
                        output.add(stack.pop());
                }
                case OPERATOR, COMPARATOR -> {
                    while (!stack.isEmpty()
                            && (stack.peek().type == TokenType.OPERATOR || stack.peek().type == TokenType.COMPARATOR)
                            && getPrec(stack.peek().value) >= getPrec(t.value)) {
                        output.add(stack.pop());
                    }
                    stack.push(t);
                }
                case LPAREN -> stack.push(t);
                case RPAREN -> {
                    while (!stack.isEmpty() && stack.peek().type != TokenType.LPAREN)
                        output.add(stack.pop());
                    if (!stack.isEmpty()) stack.pop(); // LPAREN
                    if (!stack.isEmpty() && stack.peek().type == TokenType.FUNCTION)
                        output.add(stack.pop());
                }
            }
        }
        while (!stack.isEmpty()) output.add(stack.pop());
        return output;
    }

    // ─── Data source ──────────────────────────────────────────────────

    public record RasterSource(BufferedImage image, org.locationtech.jts.geom.Envelope envelope, String name) {}
}
