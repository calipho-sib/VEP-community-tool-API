package org.nextprot.vep.domain;

/**
 * Represents a single variant position on a nextprot isoform
 */
public class ProteinVariant {

    private int nextprotPosition;

    private int enspPosition;

    private String originalAminoAcid;

    private String variantAminoAcid;

    private String sift = "-";

    private String polyphen = "-";

    private String siftPrediction;

    private String polyphenPrediction;

    private VariantType type;

    private String status;

    private String message;

    public static String RESULTS_FOUND = "OK";

    public static String ERROR = "ERROR";


    public ProteinVariant(int position, VariantType type, String originalAminoAcid, String variantAminoAcid) {
        this.type = type;
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

    public String getSift() {
        return this.sift;
    }

    public String getPolyphen(){
        return this.polyphen;
    }

    public String getSiftPrediction() { return this.siftPrediction; }

    public String getPolyphenPrediction() { return this.polyphenPrediction; }

    public String getStatus() {
        return this.status;
    }

    public void setEnspPosition(int enspPosition) {
        this.enspPosition = enspPosition;
    }

    public void setSift(String sift) {
        this.sift = sift;
    }

    public void setPolyphen(String polyphen) {
        this.polyphen = polyphen;
    }

    public void setSiftPrediction(String siftPrediction) { this.siftPrediction = siftPrediction; }

    public void setPolyphenPrediction(String polyphenPrediction) { this.polyphenPrediction = polyphenPrediction; }

    public void setStatus(String status) {
        this.status = status;
    }
}