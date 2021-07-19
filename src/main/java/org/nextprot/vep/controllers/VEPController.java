package org.nextprot.vep.controllers;

import org.nextprot.vep.ProteinVariant;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class VEPController {

    @Autowired
    private VEPAPIService VEPAPIService;

    @GetMapping
    public List<ProteinVariant> greeting(@RequestParam(name="name", required=false, defaultValue="World") String name) {
        ProteinVariant v1 = new ProteinVariant(1, "A", "L");
        ProteinVariant v2 = new ProteinVariant(2, "L", "B");
        List<ProteinVariant> list = new ArrayList<>();
        list.add(v1);
        list.add(v2);
        return list;
    }
}
