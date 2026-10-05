package ar.com.catgis;

import java.util.*;

/**
 * Machine Learning classification service.
 * Implements kNN, Random Forest, and Decision Tree classifiers from scratch.
 * No external ML dependencies needed.
 */
public final class SmileClassifierService {

    private SmileClassifierService() {}

    public enum Algorithm {
        KNN("k-Nearest Neighbors", "Clasificacion por k vecinos mas cercanos"),
        RANDOM_FOREST("Random Forest", "Ensemble de arboles de decision"),
        DECISION_TREE("Decision Tree", "Arbol de decision simple"),
        NAIVE_BAYES("Naive Bayes", "Clasificador probabilistico bayesiano");

        private final String displayName;
        private final String description;
        Algorithm(String d, String desc) { displayName = d; description = desc; }
        public String displayName() { return displayName; }
        public String description() { return description; }
    }

    public record ClassificationResult(
            int[] predictions, int correctCount, double accuracy,
            String algorithmUsed, long trainingTimeMs, long predictionTimeMs
    ) {}

    public static ClassificationResult classify(double[][] features, int[] labels,
                                                 Algorithm algorithm, double trainTestRatio) {
        long startTime = System.currentTimeMillis();

        int n = features.length;
        int trainSize = (int) (n * trainTestRatio);
        int testSize = n - trainSize;

        double[][] trainX = new double[trainSize][];
        int[] trainY = new int[trainSize];
        double[][] testX = new double[testSize][];
        int[] testY = new int[testSize];
        for (int i = 0; i < trainSize; i++) { trainX[i] = features[i]; trainY[i] = labels[i]; }
        for (int i = 0; i < testSize; i++) { testX[i] = features[trainSize + i]; testY[i] = labels[trainSize + i]; }

        try {
            Classifier classifier = switch (algorithm) {
                case KNN -> new KNNClassifier(trainX, trainY, 3);
                case RANDOM_FOREST -> new RandomForestClassifier(trainX, trainY, 50);
                case DECISION_TREE -> new DecisionTreeClassifier(trainX, trainY);
                case NAIVE_BAYES -> new NaiveBayesClassifier(trainX, trainY);
            };

            long trainTime = System.currentTimeMillis() - startTime;

            long predStart = System.currentTimeMillis();
            int[] predictions = new int[testSize];
            int correct = 0;
            for (int i = 0; i < testSize; i++) {
                predictions[i] = classifier.predict(testX[i]);
                if (predictions[i] == testY[i]) correct++;
            }
            long predTime = System.currentTimeMillis() - predStart;

            double accuracy = testSize > 0 ? (double) correct / testSize * 100 : 0;
            return new ClassificationResult(predictions, correct, accuracy,
                    algorithm.displayName(), trainTime, predTime);

        } catch (Exception e) {
            return new ClassificationResult(new int[0], 0, 0,
                    algorithm.displayName() + " (error: " + e.getMessage() + ")", 0, 0);
        }
    }

    // --- Classifiers ---

    interface Classifier { int predict(double[] x); }

    static class KNNClassifier implements Classifier {
        private final double[][] trainX;
        private final int[] trainY;
        private final int k;
        KNNClassifier(double[][] x, int[] y, int k) { this.trainX = x; this.trainY = y; this.k = k; }

        public int predict(double[] x) {
            double[] dists = new double[trainX.length];
            for (int i = 0; i < trainX.length; i++) dists[i] = euclidean(x, trainX[i]);
            Integer[] idx = new Integer[trainX.length];
            for (int i = 0; i < idx.length; i++) idx[i] = i;
            Arrays.sort(idx, Comparator.comparingDouble(i -> dists[i]));
            int[] votes = new int[10];
            for (int i = 0; i < k && i < idx.length; i++) votes[trainY[idx[i]]]++;
            int best = 0;
            for (int i = 1; i < votes.length; i++) if (votes[i] > votes[best]) best = i;
            return best;
        }
    }

    static class RandomForestClassifier implements Classifier {
        private final List<DecisionTreeClassifier> trees;
        RandomForestClassifier(double[][] x, int[] y, int numTrees) {
            trees = new ArrayList<>();
            Random rng = new Random(42);
            for (int t = 0; t < numTrees; t++) {
                int n = x.length;
                double[][] bx = new double[n][];
                int[] by = new int[n];
                for (int i = 0; i < n; i++) { int idx = rng.nextInt(n); bx[i] = x[idx]; by[i] = y[idx]; }
                trees.add(new DecisionTreeClassifier(bx, by));
            }
        }
        public int predict(double[] x) {
            int[] votes = new int[10];
            for (DecisionTreeClassifier tree : trees) votes[tree.predict(x)]++;
            int best = 0;
            for (int i = 1; i < votes.length; i++) if (votes[i] > votes[best]) best = i;
            return best;
        }
    }

    static class DecisionTreeClassifier implements Classifier {
        private final double[] thresholds;
        private final int[] featureIdx;
        private final int[] labels;
        private final int[] leftChild, rightChild;
        private int nodeCount = 0;

        DecisionTreeClassifier(double[][] x, int[] y) {
            int maxDepth = 10;
            thresholds = new double[maxDepth * 100];
            featureIdx = new int[maxDepth * 100];
            labels = new int[maxDepth * 100];
            leftChild = new int[maxDepth * 100];
            rightChild = new int[maxDepth * 100];
            Arrays.fill(leftChild, -1);
            Arrays.fill(rightChild, -1);
            buildTree(x, y, 0, 0);
        }

