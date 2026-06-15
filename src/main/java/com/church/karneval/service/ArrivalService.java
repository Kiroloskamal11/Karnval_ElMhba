package com.church.karneval.service;

import com.church.karneval.model.Arrival;

import java.util.List;
import java.util.UUID;

public interface ArrivalService {
    Arrival confirmArrival(UUID teamId, UUID stationId, UUID confirmedBy);
    Arrival updateArrival(UUID arrivalId, UUID confirmedBy);
    List<Arrival> getTeamArrivals(UUID teamId);
    List<Arrival> getStationArrivals(UUID stationId);
    List<Arrival> getAllArrivals();
}
