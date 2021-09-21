package org.nextprot.vep.domain;

/**
 * Represents a single variant position on a nextprot isoform
 */
public class ProteinVariant {

    private int nextprotPosition;

    private int enspPosition;

    private String originalAminoAcid;

    private String variantAminoAcid;

    private double sift = -1;

    private double polyphen = -1;

    private String siftPrediction;

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

    public double getSift() {
        return this.sift;
    }

    public double getPolyphen(){
        return this.polyphen;
    }

    public String getSiftPrediction() { return this.siftPrediction; }

    public String getPolyphenPrediction() { return this.polyphenPrediction; }

    public void setEnspPosition(int enspPosition) {
        this.enspPosition = enspPosition;
    }

    public void setSift(double sift) {
        this.sift = sift;
    }

    public void setPolyphen(double polyphen) {
        this.polyphen = polyphen;
    }

    public void setSiftPrediction(String siftPrediction) { this.siftPrediction = siftPrediction; }

    public void setPolyphenPrediction(String polyphenPrediction) { this.polyphenPrediction = polyphenPrediction; }
}
