package org.nextprot.vep.services.impl;

import org.nextprot.vep.domain.ProteinVariant;
import org.nextprot.vep.services.SequenceMappingService;
import org.springframework.beans.factory.annotation.Value;

import javax.annotation.PostConstruct;
import java.io.File;
import java.net.URISyntaxException;
import java.util.List;

/**
 * Service which provides the best possible mapping of a given nextprot isoform position on to an ENSP sequence
 */
public class SequenceMappingServiceImpl implements SequenceMappingService {

    @Value("${SEQUENCE_DATA_FILE}")
    private String sequenceDataFilename;

    @PostConstruct
    /**
     * Loads nextprot and ENSP sequence data from a CSV file and maintain it in memory for mapping
     */
    public void loadENSPData() {
        try {
            File sequenceDataFile = new File(this.getClass().getClassLoader().getResource(sequenceDataFilename).toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<ProteinVariant> getENSPMapping(List<ProteinVariant> variants) {
        return null;
    }
}
