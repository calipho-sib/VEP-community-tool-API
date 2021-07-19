package org.nextprot.vep;

/**
 * Represents a single variant position on a protein
 */
public class ProteinVariant {

    private int position;

    private String originalAminoAcid;

    private String variantAminoAcid;

    private float SIFT;

    private float polyphen;

    public ProteinVariant(int position, String originalAminoAcid, String variantAminoAcid) {
        this.position = position;
        this.originalAminoAcid = originalAminoAcid;
        this.variantAminoAcid = variantAminoAcid;
    }

    public int getPosition() {
        return this.position;
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

    public void setSIFT(float SIFT) {
        this.SIFT = SIFT;
    }

    public void setPolyphen(float polyphen) {
        this.polyphen = polyphen;
    }
}
