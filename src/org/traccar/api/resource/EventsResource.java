package org.traccar.api.resource;

import org.traccar.Context;
import org.traccar.api.BaseResource;
import org.traccar.model.DeviceEvent;
import org.traccar.web.JsonConverter;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by dog on 4/27/16.
 */
@Path("events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsResource extends BaseResource {
    private final int MAX_EVENT_CODE = 101;

    @GET
    public Collection<DeviceEvent> get(@QueryParam("eventCode") int eventCode,
            @QueryParam("deviceId") long deviceId, @QueryParam("from") String from, @QueryParam("to") String to)
            throws SQLException {

        List<DeviceEvent> deviceEvents = new LinkedList<>();

        if(deviceId == 0){
            Set<Long> devices = Context.getPermissionsManager().getDevicePermissions(getUserId());
            for (Long device : devices) {
                if(eventCode == 0){
                    deviceEvents.addAll(getAllOfType(device, from, to));
                } else {
                    deviceEvents.addAll(Context.getDataManager().getEvent(device, eventCode, JsonConverter.parseDate(from), JsonConverter.parseDate(to)));
                }
            }
            return sort(deviceEvents);
        } else {
            if (eventCode == 0){
                deviceEvents.addAll(getAllOfType(deviceId, from, to));
                return sort(deviceEvents);
            }
                Context.getPermissionsManager().checkDevice(getUserId(), deviceId);
                return Context.getDataManager().getEvent(deviceId, eventCode, JsonConverter.parseDate(from), JsonConverter.parseDate(to));
            }
    }

    private Collection<DeviceEvent> getAllOfType(long deviceId, String from, String to) throws SQLException {
        Collection deviceEvents = new LinkedList();
        for (int i = 100; i <= MAX_EVENT_CODE; i++) {
            deviceEvents.addAll(Context.getDataManager().getEvent(deviceId, i, JsonConverter.parseDate(from), JsonConverter.parseDate(to)));
        }
        return deviceEvents;
    }

    private List<DeviceEvent> sort(List events){
        Collections.sort(events, new Comparator<DeviceEvent>() {
            @Override
            public int compare(DeviceEvent o1, DeviceEvent o2) {
                return o1.getTime().compareTo(o2.getTime());
            }
        });
        return events;
    }

}