        private void buildTree(double[][] x, int[] y, int depth, int node) {
            if (depth >= 10 || x.length < 2) {
                int majority = majorityClass(y);
                labels[node] = majority;
                return;
            }
            boolean allSame = true;
            for (int i = 1; i < y.length; i++) if (y[i] != y[0]) { allSame = false; break; }
            if (allSame) { labels[node] = y[0]; return; }

            int bestFeature = 0;
            double bestThreshold = 0;
            double bestGini = Double.MAX_VALUE;
            int dim = x[0].length;
            for (int d = 0; d < dim; d++) {
                double[] vals = new double[x.length];
                for (int i = 0; i < x.length; i++) vals[i] = x[i][d];
                Arrays.sort(vals);
                for (int i = 0; i < Math.min(vals.length - 1, 10); i++) {
                    double t = (vals[i] + vals[Math.min(i + 1, vals.length - 1)]) / 2;
                    double gini = giniSplit(x, y, d, t);
                    if (gini < bestGini) { bestGini = gini; bestFeature = d; bestThreshold = t; }
                }
            }
            thresholds[node] = bestThreshold;
            featureIdx[node] = bestFeature;
            int nextNode = nodeCount + 1;
            leftChild[node] = nextNode;
            nodeCount++;
            rightChild[node] = nodeCount + 1;
            nodeCount++;

            List<double[]> leftX = new ArrayList<>(), rightX = new ArrayList<>();
            List<Integer> leftY = new ArrayList<>(), rightY = new ArrayList<>();
            for (int i = 0; i < x.length; i++) {
                if (x[i][bestFeature] <= bestThreshold) { leftX.add(x[i]); leftY.add(y[i]); }
                else { rightX.add(x[i]); rightY.add(y[i]); }
            }
            if (!leftX.isEmpty()) buildTree(leftX.stream().toArray(double[][]::new), leftY.stream().mapToInt(i -> i).toArray(), depth + 1, leftChild[node]);
            if (!rightX.isEmpty()) buildTree(rightX.stream().toArray(double[][]::new), rightY.stream().mapToInt(i -> i).toArray(), depth + 1, rightChild[node]);
        }

        private double giniSplit(double[][] x, int[] y, int feature, double threshold) {
            int leftN = 0, rightN = 0;
            int[] leftCounts = new int[10], rightCounts = new int[10];
            for (int i = 0; i < x.length; i++) {
                if (x[i][feature] <= threshold) { leftN++; leftCounts[y[i]]++; }
                else { rightN++; rightCounts[y[i]]++; }
            }
            double leftGini = 1.0;
            for (int c : leftCounts) if (c > 0) leftGini -= (double) c * c / (leftN * leftN);
            double rightGini = 1.0;
            for (int c : rightCounts) if (c > 0) rightGini -= (double) c * c / (rightN * rightN);
            return ((double) leftN / x.length) * leftGini + ((double) rightN / x.length) * rightGini;
        }

        private int majorityClass(int[] y) {
            int[] counts = new int[10];
            for (int v : y) if (v >= 0 && v < counts.length) counts[v]++;
            int best = 0;
            for (int i = 1; i < counts.length; i++) if (counts[i] > counts[best]) best = i;
            return best;
        }

        public int predict(double[] x) { return predict(x, 0); }
        private int predict(double[] x, int node) {
            if (node >= labels.length || (leftChild[node] == -1 && rightChild[node] == -1)) return labels[node];
            if (x[featureIdx[node]] <= thresholds[node]) return predict(x, leftChild[node]);
            return predict(x, rightChild[node]);
        }
    }

    static class NaiveBayesClassifier implements Classifier {
        private final double[][] means;
        private final double[][] stds;
        private final double[] priors;
        private final int numClasses;

        NaiveBayesClassifier(double[][] x, int[] y) {
            int maxLabel = 0;
            for (int v : y) if (v > maxLabel) maxLabel = v;
            numClasses = maxLabel + 1;
            int dim = x[0].length;
            means = new double[numClasses][dim];
            stds = new double[numClasses][dim];
            priors = new double[numClasses];

            int[] counts = new int[numClasses];
            for (int i = 0; i < x.length; i++) { int c = y[i]; counts[c]++; for (int d = 0; d < dim; d++) means[c][d] += x[i][d]; }
            for (int c = 0; c < numClasses; c++) {
                priors[c] = (double) counts[c] / x.length;
                for (int d = 0; d < dim; d++) means[c][d] /= counts[c];
            }
            for (int i = 0; i < x.length; i++) {
                int c = y[i];
                for (int d = 0; d < dim; d++) stds[c][d] += (x[i][d] - means[c][d]) * (x[i][d] - means[c][d]);
            }
            for (int c = 0; c < numClasses; c++) {
                for (int d = 0; d < dim; d++) {
                    stds[c][d] = Math.sqrt(stds[c][d] / counts[c]);
                    if (stds[c][d] < 1e-10) stds[c][d] = 1.0;
                }
            }
        }

        public int predict(double[] x) {
            int best = 0;
            double bestScore = Double.NEGATIVE_INFINITY;
            for (int c = 0; c < numClasses; c++) {
                double logP = Math.log(priors[c]);
                for (int d = 0; d < x.length; d++) {
                    double z = (x[d] - means[c][d]) / stds[c][d];
                    logP += -0.5 * z * z - Math.log(stds[c][d]);
                }
                if (logP > bestScore) { bestScore = logP; best = c; }
            }
            return best;
        }
    }

    private static double euclidean(double[] a, double[] b) {
        double sum = 0;
        int len = Math.min(a.length, b.length);
        for (int i = 0; i < len; i++) { double d = a[i] - b[i]; sum += d * d; }
        return Math.sqrt(sum);
    }
}
