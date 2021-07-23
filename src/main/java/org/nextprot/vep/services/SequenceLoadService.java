package org.nextprot.vep.services;

import org.nextprot.vep.domain.SequenceMappingProfile;

/**
 * Loads the sequence profile for given sequence pair
 */
public interface SequenceLoadService {
    SequenceMappingProfile getMappingProfile(String isoform);
}
