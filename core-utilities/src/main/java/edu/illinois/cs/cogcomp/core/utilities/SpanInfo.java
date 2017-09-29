/**
 * This software is released under the University of Illinois/Research and Academic Use License. See
 * the LICENSE file in the root folder for details. Copyright (c) 2016
 *
 * Developed by: The Cognitive Computation Group University of Illinois at Urbana-Champaign
 * http://cogcomp.cs.illinois.edu/
 */
package edu.illinois.cs.cogcomp.core.utilities;

/**
 * Created by mssammon on 9/25/17.
 */

import edu.illinois.cs.cogcomp.core.datastructures.IntPair;
import edu.illinois.cs.cogcomp.core.datastructures.Pair;

import java.io.Serializable;
import java.util.Map;

/**
 * a structure to store span information: label, offsets, attributes (including value offsets)
 */
public class SpanInfo implements Serializable {

    public final String label;
    public final IntPair spanOffsets;
    public final Map<String, Pair<String, IntPair>> attributes;

    public SpanInfo(String label, IntPair spanOffsets, Map<String, Pair<String, IntPair>> attributes ) {
        this.label = label;
        this.spanOffsets = spanOffsets;
        this.attributes = attributes;
    }
}
