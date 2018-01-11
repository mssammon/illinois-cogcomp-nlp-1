package edu.illinois.cs.cogcomp.pos.lbjava;

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.math.Permutations;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;
import edu.illinois.cs.cogcomp.lbjava.classify.Classifier;
import edu.illinois.cs.cogcomp.lbjava.classify.TestDiscrete;
import edu.illinois.cs.cogcomp.lbjava.learn.BatchTrainer;
import edu.illinois.cs.cogcomp.lbjava.learn.Learner;
import edu.illinois.cs.cogcomp.lbjava.learn.SparseAveragedPerceptron;
import edu.illinois.cs.cogcomp.lbjava.learn.TestingMetric;
import edu.illinois.cs.cogcomp.lbjava.nlp.seg.POSBracketToToken;
import edu.illinois.cs.cogcomp.lbjava.parse.FoldParser;
import edu.illinois.cs.cogcomp.lbjava.parse.Parser;
import edu.illinois.cs.cogcomp.pos.POSConfigurator;
import edu.illinois.cs.cogcomp.pos.POSLabeledUnknownWordParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Using a *tune* set, find an optimal set of parameters for POS tagger using cross-validation,
 *   based on a set of parameter options specified by user via a configuration object or file.
 * If so configured, evaluate using TestPOS on a separate *test* set.
 *
 * CAVEAT: this class redirects System.out to capture output from TestDiscrete, used for
 *    optimization metric
 *
 * TODO: abstract and move abstraction to lbjava-nlp-tools eval package
 * @author mssammon
 */
public class POSTrainOptimizeParams {

    private final Logger logger = LoggerFactory.getLogger(POSTrainOptimizeParams.class);

    private final String trainingAndDevData;
    private final String testData;
    private final OptimizationTargetStat optimizationTargetStat;
    private final ResourceManager finalConfig;
    private final String reportFile;
    private Parser trainingParserKnown;
    private Parser trainingParserUnknown;

    private Learner.Parameters[] knownTuneParameters;
    private Learner.Parameters[] unknownTuneParameters;

    private String knownModelFile;
    private String knownLexFile;
    private String unknownModelFile;
    private String unknownLexFile;
    private String baselineModelFile;
    private String baselineLexFile;
    private String mikheevModelFile;
    private String mikheevLexFile;
    private int optimizationStatIndex;
/** the list of lists of parameter values, currently [thickness, learningRate].
 *  needed to simplify reporting, as LBJava's Parameters are hard to access. */
    private List<List<Double>> parameterLists;
    private boolean evaluateOnTest;

    public enum OptimizationTargetStat {ACC, F1};

    public POSTrainOptimizeParams(ResourceManager nonDefaultConfig) throws FileNotFoundException {
        finalConfig = new POSConfigurator().getConfig(nonDefaultConfig);

        trainingAndDevData = finalConfig.getString(POSConfigurator.TRAINING_AND_DEV_DATA.key);
        testData = finalConfig.getString(POSConfigurator.TEST_DATA.key);
        evaluateOnTest = finalConfig.getBoolean(POSConfigurator.EVAL_ON_TEST.key);
        optimizationTargetStat = OptimizationTargetStat.valueOf(finalConfig.getString(POSConfigurator.OPT_TARGET_STAT.key));
        /** using TestDiscrete to generate target stat for optimization, so index refers to corresponding
         * position in array returned by {@link TextDiscrete.getOverallStats()} */
        optimizationStatIndex = getOptStatIndex(optimizationTargetStat);
        int numFolds = finalConfig.getInt(POSConfigurator.NUM_FOLDS.key);
        boolean isTrainSplitRandom = finalConfig.getBoolean(POSConfigurator.IS_TRAIN_SPLIT_RANDOM.key);
        FoldParser.SplitPolicy sp = isTrainSplitRandom ? FoldParser.SplitPolicy.random : FoldParser.SplitPolicy.sequential;

        trainingParserKnown = getFoldParser(new POSBracketToToken(trainingAndDevData), numFolds, sp);
        trainingParserUnknown = getFoldParser(new POSLabeledUnknownWordParser(trainingAndDevData), numFolds, sp);

        generateTuneParameters(finalConfig.getCommaSeparatedValues(POSConfigurator.THICKNESS_PARAMS.key),
                finalConfig.getCommaSeparatedValues(POSConfigurator.LEARNING_RATE_PARAMS.key));

        knownModelFile = finalConfig.getString("knownModelPath");
        knownLexFile = finalConfig.getString("knownLexPath");
        unknownModelFile = finalConfig.getString("unknownModelPath");
        unknownLexFile = finalConfig.getString("unknownLexPath");
        baselineModelFile = finalConfig.getString("baselineModelPath");
        baselineLexFile = finalConfig.getString("baselineLexPath");
        mikheevModelFile = finalConfig.getString("mikheevModelPath");
        mikheevLexFile = finalConfig.getString("mikheevLexPath");

        reportFile = finalConfig.getString("reportFile");
        System.setOut(new PrintStream(new BufferedOutputStream(new FileOutputStream(reportFile)), true));
    }

