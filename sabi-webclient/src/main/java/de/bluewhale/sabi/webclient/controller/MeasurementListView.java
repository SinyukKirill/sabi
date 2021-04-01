/*
 * Copyright (c) 2021 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi.webclient.controller;

import de.bluewhale.sabi.exception.BusinessException;
import de.bluewhale.sabi.model.AquariumTo;
import de.bluewhale.sabi.model.MeasurementTo;
import de.bluewhale.sabi.model.UnitTo;
import de.bluewhale.sabi.webclient.CDIBeans.UserSession;
import de.bluewhale.sabi.webclient.apigateway.MeasurementService;
import de.bluewhale.sabi.webclient.apigateway.TankService;
import de.bluewhale.sabi.webclient.utils.MessageUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.annotation.SessionScope;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * Controller for the Measurement View as shown in measureView.xhtml
 *
 * @author Stefan Schubert
 */
@Named
@SessionScope
@Slf4j
@Getter
public class MeasurementListView implements Serializable {

    private static final String MEASUREMENT_VIEW_PAGE = "measureView";
    private static final int MAX_RESULT_COUNT = 5;

    @Autowired
    MeasurementService measurementService;

    @Autowired
    TankService tankService;

    @Inject
    UserSession userSession;

    // View Modell
    private List<AquariumTo> tanks;
    private List<UnitTo> knownUnits;
    private List<MeasurementTo> measurementsTakenByUser = Collections.emptyList();
    private MeasurementTo measurement = new MeasurementTo();

    @PostConstruct
    public void init() {
        // user should be able to choose from his tanks
        try {
            tanks = tankService.getUsersTanks(userSession.getSabiBackendToken());
            if (tanks.size() == 1) {
                // default selection if user has only one tank
                measurement.setAquariumId(tanks.get(0).getId());
            }
        } catch (BusinessException e) {
            tanks = Collections.emptyList();
            log.error(e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage("Exception", new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "Backendtoken expired? Please relogin."));
        }

        try {
            knownUnits = measurementService.getAvailableMeasurementUnits(userSession.getSabiBackendToken());
        } catch (BusinessException e) {
            knownUnits = Collections.emptyList();
            log.error(e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage("Exception", new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "Backendtoken expired? Please relogin."));
        }

        fetchUsersLatestMeasurements();

    }

    private void fetchUsersLatestMeasurements() {
        try {
            measurementsTakenByUser = measurementService.getMeasurementsTakenByUser(userSession.getSabiBackendToken(), MAX_RESULT_COUNT);
        } catch (BusinessException e) {
            measurementsTakenByUser = Collections.emptyList();
            log.error(e.getLocalizedMessage());
            FacesContext.getCurrentInstance().addMessage("Exception", new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning!", "Backendtoken expired? Please relogin."));
        }
    }

    /**
     * Used to request the Unitsign, when all you have is a reference Id.
     *
     * @param unitId technical key of the Unit.
     * @return "N/A" if unitId is unknown
     */
    @NotNull
    public String getUnitSignForId(Integer unitId) {
        String result = "N/A";
        if (unitId != null) {
            for (UnitTo unitTo : knownUnits) {
                if (unitTo.getId().equals(unitId)) {
                    result = unitTo.getUnitSign();
                    break;
                }
            }
        }
        if (result.equals("N/A")) {
            log.warn("Could not determine the unit sign for unitID: {}", unitId);
        }
        return result;
    }

    /**
     * Used to request the TankName, when all you have is a reference Id.
     *
     * @param tankId technical key of the Tank.
     * @return "N/A" if tankId is unknown
     */
    @NotNull
    public String getTankNameForId(Long tankId) {
        String result = "N/A";
        if (tankId != null) {
            for (AquariumTo aquariumTo : tanks) {
                if (aquariumTo.getId().equals(tankId)) {
                    result = aquariumTo.getDescription();
                    break;
                }
            }
        }
        if (result.equals("N/A")) {
            log.warn("Could not determine the tankname for tankID: {}", tankId);
        }
        return result;
    }


    //
//    public String edit(AquariumTo tank) {
//        selectedTank = tank;
//        return TANK_EDITOR_PAGE;
//    }
//

    public String resetForm() {
        measurement = new MeasurementTo();
        return MEASUREMENT_VIEW_PAGE;
    }

    public String save() {
        if (allDataProvided(measurement)) {
            // Already stored
            try {
                measurementService.save(measurement, userSession.getSabiBackendToken());
                fetchUsersLatestMeasurements();
                resetForm();
            } catch (BusinessException e) {
                e.printStackTrace();
                FacesContext.getCurrentInstance().addMessage("Exception", new FacesMessage(FacesMessage.SEVERITY_WARN, "Sorry!",
                        MessageUtil.getFromMessageProperties("common.error.internal_server_problem", userSession.getLocale())));
            }
        }
        return MEASUREMENT_VIEW_PAGE;
    }

    private boolean allDataProvided(MeasurementTo measurement) {
        boolean result = true;
        if (measurement.getMeasuredOn() == null) result = false;
        if (measurement.getAquariumId() == null) result = false;
        if (measurement.getUnitId() == 0) result = false;
        return result;

    }

    public void editMeasurement(MeasurementTo existingMeasurement) {
        measurement = existingMeasurement;
    }
}
