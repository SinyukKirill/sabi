/*
 * Copyright (c) 2022 by Stefan Schubert under the MIT License (MIT).
 * See project LICENSE file for the detailed terms and conditions.
 */

package de.bluewhale.sabi.webclient.apigateway;

import de.bluewhale.sabi.exception.BusinessException;
import de.bluewhale.sabi.model.MeasurementReminderTo;
import de.bluewhale.sabi.model.MeasurementTo;
import de.bluewhale.sabi.model.ParameterTo;
import de.bluewhale.sabi.model.UnitTo;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import org.springframework.cache.annotation.Cacheable;

import java.io.Serializable;
import java.util.List;

/**
 * Interface between Measurement-Data in WebClient and Sabi-Server.
 * Implementation should be stateless, to keep the required resources on the PIs as low as possible.
 * That why the JWTAuthToken, is required to authenticate against backend services.
 *
 * @author Stefan Schubert
 */
public interface MeasurementService extends Serializable {

    /**
     * List Users overall Measurements. Concrete user will be derived by the calling context
     *
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @param maxResultCount      If a user has 100 measurements, It won't make sense to retrieve them all,
     *                            in case we want to display only some latest ones in the view. So we can
     *                            use this param to limit the results, which will be the youngest entries.
     *                            <b>A maxResultCount of 0 means retrieves them all.</b>
     * @return List of measurements that belong to current user. List may be empty but never NULL.
     * @throws BusinessException in case of backend auth failures.
     */
    @NotNull List<MeasurementTo> getMeasurementsTakenByUser(@NotNull String pJWTBackendAuthtoken, @NotNull Integer maxResultCount) throws BusinessException;

    /**
     * List Users Measurement Reminders he has set via his user profile settings.
     * Concrete user will be derived by the calling context (JWT Token Auth)
     *
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @return List of MeasurementReminderTo that belong to current user. List may be empty but never NULL.
     * @throws BusinessException in case of backend auth failures.
     */
    @NotNull List<MeasurementReminderTo> getMeasurementRemindersForUser(@NotNull String pJWTBackendAuthtoken) throws BusinessException;


    /**
     * To avoid unnecessary Backend calls, the implementation is suggested to
     * cache the results.
     * <p>
     * TODO: JMX Beans such that the cache can be reloaded in case
     * the backend introduces more units.
     *
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @return List of units known by the backend.
     * @throws BusinessException in case of backend auth failures.
     */
    @Cacheable
    @NotNull List<UnitTo> getAvailableMeasurementUnits(@NotNull String pJWTBackendAuthtoken) throws BusinessException;

    /**
     * List Users Measurements for a specific tank. Concrete user will be derived by the calling context
     *
     * @param JWTAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @param tankId       Id of users tank to which the measures belong.
     * @return List of measurements that belong to current user. List may be empty but never NULL.
     * @throws BusinessException in case of backend auth failures.
     */
    @NotNull List<MeasurementTo> getMeasurementsForUsersTank(@NotNull String JWTAuthtoken, @NotNull Long tankId) throws BusinessException;


    /**
     * List Users Measurements for a specific tank and measurement unit. Concrete user will be derived by the calling context
     *
     * @param JWTAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @param tankId       Id of users tank to which the measures belong.
     * @param unitId       Id which is used to filter the results for a specifc measurement unit.
     * @return List of measurements that belong to current user. List may be empty but never NULL.
     * @throws BusinessException in case of backend auth failures.
     */
    @NotNull List<MeasurementTo> getMeasurementsForUsersTankFilteredByUnit(@NotNull String JWTAuthtoken, @NotNull Long tankId, @NotNull Integer unitId) throws BusinessException;

    /**
     * Request Measurement deletion in Backend, in case he or she did a typo.
     *
     * @param measurementId       Identifier of the Measurement to delete
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @throws BusinessException
     */
    void deleteMeasurementById(@NotNull Long measurementId, @NotNull String pJWTBackendAuthtoken) throws BusinessException;

    /**
     * Update an existing or create a measurement entry for the user.
     *
     * @param measurement         Measurement Entry to patch or to create
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @throws BusinessException
     */
    void save(MeasurementTo measurement, @NotNull String pJWTBackendAuthtoken) throws BusinessException;

    /**
     * Fetches detailed Parameterinfos for requested measurement unti
     *
     * @param selectedUnitId      ID of measurement unit
     * @param pJWTBackendAuthtoken Bearer Auth string, which identifies the user against the backend.
     * @return detailed info if available or null if it does not exists
     */
    @Null ParameterTo getParameterFor(@NotNull Integer selectedUnitId, @NotNull String pJWTBackendAuthtoken) throws BusinessException;

    /**
     * Push a new Measurement reminder to the backend.
     * In case the measurementUnit is already on the list, do nothing (Idempotent)
     * @param measurementReminderTo
     * @param pJWTBackendAuthtoken
     */
    void addMeasurementReminder(MeasurementReminderTo measurementReminderTo, String pJWTBackendAuthtoken) throws BusinessException;
}
