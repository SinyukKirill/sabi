/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi.services;

import de.bluewhale.sabi.configs.AppConfig;
import de.bluewhale.sabi.model.PlagueStatusTo;
import de.bluewhale.sabi.persistence.model.LocalizedPlagueStatusEntity;
import de.bluewhale.sabi.persistence.model.PlagueStatusEntity;
import de.bluewhale.sabi.persistence.repositories.PlagueStatusRepository;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.BDDMockito.given;


/**
 * Business-Layer tests for PlagueCenterService.
 */
@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = AppConfig.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class PlagueCenterServiceTest {
    // ------------------------------ FIELDS ------------------------------

    @Autowired
    private PlagueCenterService plagueCenterService;

    @MockBean
    private PlagueStatusRepository plagueStatusRepository;

    @Autowired
    private UserService userService;

// -------------------------- OTHER METHODS --------------------------


    @Test
    @Transactional
    public void testListTranslatedPlagueStatus() throws Exception {
        // Given
        PlagueStatusEntity plagueStatusEntity = new PlagueStatusEntity();
        plagueStatusEntity.setId(1l);

        ArrayList<LocalizedPlagueStatusEntity> localizedPlagueStatusEntities = new ArrayList<>();

        LocalizedPlagueStatusEntity localizedPlagueStatus1 = new LocalizedPlagueStatusEntity();
        localizedPlagueStatus1.setPlague_status_id(1);
        localizedPlagueStatus1.setId(99l);
        localizedPlagueStatus1.setDescription("Spreading");
        localizedPlagueStatus1.setLanguage("en");

        LocalizedPlagueStatusEntity localizedPlagueStatus2 = new LocalizedPlagueStatusEntity();
        localizedPlagueStatus2.setPlague_status_id(1);
        localizedPlagueStatus2.setId(88l);
        localizedPlagueStatus2.setDescription("Ausweitend");
        localizedPlagueStatus2.setLanguage("de");

        localizedPlagueStatusEntities.add(localizedPlagueStatus1);
        localizedPlagueStatusEntities.add(localizedPlagueStatus2);

        plagueStatusEntity.setLocalizedPlagueStatusEntities(localizedPlagueStatusEntities);

        ArrayList<PlagueStatusEntity> plagueStatusEntities = new ArrayList<PlagueStatusEntity>();
        plagueStatusEntities.add(plagueStatusEntity);

        given(this.plagueStatusRepository.findAll()).willReturn(plagueStatusEntities);

        // When
        List<PlagueStatusTo> plagueStatusTos = plagueCenterService.listAllPlagueStatus("en");

        // Then
        assertNotNull(plagueStatusTos);
        assertEquals("Persisted Testdata?", 1, plagueStatusTos.size());
        assertTrue("Language filter brocken?", plagueStatusTos.get(0).getDescription().equals("Spreading"));
    }

}
