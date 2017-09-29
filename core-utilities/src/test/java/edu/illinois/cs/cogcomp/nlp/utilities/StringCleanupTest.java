/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.nlp.utilities;

import junit.framework.TestCase;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class StringCleanupTest {
    private static final String utf8RefStr = "A𝔊BC ﾁｮｺﾚｰﾄ —interet”";
    private static final String latin1RefStr = "-interet\"";
    private static final String asciiRefStr = "-interet\"";
    private static final String ctrlRefStr = "TestString";
    private static final String normalizeExample = "The 13 words you can’t write about Hillary Clinton anymore\n" +
            "Hillary Clinton has been in the public eye for a very long time, which means much has been written about her -- including quite a few adjectives. But some of these adjectives are now off-limits.\n" +
            "That's according to the Clinton \"Super Volunteers,\" who have promised to track the media's use of words they believe to be sexist code words.\n" +
            "The 13 words you can t write about Hillary Clinton anymore - The Washington Post\n" +
            "Here are these words, if you don`t want to read article: \"polarizing,\" \"calculating,\" \"disingenuous,\" \"insincere,\" \"ambitious,\" \"inevitable,\" \"entitled,\" \"over-confident,\" \"secretive,\" \"will do anything to win,\" \"represents the past,\" and \"out of touch.\"\n" +
            "Also apparently off the table: \"tone deaf\" -- at least according to a new Twitter account\n" +
            "That`s right, all words characterizing Hillary are now off-limits. What words we should use?\n" +
            "And what about other candidates? What words about Bush, Cruz, Romney, Carson, Walker?\n" +
            "I think about a half the words in a language could be banned if they follow this example\n" +
            "You forgot........BENGHAZI\n" +
            "Who banned the words?\n" +
            "Sounds like that libral media up to their old tricks again\n" +
            "Seems some words need added.\n" +
            "Benghazi, as already pointed out. Whitewater, Monica, BJ, infidelity, Foster, health care reform, sniper fire, Sir Edmund Hillary, head trauma, Iraq war vote, support for the Iraq war, email, servers, lesbians........\n" +
            "Yeah, especially the words \"to be\".\n" +
            "The marching orders came from \"Clinton Super Volunteers\". It gives the Hildebeast Plausible Deniability.\n" +
            "Marching orders? Did these volunteers order the nation's media to stop using the words? I'd love to see that order. Got a copy?\n" +
            "How about dumb ole ****?\n" +
            "\"Propaganda must always address itself to the broad masses of the people. (...) All propaganda must be presented in a popular form and must fix its intellectual level so as not to be above the heads of the least intellectual of those to whom it is directed. (...) The art of propaganda consists precisely in being able to awaken the imagination of the public through an appeal to their feelings, in finding the appropriate psychological form that will arrest the attention and appeal to the hearts of the national masses. The broad masses of the people are not made up of diplomats or professors of public jurisprudence nor simply of persons who are able to form reasoned judgment in given cases, but a vacillating crowd of human children who are constantly wavering between one idea and another. (...) The great majority of a nation is so feminine in its character and outlook that its thought and conduct are ruled by sentiment rather than by sober reasoning. This sentiment, however, is not complex, but simple and consistent. It is not highly differentiated, but has only the negative and positive notions of love and hatred, right and wrong, truth and falsehood.\"\n" +
            "\"Propaganda must not investigate the truth objectively and, in so far as it is favourable to the other side, present it according to the theoretical rules of justice; yet it must present only that aspect of the truth which is favourable to its own side. (...) The receptive powers of the masses are very restricted, and their understanding is feeble. On the other hand, they quickly forget. Such being the case, all effective propaganda must be confined to a few bare essentials and those must be expressed as far as possible in stereotyped formulas. These slogans should be persistently repeated until the very last individual has come to grasp the idea that has been put forward. (...) Every change that is made in the subject of a propagandist message must always emphasize the same conclusion. The leading slogan must of course be illustrated in many ways and from several angles, but in the end one must always return to the assertion of the same formula.\" ~~ Adolf Hitler, Mein Kampf\n";
    private static Logger logger = LoggerFactory.getLogger(StringCleanupTest.class);
    private static String suppSample = "A" + "\uD835\uDD0A" + "B" + "C";
    private static String halfWidthKatanaSample = "\uff81\uff6e\uff7a\uff9a\uff70\uff84";
    private static String diacriticSample = "—intérêt”";
    private static String ctrlSample = "Test" + String.valueOf((char) 3) + "String";
    private static String combinedStr = suppSample + " " + halfWidthKatanaSample + " "
            + diacriticSample;
//    protected void setUp() throws Exception {
//        super.setUp();
//    }
//
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }

    @Test
    public void testStringCleanup() {
        String inStr = combinedStr;
        String utf8Str = StringCleanup.normalizeToUtf8(inStr);

        logger.info("Normalized to UTF-8:");
        logger.info(utf8Str);

        assertEquals(utf8RefStr, utf8Str);

        String latin1Str = StringCleanup.normalizeToLatin1(diacriticSample);

        logger.info("Normalized to Latin1:");
        logger.info(latin1Str);

        assertEquals(latin1RefStr, latin1Str);

        String asciiStr = StringCleanup.normalizeToLatin1(diacriticSample);

        logger.info("Normalized to ascii:");
        logger.info(asciiStr);

        assertEquals(asciiRefStr, asciiStr);

        String withoutCtrlCharStr = StringCleanup.removeControlCharacters(ctrlSample);

        logger.info("Removed Control Characters:");
        logger.info(withoutCtrlCharStr);

        assertEquals(ctrlRefStr, withoutCtrlCharStr);

        // this tests whether normalize() throws an exception on input text
        String cleanedNormEx = StringCleanup.normalizeToLatin1(normalizeExample);
        assertNotNull(cleanedNormEx);
    }
}
