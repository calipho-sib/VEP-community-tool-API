package org.nextprot.vep.services;

/**
 * Provide amino acid code convertion
 */
public interface AminoAcidService {

    String getThreeLetterCode(String aminoAcidOneLetterCode) throws Exception;
}
