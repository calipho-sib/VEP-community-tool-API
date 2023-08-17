package org.nextprot.vep.services.impl;

import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class IOSequenceMappingServiceImpl implements SequenceMappingService {

    Logger logger = LoggerFactory.getLogger(IOSequenceMappingServiceImpl.class);

    @Value("${SEQUENCE_DATA_FILE}")
    private String sequenceDataFilename;

    final
    ResourceLoader resourceLoader;

    // Map to keep the isoform and ENSP sequences for isoform
    // Note that there can be multiple ENSPs for a given isoform
    private Map<String, List<SequenceMappingProfile>> sequenceMap = new HashMap<>();

    // Map to keep ensps for isoforms
    private Map<String, String> enspMap = new HashMap<>();

    // Map to keep enst for isoforms
    private Map<String, String> enstMap = new HashMap<>();

    // Map to keep ensg for isoforms
    private Map<String, String> ensgMap = new HashMap<>();

    // Map to keep isoforms for entries
    private Map<String, List<String>> entryIsoformMap = new HashMap<>();

    public IOSequenceMappingServiceImpl(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }


    @PostConstruct
    /**
     * Loads nextprot and ENSP sequence data from a CSV file and maintain it in memory for mapping
     */
    public void loadENSPData() {
        try {

            logger.info("Loading data from " + sequenceDataFilename);
            Resource resource = resourceLoader.getResource("classpath:" + sequenceDataFilename);
            BufferedReader bufferedInputStream = new BufferedReader(new InputStreamReader(resource.getInputStream()));
            String sequenceData = null;
            int loadedIsoforms = 0;
            while((sequenceData =  bufferedInputStream.readLine()) != null) {
                System.out.println(sequenceData);
                String ensgField = sequenceData.split(",")[3];

                String entry = sequenceData.split(",")[0];
                String isoform = "-";
                String nextprotSequence = "-";
                String ensg = "NO_NP_ENSG";
                String enst = "NO_ENSEMBL_ENST";
                String ensp = "NO_ENSEMBL_ENSP";
                String enspSequence = "-";

                if(entry.equals("NX_P06213")) {
                    int i = 0;
                }

                if(!ensgField.equals("NO_NP_ENSG")) {
                    isoform = sequenceData.split(",")[1];
                    nextprotSequence = sequenceData.split(",")[2];
                    ensg = sequenceData.split(",") [3];

                    enst = sequenceData.split(",") [4];

                    if(!enst.equals("NO_ENSEMBL_ENST")) {
                        if(sequenceData.split(",").length > 5) {
                            ensp = sequenceData.split(",") [5];
                            if(sequenceData.split(",").length > 6) {
                                enspSequence = sequenceData.split(",")[6];
                            }
                        }
                    }

                }

                SequenceMappingProfile sequenceMappingProfile = new SequenceMappingProfile
                        .SequenceMappingProfileBuilder()
                        .ensg(ensg)
                        .enst(enst)
                        .ensp(ensp)
                        .enspSequence(enspSequence)
                        .isoform(isoform)
                        .isoformSequence(nextprotSequence)
                        .build();

                // Updates the sequence map
                List<SequenceMappingProfile> mappingProfileList = sequenceMap.getOrDefault(entry, new ArrayList<>());
                mappingProfileList.add(sequenceMappingProfile);
                sequenceMap.put(entry, mappingProfileList);

                loadedIsoforms++;
            }
            logger.info("Loaded isoforms " + loadedIsoforms);
        } catch ( FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Cacheable(key= "#isoform", sync = true)
    public List<SequenceMappingProfile> getMappingProfilesForIsoform(String isoform) {
        // Derive the entry
        String entry = isoform.split("-")[0];

        List<SequenceMappingProfile> mappingProfiles = sequenceMap.get(entry);
        if(mappingProfiles == null) {
            return null;
        }

        return mappingProfiles.stream()
                .filter(mappingProfile -> isoform.equals(mappingProfile.getIsoform()))
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(key= "#entry", sync = true)
    public List<SequenceMappingProfile> getMappingProfiles(String entry) {
        List<SequenceMappingProfile> mappingProfiles = sequenceMap.get(entry);
        if(mappingProfiles == null) {
            logger.info("No isoforms found for the given entry " + entry);
            return new ArrayList<>();
        } else {
            return mappingProfiles;
        }
    }

    /**
     * Iterates over all the isoforms and determins if the nextprot and ENSP sequences are;
     *  1. Exactly the same
     *  2. neXtProt sequence is a subsequence of ENSP sequence
     *  3. ENSP sequence sequence is a subsequence of neXtProt
     *  3. Sequences don't match, hence to use the alignment algorithm
     * @return Mapping profile as a CSV line
     */
    public String getAllMappingProfiles() {
        List<String> results = new ArrayList<>();
        /*for(String entry: entryIsoformMap.keySet()){
            logger.info("Processing entry " + entry);
            List<String> result = entryIsoformMap.get(entry)
                .stream()
                .map(isoform -> {
                    SequenceMappingProfile mappingProfile = getMappingProfile(isoform);
                    String ensp = mappingProfile != null ? mappingProfile.getEnsp() : "-";
                    int offset = mappingProfile != null ? mappingProfile.getOffset() : -3;
                    String alignmentResult = mappingProfile != null ? mappingProfile.getAlignmentResults() : "ALIGNMENT NOT POSSIBLE";
                    String npSeq = sequenceMap.get(isoform) != null ? sequenceMap.get(isoform)[0] : "NO_NP_SEQ";
                    String enspSeq = sequenceMap.get(isoform) != null ? sequenceMap.get(isoform)[1] : "NO_ENSP_SEQ";

                    String ensg = ensgMap.get(isoform);
                    String enst = enstMap.get(isoform);
                    return entry + ","
                            + ensg + ","
                            + enst + ","
                            + isoform + "," + npSeq
                            + "," + ensp + "," + enspSeq + ","
                            + sequenceComaprison(offset) + "," + alignmentResult;
                })
                .collect(Collectors.toList());
            results.addAll(result);
        }*/
        return String.join("\n", results);
    }

    private String sequenceComaprison(int offset) {
        if(offset == 0) {
            return "EXACT_MATCH";
        } else if(offset > 0) {
            return "NP_INCLUDES_IN_ENSP";
        } else if(offset == -1) {
            return "ENSP_INCLUDES_IN_NP";
        } else if(offset == -2) {
            return "DIFFERENT_SEQUENCES";
        }
        return "-";
    }
}
