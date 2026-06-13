package ar.com.catgis.layout;

/**
 * Scale rounding rule for layout scale bars.
 */
public enum ScaleRule {
    PREFERRED_CARTOGRAPHY("Preferidas 500/1000/2000/5000/10000", new double[]{250d, 500d, 1000d, 2000d, 5000d, 10000d, 20000d, 50000d, 100000d}),
    ENGINEERING("Ingenieria 1-2-5", null),
    LARGE_AREA("Grandes areas 1000/2500/5000/10000", new double[]{500d, 1000d, 2500d, 5000d, 10000d, 25000d, 50000d, 100000d, 250000d});

    private final String label;
    private final double[] preferredValues;

    ScaleRule(String label, double[] preferredValues) {
        this.label = label;
        this.preferredValues = preferredValues;
    }

    /**
     * Round a raw scale value to the nearest preferred value or nice round number.
     */
    public double roundValue(double rawValue) {
        if (rawValue <= 0) {
            return 0;
        }
        if (preferredValues != null && preferredValues.length > 0) {
            double best = preferredValues[0];
            double bestDistance = Math.abs(preferredValues[0] - rawValue);
            for (double preferredValue : preferredValues) {
                double distance = Math.abs(preferredValue - rawValue);
                if (distance < bestDistance) {
                    best = preferredValue;
                    bestDistance = distance;
                }
            }
            return best;
        }

        double exponent = Math.pow(10, Math.floor(Math.log10(rawValue)));
        double normalized = rawValue / exponent;
        double rounded;
        if (normalized < 1.5d) {
            rounded = 1d;
        } else if (normalized < 3.5d) {
            rounded = 2d;
        } else if (normalized < 7.5d) {
            rounded = 5d;
        } else {
            rounded = 10d;
        }
        return rounded * exponent;
    }

    @Override
    public String toString() {
        return label;
    }
}
