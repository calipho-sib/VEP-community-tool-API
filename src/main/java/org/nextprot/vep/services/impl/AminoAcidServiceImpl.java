package org.nextprot.vep.services.impl;

import org.nextprot.vep.services.AminoAcidService;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;

@Service
public class AminoAcidServiceImpl implements AminoAcidService {

    private Map<String,String> aminoAcids = new HashMap<>();

    @PostConstruct
    public void init() {
        aminoAcids.put("A", "Ala");
        aminoAcids.put("R", "Arg");
        aminoAcids.put("N", "Asn");
        aminoAcids.put("D", "Asp");
        aminoAcids.put("C", "Cys");
        aminoAcids.put("Q", "Gln");
        aminoAcids.put("E", "Glu");
        aminoAcids.put("G", "Gly");
        aminoAcids.put("H", "His");
        aminoAcids.put("I", "Ile");
        aminoAcids.put("L", "Leu");
        aminoAcids.put("K", "Lys");
        aminoAcids.put("M", "Met");
        aminoAcids.put("F", "Phe");
        aminoAcids.put("P", "Pro");
        aminoAcids.put("S", "Ser");
        aminoAcids.put("T", "Thr");
        aminoAcids.put("W", "Trp");
        aminoAcids.put("V", "Val");
        aminoAcids.put("B", "Asx");
        aminoAcids.put("Z", "Glx");
        aminoAcids.put("X", "Xaa");
        aminoAcids.put("J", "Xle");
    }

    @Override
    public String getThreeLetterCode(String aminoAcidOneLetterCode) throws Exception {
        String code = aminoAcids.get(aminoAcidOneLetterCode);
        if(code == null) {
            throw new Exception("Invalid Amino Acid Code");
        } else {
            return code;
        }
    }
}
