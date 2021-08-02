package org.nextprot.vep.services.impl;

import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

@Service
public class IOSequenceMappingServiceImpl implements SequenceMappingService {

    @Value("${SEQUENCE_DATA_FILE}")
    private String sequenceDataFilename;

    // Map to keep the sequences for isoforms
    private Map<String, String[]> sequenceMap = new HashMap<>();

    // Map to keep ensps for isoforms
    private Map<String, String> enspMap = new HashMap<>();

    @PostConstruct
    /**
     * Loads nextprot and ENSP sequence data from a CSV file and maintain it in memory for mapping
     */
    public void loadENSPData() {
        try {
            File sequenceDataFile = new File(this.getClass().getClassLoader().getResource(sequenceDataFilename).toURI());
            BufferedReader bufferedInputStream = new BufferedReader(new FileReader(sequenceDataFile));
            String sequenceData = null;
            int loadedIsoforms = 0;
            while((sequenceData =  bufferedInputStream.readLine()) != null) {
                String isoform = sequenceData.split(",")[5];
                String ensp = sequenceData.split(",") [3];
                String enspSequence = sequenceData.split(",")[4];
                String nextprotSequence = sequenceData.split(",")[6];
                String[] sequences = new String[2];
                sequences[0] = enspSequence;
                sequences[1] = nextprotSequence;

                // Updates the sequence map
                sequenceMap.put(isoform, sequences);

                // Updates the ensp map
                enspMap.put(isoform, ensp);

                loadedIsoforms++;
            }
            System.out.println("Loaded isoforms " + loadedIsoforms);
        } catch (URISyntaxException | FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(key= "#isoform", value = "annotations", sync = true)
    public SequenceMappingProfile getMappingProfile(String isoform) {
        if(!sequenceMap.keySet().contains(isoform)) {
            return null;
        }

        String nextprotSequence = sequenceMap.get(isoform)[0];
        String enspSequence = sequenceMap.get(isoform)[1];
        if(nextprotSequence == null || enspSequence == null) {
            return null;
        }

        SequenceMappingProfile sequenceMappingProfile = new SequenceMappingProfile();
        sequenceMappingProfile.setEnsp(enspMap.get(isoform));
        if(nextprotSequence.equals(enspSequence)) {
            sequenceMappingProfile.setOffset(0);
        } else if(enspSequence.contains(nextprotSequence)) {
            // Nextprot sequence is a substring of ensp sequence
            sequenceMappingProfile.setOffset(enspSequence.indexOf(nextprotSequence));
        } else if(nextprotSequence.contains(enspSequence)) {
            // Nextprot sequence is a substring of ensp sequence
            sequenceMappingProfile.setOffset(-1);
        }

        return sequenceMappingProfile;
    }
}
