package ar.com.catgis;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SmileClassifierServiceTest {

    private double[][] features;
    private int[] labels;

    @BeforeEach
    void setUp() {
        // 3 clusters: class 0 around (0,0), class 1 around (10,10), class 2 around (20,20)
        features = new double[30][2];
        labels = new int[30];
        java.util.Random rng = new java.util.Random(42);
        for (int i = 0; i < 10; i++) {
            features[i] = new double[]{rng.nextGaussian(), rng.nextGaussian()};
            labels[i] = 0;
        }
        for (int i = 10; i < 20; i++) {
            features[i] = new double[]{10 + rng.nextGaussian(), 10 + rng.nextGaussian()};
            labels[i] = 1;
        }
        for (int i = 20; i < 30; i++) {
            features[i] = new double[]{20 + rng.nextGaussian(), 20 + rng.nextGaussian()};
            labels[i] = 2;
        }
    }

    @Test
    void knnClassifiesCorrectly() {
        var result = SmileClassifierService.classify(features, labels,
                SmileClassifierService.Algorithm.KNN, 0.8);
        assertNotNull(result);
        assertTrue(result.accuracy() > 60, "kNN accuracy should be > 60%, got: " + result.accuracy());
        assertTrue(result.predictions().length > 0);
        assertEquals("k-Nearest Neighbors", result.algorithmUsed());
    }

    @Test
    void randomForestClassifiesCorrectly() {
        var result = SmileClassifierService.classify(features, labels,
                SmileClassifierService.Algorithm.RANDOM_FOREST, 0.8);
        assertNotNull(result);
        assertTrue(result.accuracy() > 50, "RF accuracy should be > 50%, got: " + result.accuracy());
        assertEquals("Random Forest", result.algorithmUsed());
    }

    @Test
    void naiveBayesClassifiesCorrectly() {
        var result = SmileClassifierService.classify(features, labels,
                SmileClassifierService.Algorithm.NAIVE_BAYES, 0.8);
        assertNotNull(result);
        assertTrue(result.accuracy() > 40, "NB accuracy should be > 40%, got: " + result.accuracy());
        assertEquals("Naive Bayes", result.algorithmUsed());
    }

    @Test
    void decisionTreeClassifiesCorrectly() {
        var result = SmileClassifierService.classify(features, labels,
                SmileClassifierService.Algorithm.DECISION_TREE, 0.8);
        assertNotNull(result);
        assertTrue(result.accuracy() > 50, "DT accuracy should be > 50%, got: " + result.accuracy());
        assertEquals("Decision Tree", result.algorithmUsed());
    }

    @Test
    void allAlgorithmsReturnValidResults() {
        for (SmileClassifierService.Algorithm algo : SmileClassifierService.Algorithm.values()) {
            var result = SmileClassifierService.classify(features, labels, algo, 0.8);
            assertNotNull(result, "Result should not be null for " + algo.displayName());
            assertTrue(result.predictions().length > 0, "Should have predictions for " + algo.displayName());
            assertTrue(result.accuracy() >= 0, "Accuracy should be >= 0 for " + algo.displayName());
            assertTrue(result.accuracy() <= 100, "Accuracy should be <= 100 for " + algo.displayName());
        }
    }
}
