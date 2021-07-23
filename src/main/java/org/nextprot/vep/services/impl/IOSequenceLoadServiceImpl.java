package org.nextprot.vep.services.impl;

import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceLoadService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

public class IOSequenceLoadServiceImpl implements SequenceLoadService {

    @Value("${SEQUENCE_DATA_FILE}")
    private String sequenceDataFilename;

    private Map<String, String[]> sequenceMap = new HashMap<>();

    @PostConstruct
    /**
     * Loads nextprot and ENSP sequence data from a CSV file and maintain it in memory for mapping
     */
    public void loadENSPData() {
        try {
            File sequenceDataFile = new File(this.getClass().getClassLoader().getResource(sequenceDataFilename).toURI());
            BufferedReader bufferedInputStream = new BufferedReader(new FileReader(sequenceDataFile));
            String sequenceData = null;
            while((sequenceData =  bufferedInputStream.readLine()) != null) {
                String isoform = sequenceData.split(",")[5];
                String enspSequence = sequenceData.split(",")[4];
                String nextprotSequence = sequenceData.split(",")[6];
                String[] sequences = new String[2];
                sequences[0] = enspSequence;
                sequences[1] = nextprotSequence;
                sequenceMap.put(isoform, sequences);
            }
        } catch (URISyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(key= "#isoform", value = "annotations", sync = true)
    public SequenceMappingProfile getMappingProfile(String isoform) {
        String nextprotSequence = sequenceMap.get(isoform)[0];
        String enspSequence = sequenceMap.get(isoform)[1];
        if(nextprotSequence == null || enspSequence == null) {
            return null;
        }

        SequenceMappingProfile sequenceMappingProfile = new SequenceMappingProfile();
        if(nextprotSequence.equals(enspSequence)) {
            sequenceMappingProfile.setOffset(0);
        }

        // Nextprot sequence is a substring of ensp sequence
        if(enspSequence.contains(nextprotSequence)) {
            sequenceMappingProfile.setOffset(enspSequence.indexOf(nextprotSequence));
        }

        // Nextprot sequence is a substring of ensp sequence
        if(nextprotSequence.contains(enspSequence)) {
            sequenceMappingProfile.setOffset(-1);
        }

        return sequenceMappingProfile;
    }
}
