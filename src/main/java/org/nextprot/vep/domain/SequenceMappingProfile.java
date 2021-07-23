package org.nextprot.vep.domain;

import java.io.Serializable;

/**
 * Mapping profile, which maintains;
 * - an offset :
 *         -- 0 if both sequences are same
 *         -- x offset to be applied to map
 * - if the sequences are not equal maintains an aligner : Not Implemented yet
 */
public class SequenceMappingProfile implements Serializable {

    // Offset to be applied to get the mapped position
    // This is the mapping mechanism, when the nextprot sequence is a substring of ensp sequence
    private int offset;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOffset() {
        return this.offset;
    }

    public String toString() {
        return Integer.toString(offset);
    }
}
