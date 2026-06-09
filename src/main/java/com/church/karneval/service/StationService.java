package com.church.karneval.service;

import com.church.karneval.model.Station;

import java.util.List;
import java.util.UUID;

public interface StationService {
    List<Station> getAllStations();

    Station getStationById(UUID id);

    Station createStation(String name, String description, String locationHint);

    Station updateStation(UUID id, String name, String description, String locationHint);

    void deleteStation(UUID id);
}
