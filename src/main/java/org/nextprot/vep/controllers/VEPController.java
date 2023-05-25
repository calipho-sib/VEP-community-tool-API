package org.nextprot.vep.controllers;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.ProteinVariantRequest;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * VEP Controller, which provides the SIFT and Polyphen values using sequence mapping service and VEP API service
 */
@RestController
public class VEPController {

    @Qualifier("APIService")
    @Autowired
    VEPAPIService vepapiService;

    @Autowired
    SequenceMappingService sequenceMappingService;

    /**
     * Calls the Sequence mapping service to get the neXtProt to EnsEMBL mappings and calls the EnsEMBL REST API to get
     * the SIFT and Polyphen values for the requested variants
     * @param variantRequest  List of variants in a given neXtProt isoform in the form of (location, originalAA, variantAA)
     * @return SIFT and Polyphen values for the requested variants
     */
    @PostMapping("/vep-api/vep-results")
    public List<ProteinVariant> getVEPResults(@RequestBody ProteinVariantRequest variantRequest) {
        return vepapiService.getVEPResults(variantRequest.getIsoform(), variantRequest.getVariants());
    }

    /**
     * Returns a list of sequence mapping profiles given an entry
     * If the offset is less than 0 mapping does not exist
     * @param entry
     * @return List<SequenceMappingProfile>
     */
    @GetMapping("/vep-api/mapping-isoforms/{entry}")
    public List<SequenceMappingProfile> getMappableIsoforms(@PathVariable String entry) {
        return sequenceMappingService.getMappingProfiles(entry);
    }

    @RequestMapping(value="/vep-api/mapping-profiles", produces = "application/text")
    public String getAllMappingProfiles() {
        return sequenceMappingService.getAllMappingProfiles();
    }
}