/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi.rest.controller;

import de.bluewhale.sabi.exception.Message;
import de.bluewhale.sabi.model.AquariumTo;
import de.bluewhale.sabi.model.IoTMeasurementTo;
import de.bluewhale.sabi.model.MeasurementTo;
import de.bluewhale.sabi.model.ResultTo;
import de.bluewhale.sabi.services.MeasurementService;
import de.bluewhale.sabi.services.TankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;

@RestController
@Validated
@RequestMapping(value = "api/aquarium_iot")
@Slf4j
/**
 * This controller integrates automatic measurement devices like
 * https://github.com/StefanSchubert/aquarium_IoT#readme
 * which will be able to report certain measurements for a
 * users tank, by using an api key, which has been generated by the user
 * in his tank view.
 */
public class AquariumIoTController {

    static final int MIN_GAP_IN_HOURS_BETWEEN_SUBSEQUENT_SUBMISSIONS = 1;

// ------------------------------ FIELDS ------------------------------

    @Autowired
    MeasurementService measurementService;

    @Autowired
    TankService tankService;

// -------------------------- OTHER METHODS --------------------------

    @Operation(method = "Add a new temperature measurement. Needs to be provided via json body.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created. Measurement has been stored to your tank."
            ),
            @ApiResponse(responseCode = "429", description = "Request ignored, please take care to have at least "+MIN_GAP_IN_HOURS_BETWEEN_SUBSEQUENT_SUBMISSIONS+"h between subsequent requests."),
            @ApiResponse(responseCode = "401", description = "Unauthorized-request did not contained a valid API-key."),
            @ApiResponse(responseCode = "400", description = "In case of passing an invalid IoTMeasurementTo")
    })
    @RequestMapping(value = {"temp_measurement"}, method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @ResponseBody
    public ResponseEntity<IoTMeasurementTo> addMeasurement(@RequestBody @Valid IoTMeasurementTo ioTmeasurementTo) {

        ResponseEntity<IoTMeasurementTo> responseEntity;
        AquariumTo aquariumTo;

        try {
            aquariumTo = tankService.getTankForTemperatureApiKey(ioTmeasurementTo.getApiKey());
            if (aquariumTo == null || aquariumTo.getActive()==false)  {
                return new ResponseEntity<>(ioTmeasurementTo, HttpStatus.UNAUTHORIZED);
            }
        } catch (Exception e) {
            log.warn("Submission with invalid apiKey for tempMeasurement detected. Exception was: {}",e.getMessage());
            return new ResponseEntity<>(ioTmeasurementTo, HttpStatus.UNAUTHORIZED);
        }

        // check time constraint: when was the last measurement taken? Do we have the minimum GAP to be allowed to store another record.
        LocalDateTime timeOfLastMeasureRecord = measurementService.getLastTimeOfMeasurementTakenFilteredBy(aquariumTo.getId(), 2);// Unit 2 is Celcius
        if (timeOfLastMeasureRecord != null && timeOfLastMeasureRecord.plusHours(MIN_GAP_IN_HOURS_BETWEEN_SUBSEQUENT_SUBMISSIONS).isAfter(LocalDateTime.now())) {
            log.info("Received an iot device temp submission too frequently. Hour gap must be {}",MIN_GAP_IN_HOURS_BETWEEN_SUBSEQUENT_SUBMISSIONS);
            return new ResponseEntity<>(ioTmeasurementTo, HttpStatus.TOO_MANY_REQUESTS);
        }

        MeasurementTo measurementTo = new MeasurementTo();
        measurementTo.setUnitId(2); // Temperature in °C
        measurementTo.setMeasuredValue(ioTmeasurementTo.getMeasuredValueInCelsius());
        measurementTo.setAquariumId(aquariumTo.getId());
        measurementTo.setMeasuredOn(LocalDateTime.now());

        // If we come so far, the API-Key has been validated, we are cleared to add the measurement
        ResultTo<MeasurementTo> measurementResultTo = measurementService.addIotAuthorizedMeasurement(measurementTo);

        final Message resultMessage = measurementResultTo.getMessage();
        if (Message.CATEGORY.INFO.equals(resultMessage.getType())) {
            responseEntity = new ResponseEntity<>(ioTmeasurementTo, HttpStatus.CREATED);
        } else {
            String msg = "Measurement cannot be added twice. A Measurement with Id " + measurementTo.getId() + " already exist.";
            log.warn(msg);
            responseEntity = new ResponseEntity<>(ioTmeasurementTo, HttpStatus.CONFLICT);
        }
        return responseEntity;
    }

}