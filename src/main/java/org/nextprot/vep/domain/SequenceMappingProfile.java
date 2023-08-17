package org.nextprot.vep.domain;

import jaligner.Alignment;
import org.nextprot.vep.services.impl.IOSequenceMappingServiceImpl;
import org.nextprot.vep.utils.PamAligner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;

/**
 * Mapping profile, which maintains a profile of ENST, ENSP ids corresponding to an isoform with an offset;
 * - an offset :
 *         -- 0 if both sequences are same
 *         -- x offset to be applied to map
 * - if the sequences are not equal maintains an aligner : Not Implemented yet
 * Note: there can be multiple mapping profiles for a given isoform
 */
public class SequenceMappingProfile implements Serializable {

    Logger logger = LoggerFactory.getLogger(SequenceMappingProfile.class);
    private String ensg;
    private String enst;
    private String ensp;
    private String enspSequence;
    private String isoform;
    private String isoformSequence;

    // Offset to be applied to get the mapped position
    // This is the mapping mechanism, when the nextprot sequence is a substring of ensp sequence
    private int offset;

    // Alignment results from pamaligner
    private String alignmentResults;

    public SequenceMappingProfile(SequenceMappingProfileBuilder mappingProfileBuilder) {
        this.ensg = mappingProfileBuilder.ensg;
        this.enst = mappingProfileBuilder.enst;
        this.ensp = mappingProfileBuilder.ensp;
        this.enspSequence = mappingProfileBuilder.enspSequence;
        this.isoform = mappingProfileBuilder.isoform;
        this.isoformSequence = mappingProfileBuilder.isoformSequence;

        if (isoformSequence != null && enspSequence != null && !isoformSequence.equals("-") && !enspSequence.equals("-")) {
            // Calculates the offset and alignment
            if(isoformSequence.equals(enspSequence)) {
                logger.info("Exact Match: NP" + isoformSequence.length() + "ENSP" + enspSequence);
                this.offset = 0;
            } else if(enspSequence.contains(isoformSequence)) {
                // ensp sequence is a substring of nextprot sequence
                logger.info("NP substring: NP " + isoformSequence + " ENSP " + enspSequence);
                this.offset = enspSequence.indexOf(isoformSequence);
            } else if(isoformSequence.contains(enspSequence)) {
                // Nextprot sequence is a substring of ensp sequence
                logger.info("ENSP substring: NP " + isoformSequence + " ENSP " + enspSequence);
                this.offset = -1;
            } else {
                logger.info("Different sequences: NP " + isoformSequence + " ENSP " + enspSequence);
                // Have to do the alignment
                PamAligner aligner = new PamAligner("nextprot", isoformSequence, "ensp", enspSequence);
                Alignment alignment = aligner.getAlignment();
                String alignmentResult = aligner.getS1Identities() + "," + aligner.getS1InnerGapCount() + "," +
                        aligner.getS2InnerGapCount() + "," + aligner.getInnerGapCount() + "," +
                        alignment.getLength() + "," + alignment.getIdentity() + "," +
                        alignment.getSimilarity() + "," + alignment.getStart1() + "," +
                        alignment.getStart2() + "," + alignment.getGaps1() + "," +
                        alignment.getGaps2() + "," + alignment.getScore() + "," +
                        alignment.getScoreWithNoTerminalGaps()  + "," +
                        new String(alignment.getSequence1()) + "," +
                        new String(alignment.getMarkupLine()) + "," +
                        new String(alignment.getSequence2());
                logger.info("NP seq length " + isoformSequence.length() + " ENSP seq length " + enspSequence);
                this.offset = -2;
                this.alignmentResults = alignmentResult;
            }
        }
    }


    public String getEnsg() { return this.ensg; }

    public String getEnsp() {
        return this.ensp;
    }

    public String getEnspSequence() { return this.enspSequence; }

    public String getEnst() { return  this.enst; }

    public String getIsoform() { return this.isoform; }

    public String getIsoformSequence() { return this.isoformSequence; }

    public int getOffset() {
        return this.offset;
    }

    public String getAlignmentResults() {
        return this.alignmentResults;
    }

    public static class SequenceMappingProfileBuilder {
        private String ensg;
        private String enst;
        private String ensp;
        private String enspSequence;
        private String isoform;
        private String isoformSequence;

        public SequenceMappingProfileBuilder ensg(String ensg) {
            this.ensg = ensg;
            return this;
        }

        public SequenceMappingProfileBuilder enst(String enst) {
            this.enst = enst;
            return this;
        }

        public SequenceMappingProfileBuilder ensp(String ensp) {
            this.ensp = ensp;
            return this;
        }

        public SequenceMappingProfileBuilder enspSequence(String enspSequence) {
            this.enspSequence = enspSequence;
            return this;
        }

        public SequenceMappingProfileBuilder isoform(String isoform) {
            this.isoform = isoform;
            return this;
        }

        public SequenceMappingProfileBuilder isoformSequence(String isoformSequence) {
            this.isoformSequence = isoformSequence;
            return this;
        }

        public SequenceMappingProfile build() {
            return new SequenceMappingProfile(this);
        }
    }


}
