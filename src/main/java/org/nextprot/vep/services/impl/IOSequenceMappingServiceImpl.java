package org.nextprot.vep.services.impl;

import jaligner.Alignment;
import org.nextprot.vep.domain.SequenceMappingProfile;
import org.nextprot.vep.services.SequenceMappingService;
import org.nextprot.vep.utils.PamAligner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.annotation.PostConstruct;
import java.io.*;
import java.net.URISyntaxException;
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

    @Autowired
    ResourceLoader resourceLoader;

    // Map to keep the sequences for isoforms
    private Map<String, String[]> sequenceMap = new HashMap<>();

    // Map to keep ensps for isoforms
    private Map<String, String> enspMap = new HashMap<>();

    // Map to keep enst for isoforms
    private Map<String, String> enstMap = new HashMap<>();

    // Map to keep ensg for isoforms
    private Map<String, String> ensgMap = new HashMap<>();

    // Map to keep isoforms for entries
    private Map<String, List<String>> entryIsoformMap = new HashMap<>();


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
                String entry = sequenceData.split(",")[0];
                String isoform = sequenceData.split(",")[5];
                String nextprotSequence = sequenceData.split(",")[6];
                String ensg = sequenceData.split(",") [1];
                String enst = sequenceData.split(",") [2];
                String ensp = sequenceData.split(",") [3];
                String enspSequence = sequenceData.split(",")[4];

                String[] sequences = new String[2];
                sequences[0] = enspSequence;
                sequences[1] = nextprotSequence;

                // Updates the sequence map
                sequenceMap.put(isoform, sequences);

                // Updates the ensg map
                if(ensg != null) {
                    ensgMap.put(isoform, ensg);
                }

                // Updates the enst map
                if(enst != null) {
                    enstMap.put(isoform, enst);
                }

                // Updates the ensp map
                enspMap.put(isoform, ensp);

                // Updates the entry isoform map
                if(entryIsoformMap.get(entry) != null) {
                    entryIsoformMap.get(entry).add(isoform);
                } else {
                    List<String> isoformList = new ArrayList<>();
                    isoformList.add(isoform);
                    entryIsoformMap.put(entry, isoformList);
                }

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
        sequenceMappingProfile.setIsoform(isoform);
        if(nextprotSequence.equals(enspSequence)) {
            logger.info("Exact Match: NP" + nextprotSequence.length() + "ENSP" + enspSequence);
            sequenceMappingProfile.setOffset(0);
        } else if(enspSequence.contains(nextprotSequence)) {
            // ensp sequence is a substring of nextprot sequence
            logger.info("NP substring: NP " + nextprotSequence + " ENSP " + enspSequence);
            sequenceMappingProfile.setOffset(enspSequence.indexOf(nextprotSequence));
        } else if(nextprotSequence.contains(enspSequence)) {
            // Nextprot sequence is a substring of ensp sequence
            logger.info("ENSP substring: NP " + nextprotSequence + " ENSP " + enspSequence);
            sequenceMappingProfile.setOffset(-1);
        } else {
            logger.info("Different sequences: NP " + nextprotSequence + " ENSP " + enspSequence);
            // Have to do the alignment
            PamAligner aligner = new PamAligner("nextprot", nextprotSequence, "ensp", enspSequence);
            Alignment alignment = aligner.getAlignment();
            String alignmentResult = aligner.getS1Identities() + "," + aligner.getS1InnerGapCount() + "," +
                    aligner.getS2InnerGapCount() + "," + aligner.getInnerGapCount() + "," +
                    alignment.getLength() + "," + alignment.getIdentity() + "," +
                    alignment.getSimilarity() + "," + alignment.getStart1() + "," +
                    alignment.getStart2() + "," + alignment.getGaps1() + "," +
                    alignment.getGaps2() + "," + alignment.getScore() + "," +
                    alignment.getScoreWithNoTerminalGaps()  + "," +
                    new String(alignment.getSequence1()) + "," +
                    new String(alignment.getMarkupLine()) + "," +
                    new String(alignment.getSequence2());
            logger.info("NP seq length " + nextprotSequence.length() + " ENSP seq length " + enspSequence);
            sequenceMappingProfile.setOffset(-2);
            sequenceMappingProfile.setAlignmentResults(alignmentResult);
        }

        return sequenceMappingProfile;
    }

    @Override
    @Cacheable(key= "#entry", sync = true)
    public List<SequenceMappingProfile> getMappingProfiles(String entry) {
        List<String> isoforms = entryIsoformMap.get(entry);
        if(isoforms == null) {
            logger.info("No isoforms found for the given entry " + entry);
            return new ArrayList<>();
        } else {
            return entryIsoformMap.get(entry)
                    .stream()
                    .map(isoform -> getMappingProfile(isoform))
                    .collect(Collectors.toList());
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
        for(String entry: entryIsoformMap.keySet()){
            logger.info("Processing entry " + entry);
            List<String> result = entryIsoformMap.get(entry)
                .stream()
                .map(isoform -> {
                    SequenceMappingProfile mappingProfile = getMappingProfile(isoform);
                    String ensg = ensgMap.get(isoform);
                    String enst = enspMap.get(isoform);
                    return entry + ","
                            + ensg + ","
                            + enst + ","
                            + isoform + "," + sequenceMap.get(isoform)[0]
                            + "," + mappingProfile.getEnsp() + "," + sequenceMap.get(isoform)[1] + ","
                            + sequenceComaprison(mappingProfile.getOffset()) + "," +
                            mappingProfile.getAlignmentResults();
                })
                .collect(Collectors.toList());
            results.addAll(result);
        }
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
        return "SOMETHING_WRONG";
    }
}
