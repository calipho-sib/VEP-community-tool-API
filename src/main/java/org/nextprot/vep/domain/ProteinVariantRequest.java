package org.nextprot.vep.domain;

import java.util.List;

/**
 * Represents the API request with the list of variants of a given isoform
 */
public class ProteinVariantRequest {

    private String version;

    private String isoform;

    private List<ProteinVariant> variants;

    public String getVersion() { return version;}

    public String getIsoform() {
        return isoform;
    }

    public List<ProteinVariant> getVariants() {
        return variants;
    }

    public void setVersion(String vepVersion) { this.version = vepVersion; }

    public void setIsoform(String isoform) {
        this.isoform = isoform;
    }

    public void setVariants(List<ProteinVariant> variants) {
        this.variants = variants;
    }

}
