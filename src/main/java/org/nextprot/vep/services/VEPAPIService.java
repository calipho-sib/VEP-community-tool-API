package org.nextprot.vep.services;

import org.nextprot.vep.domain.ProteinVariant;

import java.util.List;

/**
 *  Handles requests towards the EnsEMBL VEP REST API
 */
public interface VEPAPIService {

    List<ProteinVariant> getVEPResults();
}
