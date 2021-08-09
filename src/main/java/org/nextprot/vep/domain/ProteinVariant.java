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

    private double SIFT;

    private double polyphen;

    private String SIFTPrediction;

    private String polyphenPrediction;

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

    public double getSIFT() {
        return this.SIFT;
    }

    public double getPolyphen(){
        return this.polyphen;
    }

    public String getSIFTPrediction() { return this.SIFTPrediction; }

    public String getPolyphenPrediction() { return this.polyphenPrediction; }

    public void setEnspPosition(int enspPosition) {
        this.enspPosition = enspPosition;
    }

    public void setSIFT(double SIFT) {
        this.SIFT = SIFT;
    }

    public void setPolyphen(double polyphen) {
        this.polyphen = polyphen;
    }

    public void setSIFTPrediction(String SIFTPrediction) { this.SIFTPrediction = SIFTPrediction; }

    public void setPolyphenPrediction(String polyphenPrediction) { this.polyphenPrediction = polyphenPrediction; }
}
