/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.pos;

import edu.illinois.cs.cogcomp.core.utilities.configuration.Configurator;
import edu.illinois.cs.cogcomp.core.utilities.configuration.Property;
import edu.illinois.cs.cogcomp.core.utilities.configuration.ResourceManager;

import java.io.IOException;

/**
 * Constant values used by the LBJ source file.
 *
 * @author Nick Rizzolo
 **/
public class POSConfigurator extends Configurator {
    /** A configurable prefix. This MUST be set by user when training/evaluating POS tagger -- point
     *  it to the directory containing the corpus in paren format.*/
    public static final Property CORPUS_PREFIX = new Property("corpusPrefix",
            "/shared/corpora/corporaWeb/written/eng/POS_HYPHEN_SPLIT/");
//            "/shared/experiments/mssammon/workspace-github/cogcomp-nlp/pos/corpus-paren-format/");

    /** The file containing the training set. */
    public static final Property TRAINING_DATA = new Property("trainingData", CORPUS_PREFIX.value
            + "00-18.br");
    /** The file containing the development set. */
    public static final Property DEV_DATA = new Property("devData", CORPUS_PREFIX.value
            + "19-21.br");
    /** The file containing the test set. */
    public static final Property TEST_DATA = new Property("testData", CORPUS_PREFIX.value
            + "22-24.br");
    /** The file containing the training <i>and</i> development sets. */
    public static final Property TRAINING_AND_DEV_DATA = new Property("trainingAndDevData",
            CORPUS_PREFIX.value + "00-21.br");

    public static final Property BASELINE_NAME = new Property("baselineName", "BaselineTarget");
    public static final Property MIKHEEV_NAME = new Property("mikheevName", "MikheevTable");
    public static final Property KNOWN_NAME = new Property("knownName", "POSTaggerKnown");
    public static final Property UNKNOWN_NAME = new Property("unknownName", "POSTaggerUnknown");

    public static final Property MODEL_PATH = new Property("modelPath",
            "models/edu/illinois/cs/cogcomp/pos/lbjava/");
    public static final Property BASELINE_MODEL_PATH = new Property("baselineModelPath",
            MODEL_PATH.value + BASELINE_NAME.value + ".lc");
    public static final Property BASELINE_LEX_PATH = new Property("baselineLexPath",
            MODEL_PATH.value + BASELINE_NAME.value + ".lex");
    public static final Property MIKHEEV_MODEL_PATH = new Property("mikheevModelPath",
            MODEL_PATH.value + MIKHEEV_NAME.value + ".lc");
    public static final Property MIKHEEV_LEX_PATH = new Property("mikheevLexPath", MODEL_PATH.value
            + MIKHEEV_NAME.value + ".lex");
    public static final Property KNOWN_MODEL_PATH = new Property("knownModelPath", MODEL_PATH.value
            + KNOWN_NAME.value + ".lc");
    public static final Property KNOWN_LEX_PATH = new Property("knownLexPath", MODEL_PATH.value
            + KNOWN_NAME.value + ".lex");
    public static final Property UNKNOWN_MODEL_PATH = new Property("unknownModelPath",
            MODEL_PATH.value + UNKNOWN_NAME.value + ".lc");
    public static final Property UNKNOWN_LEX_PATH = new Property("unknownLexPath", MODEL_PATH.value
            + UNKNOWN_NAME.value + ".lex");
    public static final Property NUM_FOLDS = new Property("numFolds", "5");
    public static final Property IS_TRAIN_SPLIT_RANDOM = new Property("isTrainSplitRandom", "true");
    /** what statistic to use when tuning classifier performance: ACC or F1 */
    public static final Property OPT_TARGET_STAT = new Property("performanceTargetStat", "ACC");
    public static final Property THICKNESS_PARAMS = new Property("thicknessParams", "2,4,8,16,32");
    public static final Property LEARNING_RATE_PARAMS = new Property("learningRateParams", "0.05,0.1,0.2,0.4");
    /** after tuning, whether to evaluate on test data as well as on training data */
    public static final Property EVAL_ON_TEST = new Property("evaluateOnTestData", "false");


    @Override
    public ResourceManager getDefaultConfig() {
        Property[] props =
                {CORPUS_PREFIX, TRAINING_DATA, DEV_DATA, TEST_DATA, TRAINING_AND_DEV_DATA,
                        BASELINE_NAME, MIKHEEV_NAME, KNOWN_NAME, UNKNOWN_NAME, MODEL_PATH,
                        BASELINE_MODEL_PATH, MIKHEEV_MODEL_PATH, KNOWN_MODEL_PATH, KNOWN_LEX_PATH,
                        UNKNOWN_MODEL_PATH, UNKNOWN_LEX_PATH, BASELINE_LEX_PATH, MIKHEEV_LEX_PATH,
                        NUM_FOLDS, IS_TRAIN_SPLIT_RANDOM, OPT_TARGET_STAT, THICKNESS_PARAMS,
                        LEARNING_RATE_PARAMS, EVAL_ON_TEST
                };
        return new ResourceManager(generateProperties(props));
    }

}
