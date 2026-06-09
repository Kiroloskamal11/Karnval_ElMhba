package com.church.karneval.service.impl;

import com.church.karneval.model.Station;
import com.church.karneval.repository.StationRepository;
import com.church.karneval.service.StationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class StationServiceImpl implements StationService {

    private final StationRepository stationRepository;

    public StationServiceImpl(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Station getStationById(UUID id) {
        return stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المحطة بالمعرف: " + id));
    }

    @Override
    @Transactional
    public Station createStation(String name, String description, String locationHint) {
        Station station = new Station();
        station.setName(name);
        station.setDescription(description);
        station.setLocationHint(locationHint);
        return stationRepository.save(station);
    }

    @Override
    @Transactional
    public Station updateStation(UUID id, String name, String description, String locationHint) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المحطة بالمعرف: " + id));
        station.setName(name);
        station.setDescription(description);
        station.setLocationHint(locationHint);
        return stationRepository.save(station);
    }

    @Override
    @Transactional
    public void deleteStation(UUID id) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("لم يتم العثور على المحطة بالمعرف: " + id));
        stationRepository.delete(station);
    }
}
