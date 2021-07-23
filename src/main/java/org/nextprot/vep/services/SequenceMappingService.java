package org.nextprot.vep.services;

import org.nextprot.vep.domain.ProteinVariant;

import java.util.List;

public interface SequenceMappingService {

    List<ProteinVariant> getENSPMapping(String isoform, List<ProteinVariant> variants);
}
