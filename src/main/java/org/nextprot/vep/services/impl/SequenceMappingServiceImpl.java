package org.nextprot.vep.services.impl;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceLoadService;
import org.nextprot.vep.services.SequenceMappingService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Service which provides the best possible mapping of a given nextprot isoform position on to an ENSP sequence
 */
public class SequenceMappingServiceImpl implements SequenceMappingService {

    @Autowired
    SequenceLoadService sequenceLoadService;

    @Override
    public List<ProteinVariant> getENSPMapping(String isoform, List<ProteinVariant> variants) {
        SequenceMappingProfile mappingProfile = sequenceLoadService.getMappingProfile(isoform);
        int offset = mappingProfile.getOffset();
        return null;
    }
}
