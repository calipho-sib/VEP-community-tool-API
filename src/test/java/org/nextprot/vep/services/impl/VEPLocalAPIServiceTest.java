package org.nextprot.vep.services.impl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.nextprot.vep.services.VEPAPIService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class VEPLocalAPIServiceTest {

    @Qualifier("LocalService")
    @Autowired
    VEPAPIService VEPLocalService;

    @Test
    public void dockerTest() {
        VEPLocalService.getVEPResults("NX", null);
    }
}
