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
 * <p>
 * Progress reporting via {@link ProgressListener}.
 * </p>
 */
public final class RasterCalculatorEngine {

    private RasterCalculatorEngine() {}

    /**
     * Callback for pixel-by-pixel progress updates.
     */
    @FunctionalInterface
    public interface ProgressListener {
        void onProgress(int current, int total);
    }

    public static BufferedImage evaluate(List<RasterSource> sources, String expression) {
        return evaluate(sources, expression, null);
    }

    public static BufferedImage evaluate(List<RasterSource> sources, String expression,
                                          ProgressListener progress) {
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
        int totalPixels = width * height;
        int pixelCount = 0;

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
                pixelCount++;
                if (progress != null && pixelCount % 1000 == 0) {
                    progress.onProgress(pixelCount, totalPixels);
                }
            }
        }

        if (progress != null) {
            progress.onProgress(totalPixels, totalPixels);
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
                        case "atan2" -> { double dy = stack.pop(); stack.push(Math.atan2(stack.pop(), dy)); }
                        case "asin" -> stack.push(Math.asin(Math.max(-1, Math.min(1, stack.pop()))));
                        case "acos" -> stack.push(Math.acos(Math.max(-1, Math.min(1, stack.pop()))));
                        case "atan" -> stack.push(Math.atan(stack.pop()));
                        case "cot" -> stack.push(1.0 / Math.tan(stack.pop()));
                        case "sec" -> stack.push(1.0 / Math.cos(stack.pop()));
                        case "csc" -> stack.push(1.0 / Math.sin(stack.pop()));
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
                        case "random" -> stack.push(Math.random() * 255.0);
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
                         "cot","sec","csc",
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

    // ─── Pansharpening (IHS method) ──────────────────────────────────

    /**
     * Pansharpen 3-band multispectral image using a panchromatic band.
     * Uses IHS (Intensity-Hue-Saturation) color space transform.
     *
     * @param msRed   multispectral red band (low resolution)
     * @param msGreen multispectral green band
     * @param msBlue  multispectral blue band
     * @param pan     panchromatic band (high resolution)
     * @return pansharpened RGB image at pan resolution, or null on error
     */
    public static BufferedImage pansharpenIhs(BufferedImage msRed, BufferedImage msGreen,
                                               BufferedImage msBlue, BufferedImage pan) {
        if (msRed == null || msGreen == null || msBlue == null || pan == null) return null;
        int pw = pan.getWidth();
        int ph = pan.getHeight();
        Raster panRaster = pan.getRaster();
        double[] panPx = new double[panRaster.getNumBands()];

        // Resize multispectral bands to pan dimensions (nearest neighbor)
        BufferedImage rUp = resizeNearest(msRed, pw, ph);
        BufferedImage gUp = resizeNearest(msGreen, pw, ph);
        BufferedImage bUp = resizeNearest(msBlue, pw, ph);
        if (rUp == null || gUp == null || bUp == null) return null;

        Raster rRas = rUp.getRaster();
        Raster gRas = gUp.getRaster();
        Raster bRas = bUp.getRaster();
        double[] rPx = new double[1], gPx = new double[1], bPx = new double[1];

        BufferedImage result = new BufferedImage(pw, ph, BufferedImage.TYPE_INT_RGB);
        WritableRaster out = result.getRaster();
        double[] outPx = new double[3];

        for (int y = 0; y < ph; y++) {
            for (int x = 0; x < pw; x++) {
                rRas.getPixel(x, y, rPx);
                gRas.getPixel(x, y, gPx);
                bRas.getPixel(x, y, bPx);
                panRaster.getPixel(x, y, panPx);

                double r = rPx[0] / 255.0;
                double g = gPx[0] / 255.0;
                double b = bPx[0] / 255.0;
                double p = panPx[0] / 255.0;

                // RGB → IHS
                double i = (r + g + b) / 3.0;
                double minRgb = Math.min(Math.min(r, g), b);
                double s = (i > 0) ? 1.0 - (minRgb / i) : 0;
                double h = 0;
                if (s > 0.001) {
                    double num = 0.5 * ((r - g) + (r - b));
                    double den = Math.sqrt((r - g) * (r - g) + (r - b) * (g - b));
                    double acos = (den > 0) ? Math.acos(num / den) : 0;
                    h = (b <= g) ? acos : (2 * Math.PI - acos);
                }

                // Replace intensity with pan
                i = p;

                // IHS → RGB
                double rOut, gOut, bOut;
                if (s < 0.001) {
                    rOut = gOut = bOut = i;
                } else {
                    double hDeg = h * 180.0 / Math.PI;
                    if (hDeg < 120) {
                        bOut = i * (1 - s);
                        rOut = i * (1 + s * Math.cos(h) / Math.cos(Math.PI / 3 - h));
                        gOut = 3 * i - (rOut + bOut);
                    } else if (hDeg < 240) {
                        double h2 = h - 2 * Math.PI / 3;
                        rOut = i * (1 - s);
                        gOut = i * (1 + s * Math.cos(h2) / Math.cos(Math.PI / 3 - h2));
                        bOut = 3 * i - (rOut + gOut);
                    } else {
                        double h2 = h - 4 * Math.PI / 3;
                        gOut = i * (1 - s);
                        bOut = i * (1 + s * Math.cos(h2) / Math.cos(Math.PI / 3 - h2));
                        rOut = 3 * i - (gOut + bOut);
                    }
                }

                outPx[0] = clamp(rOut * 255.0, 0, 255);
                outPx[1] = clamp(gOut * 255.0, 0, 255);
                outPx[2] = clamp(bOut * 255.0, 0, 255);
                out.setPixel(x, y, outPx);
            }
        }
        return result;
    }

    private static BufferedImage resizeNearest(BufferedImage src, int w, int h) {
        if (src == null) return null;
        java.awt.Image scaled = src.getScaledInstance(w, h, java.awt.Image.SCALE_DEFAULT);
        BufferedImage result = new BufferedImage(w, h, src.getType());
        java.awt.Graphics2D g = result.createGraphics();
        g.drawImage(scaled, 0, 0, null);
        g.dispose();
        return result;
    }

    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}
