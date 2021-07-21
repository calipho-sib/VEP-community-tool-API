package org.nextprot.vep.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Represents a single variant position on a nextprot isoform
 */
public class ProteinVariant {

    private int nextprotPosition;

    private int enspPosition;

    @JsonProperty("original-amino-acid")
    private String originalAminoAcid;

    @JsonProperty("variant-amino-acid")
    private String variantAminoAcid;

    private float SIFT;

    private float polyphen;

    public ProteinVariant(int position, String originalAminoAcid, String variantAminoAcid) {
        this.nextprotPosition = position;
        this.originalAminoAcid = originalAminoAcid;
        this.variantAminoAcid = variantAminoAcid;
    }

    public int getNextprotPosition() {
        return this.nextprotPosition;
    }

    public int getEnspPosition() {
        return this.enspPosition;
    }

    public String getOriginalAminoAcid() {
        return this.originalAminoAcid;
    }

    public String getVariantAminoAcid() {
        return  this.variantAminoAcid;
    }

    public float getSIFT() {
        return this.SIFT;
    }

    public float getPolyphen(){
        return this.polyphen;
    }

    public void setEnspPosition(int enspPosition) {
        this.enspPosition = enspPosition;
    }

    public void setSIFT(float SIFT) {
        this.SIFT = SIFT;
    }

    public void setPolyphen(float polyphen) {
        this.polyphen = polyphen;
    }

}
