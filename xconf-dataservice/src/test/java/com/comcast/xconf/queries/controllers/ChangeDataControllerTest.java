package com.comcast.xconf.queries.controllers;

import com.comcast.apps.dataaccess.dao.SimpleDao;
import com.comcast.apps.dataaccess.util.JsonUtil;
import com.comcast.hydra.astyanax.data.IPersistable;
import com.comcast.xconf.change.ApprovedChange;
import com.comcast.xconf.change.Change;
import com.comcast.xconf.change.EntityType;
import com.comcast.xconf.logupload.telemetry.PermanentTelemetryProfile;
import com.comcast.xconf.service.change.ChangeCrudService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import java.util.Arrays;
import java.util.List;

import static com.comcast.xconf.queries.controllers.ChangeDataController.CHANGE_URL;
import static com.comcast.xconf.queries.controllers.TelemetryProfileDataController.TELEMETRY_PROFILE_URL;
import static org.junit.Assert.assertNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangeDataControllerTest extends BaseQueriesControllerTest {

    @Autowired
    protected SimpleDao<String, Change> changeDao;

    @Autowired
    protected SimpleDao<String, ApprovedChange> approvedDao;

    @Autowired
    private ChangeCrudService<PermanentTelemetryProfile> changesCrudService;

    @Before
    @After
    public void cleanChangeData() {
        deleteAllEntities();

        List<SimpleDao<String, ? extends Change>> daoList = Arrays.asList(changeDao, approvedDao);
        for (SimpleDao<String, ? extends IPersistable> dao : daoList) {
            for (IPersistable iPersistable : dao.getAll()) {
                dao.deleteOne(iPersistable.getId());
            }
        }
    }

    @Test
    public void getAllProfileChanges() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        mockMvc.perform(post(TELEMETRY_PROFILE_URL + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated());

        mockMvc.perform(get(CHANGE_URL + "/all")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("[0].entityId").value(profile.getId()))
                .andExpect(jsonPath("[0].entityType").value(EntityType.TELEMETRY_PROFILE.toString()));
    }

    @Test
    public void cancelChange() throws Exception {
        PermanentTelemetryProfile profile = createPermanentTelemetryProfile();

        mockMvc.perform(post(TELEMETRY_PROFILE_URL + "/change")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isCreated());

        String changeId = changesCrudService.getChangesByEntityId(profile.getId()).stream().findFirst().get().getId();

        mockMvc.perform(get(CHANGE_URL + "/cancel/" + changeId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JsonUtil.toJson(profile)))
                .andExpect(status().isOk());

        assertNull(changeDao.getOne(changeId));
        assertNull(approvedDao.getOne(changeId));
    }
}