    /**
     * select the index of the desired performance stat in the array returned by the TestDiscrete object
     * @param optimizationTargetStat enum value of performance stat
     * @return integer index of corresponding output in TestDiscrete result array
     */
    private int getOptStatIndex(OptimizationTargetStat optimizationTargetStat) {
        if (optimizationTargetStat.equals(OptimizationTargetStat.F1))
            return 2;
        else if (optimizationTargetStat.equals(OptimizationTargetStat.ACC))
            return 3;
        else
            throw new IllegalArgumentException("Invalid value '" + optimizationTargetStat.name() +
                    "' specified for tuning optimization target statistic.");
    }

    /**
     * generate all combinations of thickness/learning rate values.
     * @param thicknessVals
     * @param learningRateVals
     */
    private void generateTuneParameters(String[] thicknessVals, String[] learningRateVals) {

        List<Double> thicknessParams = getDblValues(thicknessVals);
        List<Double> learningRateParams = getDblValues(learningRateVals);

        List<List<Double>> parameters = new ArrayList<>();
        parameters.add(thicknessParams);
        parameters.add(learningRateParams);
        parameterLists = Collections.synchronizedList(Permutations.crossProduct(parameters));
        knownTuneParameters = new Learner.Parameters[parameterLists.size()];

        for (int i = 0; i < parameterLists.size(); ++i) {
            List<Double> paramInstance = parameterLists.get(i);
            SparseAveragedPerceptron.Parameters sapp = new SparseAveragedPerceptron.Parameters();
            sapp.thickness = paramInstance.get(0);
            sapp.learningRate = paramInstance.get(1);
            knownTuneParameters[i] = sapp;
            unknownTuneParameters[i] = sapp;
        }
    }

    private List<Double> getDblValues(String[] commaSeparatedValues) {
        List<Double> dblVals = new ArrayList<>(commaSeparatedValues.length);

        for (int i = 0; i < commaSeparatedValues.length; ++i) {
            dblVals.add(Double.parseDouble(commaSeparatedValues[i]));
        }

        return dblVals;
    }

    private Parser getFoldParser(Parser parser, int numFolds, FoldParser.SplitPolicy splitPolicy) {
        // Parser parser, int K, SplitPolicy split, int pivot, boolean f
        return new FoldParser(parser, numFolds, splitPolicy, 0, false);
    }

    public void tune() {
        MikheevTable.isTraining = true;
        BaselineTarget.isTraining = true;

        int numExamplesBetweenStatusMessages = 500;

        BaselineTarget baselineTarget = new BaselineTarget(baselineModelFile, baselineLexFile);
        MikheevTable mikheevTable = new MikheevTable(mikheevModelFile, mikheevLexFile);


        // tune baseline, mikheevTable once as they simply count things
        Object ex;

        logger.info("training baseline & mikheev...");

        while ((ex = trainingParserKnown.next()) != null) {
            baselineTarget.learn(ex);
            mikheevTable.learn(ex);
        }

        baselineTarget.doneLearning();
        baselineTarget.save();
        mikheevTable.doneLearning();
        mikheevTable.save();

        // tune pos models for known, unknown words

        Learner knownLearner = new POSTaggerKnown(knownModelFile, knownLexFile, baselineTarget);
        Learner unknownLearner = new POSTaggerUnknown(unknownModelFile, unknownLexFile, mikheevTable);

        BatchTrainer kbt = new BatchTrainer(knownLearner, trainingParserKnown, numExamplesBetweenStatusMessages);
        BatchTrainer ubt = new BatchTrainer(unknownLearner, trainingParserUnknown, numExamplesBetweenStatusMessages);


        //given a FoldParser, the rounds argument will be used to record the number of training rounds needed
        // for the corresponding entry in the parameters argument.
        int[] knownRounds = new int[knownTuneParameters.length];
        int[] unknownRounds = new int[unknownTuneParameters.length];

        logger.info("Tuning known pos tagger params...");
        Learner.Parameters tunedKnownParams = kbt.tune(knownTuneParameters, knownRounds, trainingParserKnown, new POSTestMetric());


        logger.info("Tuning unknown pos tagger params...");
        Learner.Parameters tunedUnknownParams = ubt.tune(unknownTuneParameters, unknownRounds, trainingParserUnknown, new POSTestMetric());

        logger.info("Generating report...");
        // need to figure out the corresponding number of rounds for each optimal parameter set
        // -- lbjava does NOT make this easy
        IntPair numKnownRoundsAndIndex = findNumRounds(tunedKnownParams, knownTuneParameters, knownRounds);
        IntPair numUnknownRoundsAndIndex = findNumRounds(tunedUnknownParams, unknownTuneParameters, unknownRounds);

        //using the optimal parameters, tune final models and evaluate on test data
        trainAndWriteLearner(knownLearner, trainingParserKnown, tunedKnownParams, numKnownRoundsAndIndex.getFirst());
        trainAndWriteLearner(unknownLearner, trainingParserUnknown, tunedUnknownParams, numUnknownRoundsAndIndex.getFirst());

        // evaluate and write results, params to file
        testAndReport(parameterLists.get(numKnownRoundsAndIndex.getSecond()), numKnownRoundsAndIndex.getFirst(),
                parameterLists.get(numUnknownRoundsAndIndex.getSecond()), numUnknownRoundsAndIndex.getFirst());
        logger.info("done reporting.");
    }

