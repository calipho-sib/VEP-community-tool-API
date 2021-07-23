package org.nextprot.vep.services;

import org.nextprot.vep.domain.SequenceMappingProfile;

/**
 * Loads the sequence profile for given sequence pair and provides the best possible mapping of a given
 * nextprot isoform position on to an ENSP sequence
 *
 */
public interface SequenceMappingService {
    SequenceMappingProfile getMappingProfile(String isoform);
}
