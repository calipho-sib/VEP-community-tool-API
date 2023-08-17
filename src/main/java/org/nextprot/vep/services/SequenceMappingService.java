package org.nextprot.vep.services;

import org.nextprot.vep.domain.SequenceMappingProfile;

import java.util.List;
import java.util.Map;

/**
 * Loads the sequence profile for given sequence pair and provides the best possible mapping of a given
 * nextprot isoform position on to an ENSP sequence
 *
 */
public interface SequenceMappingService {
    /**
     * Returns the mapping profiles for a given isoform
     * i.e how the nextprot isoform is mapped with ENST, ENSP sequences
     * Note: There can be multiple profiles for a given isoform
     * @param isoform
     * @return SequenceMappingProfile
     */
    List<SequenceMappingProfile> getMappingProfilesForIsoform(String isoform);

    /**
     * Returns a list of sequence mapping profiles of the isoforms of a given entry
     * @param entry
     * @return List<SequenceMappingProfile>
     */
    List<SequenceMappingProfile> getMappingProfiles(String entry);

    /**
     * Returns the mapping profiles
     * @return
     */
    String getAllMappingProfiles();
}