    private void testAndReport(List<Double> tunedKnownParams, int numKnownRounds, List<Double> tunedUnknownParams, int numUnknownRounds) {

        // System.out has been redirected to report file...
        System.out.println("<report>\n<knownParams>\n");
        printParams(tunedKnownParams, numKnownRounds);
        System.out.println("</knownParams>\n<unknownParams>\n");
        printParams(tunedUnknownParams, numUnknownRounds);
        System.out.println("</unknownParams>\n<performance>\n<trainingData>\n");

        TestDiscrete.testDiscrete(new TestDiscrete(), new POSTagger(), new POSLabel(),
                new POSBracketToToken(trainingAndDevData), true, 0);
        System.out.println("</trainingData>\n");
        if (evaluateOnTest) {
            System.out.println("<testData>\n");
            TestDiscrete.testDiscrete(new TestDiscrete(), new POSTagger(), new POSLabel(),
                    new POSBracketToToken(testData), true, 0);
            System.out.println("</testData>\n");
        }
        System.out.println("</performance>\n</report>\n");
    }

    private void printParams(List<Double> tunedParams, int numRounds) {
        System.out.println("<thickness>" + tunedParams.get(0) + "</thickness>\n") ;
        System.out.println("<learningRate>" + tunedParams.get(1) + "</learningRate>\n");
        System.out.println("<numRounds>" + numRounds + "</numRounds>\n");
    }


    private void trainAndWriteLearner(Learner learner, Parser trainingParser, Learner.Parameters tunedParams, int numRounds) {
        learner.forget();
        //defensive; setting the learner parameters is already done inside tune()
        learner.setParameters(tunedParams);
        trainingParserKnown.reset();

        learner.beginTraining();

        for (int i = 0; i < numRounds; ++i) {
            trainingParser.reset();
            Object ex;
            while ((ex = trainingParser.next()) != null)
                learner.learn(ex);
            learner.doneWithRound();
        }
        learner.doneLearning();
        learner.save();
    }


    private IntPair findNumRounds(Learner.Parameters tunedParams, Learner.Parameters[] paramLists, int[] roundList) {

        for (int i = 0; i < paramLists.length; ++i)
            if (paramLists[i].equals(tunedParams))
                return new IntPair(roundList[i], i);

        throw new IllegalArgumentException("No match for tunedParams found in parameter list.");
    }

    /**
     * a TestingMetric that uses the global F1 from LBJava's TestDiscrete class
     */
    public class POSTestMetric implements TestingMetric {

        private static final String NAME = "TestDiscreteMetric";
        @Override
        public String getName() {
            return NAME;
        }

        @Override
        public double test(Classifier classifier, Classifier oracle, Parser parser) {
            TestDiscrete td = TestDiscrete.testDiscrete(classifier, oracle, parser);
            return td.getOverallStats()[optimizationStatIndex];
        }
    }

    public static void main(String[] args) {
        Properties props = new Properties();
        props.setProperty(POSConfigurator.MODEL_PATH.key, "tunemodels/edu/illinois/cs/cogcomp/pos/lbjava");

        POSTrainOptimizeParams opt = null;
        try {
            opt = new POSTrainOptimizeParams(new ResourceManager(props));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        opt.tune();
    }
}
