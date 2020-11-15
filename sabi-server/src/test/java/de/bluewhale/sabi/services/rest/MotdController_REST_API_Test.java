/*
 * Copyright (c) 2020 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi.services.rest;

import de.bluewhale.sabi.model.MotdTo;
import de.bluewhale.sabi.services.AppService;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.reset;


/**
 * Checks Motd Service
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class MotdController_REST_API_Test {

    @MockBean
    AppService appService;

    @Autowired
    private TestRestTemplate restTemplate;


    @After
    public void cleanUpMocks() {
        reset(appService);
    }

    /**
     * Tests MOTD Rest API in case we have no content.
     *
     * @throws Exception
     */
    @Test // REST-API
    public void testModtRetrievalWithNoNews() throws Exception {

        // given a motd
        MotdTo motdTo = new MotdTo("junit modt");

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/app/motd/xx", HttpMethod.GET, null, String.class);

        // then we should get a 204 as result.
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.NO_CONTENT));
    }

    /**
     * Tests MOTD Rest API in case we news.
     *
     * @throws Exception
     */
    @Test // REST-API
    public void testModtRetrieval() throws Exception {

        // given a motd
        String motd = "Junit Modt";
        given(this.appService.fetchMotdFor("en")).willReturn(motd);

        // when
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> responseEntity = restTemplate.exchange("/api/app/motd/en", HttpMethod.GET, null, String.class);

        // then we should get a 200 as result.
        assertThat(responseEntity.getStatusCode(), equalTo(HttpStatus.OK));
    }
}
