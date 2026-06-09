package com.church.karneval.service.impl;

import com.church.karneval.enums.NotificationType;
import com.church.karneval.enums.UserRole;
import com.church.karneval.model.Arrival;
import com.church.karneval.model.Station;
import com.church.karneval.model.Team;
import com.church.karneval.model.User;
import com.church.karneval.repository.ArrivalRepository;
import com.church.karneval.repository.StationRepository;
import com.church.karneval.repository.TeamRepository;
import com.church.karneval.repository.UserRepository;
import com.church.karneval.service.ArrivalService;
import com.church.karneval.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class ArrivalServiceImpl implements ArrivalService {

    private final ArrivalRepository arrivalRepository;
    private final TeamRepository teamRepository;
    private final StationRepository stationRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ArrivalServiceImpl(
            ArrivalRepository arrivalRepository,
            TeamRepository teamRepository,
            StationRepository stationRepository,
            UserRepository userRepository,
            NotificationService notificationService) {
        this.arrivalRepository = arrivalRepository;
        this.teamRepository = teamRepository;
        this.stationRepository = stationRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    @Override
    @Transactional
    public Arrival confirmArrival(UUID teamId, UUID stationId, UUID confirmedBy) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على الفريق بالمعرف: " + teamId));

        Station station = stationRepository.findById(stationId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على المحطة بالمعرف: " + stationId));

        User confirmedByUser = userRepository.findById(confirmedBy)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على المستخدم بالمعرف: " + confirmedBy));

        // Enforce that a Team Leader can only confirm arrivals for their own team
        if (confirmedByUser.getRole() == UserRole.TEAM_LEADER) {
            if (confirmedByUser.getTeam() == null || !confirmedByUser.getTeam().getId().equals(teamId)) {
                throw new RuntimeException("غير مصرح لك بتسجيل الوصول لفريق آخر.");
            }
        }

        Optional<Arrival> existing = arrivalRepository.findByTeamIdAndStationId(teamId, stationId);

        Arrival arrival;
        if (existing.isPresent()) {
            arrival = existing.get();
            arrival.setArrivedAt(OffsetDateTime.now());
            arrival.setConfirmedBy(confirmedByUser);
        } else {
            arrival = new Arrival(team, station, confirmedByUser);
        }

        Arrival saved = arrivalRepository.save(arrival);

        // إشعار للـ Admins
        notificationService.createNotificationForAllAdmins(
                NotificationType.ARRIVAL_CONFIRMED,
                "وصول فريق ✅",
                "فريق " + team.getName() +
                        " وصل إلى " + station.getName());

        return saved;
    }

    @Override
    @Transactional
    public Arrival updateArrival(UUID arrivalId, UUID confirmedBy) {
        Arrival arrival = arrivalRepository.findById(arrivalId)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على وصول بالمعرف: " + arrivalId));

        User confirmedByUser = userRepository.findById(confirmedBy)
                .orElseThrow(() -> new RuntimeException(
                        "لم يتم العثور على المستخدم بالمعرف: " + confirmedBy));

        // Enforce that a Team Leader can only update arrivals for their own team
        if (confirmedByUser.getRole() == UserRole.TEAM_LEADER) {
            if (confirmedByUser.getTeam() == null || !confirmedByUser.getTeam().getId().equals(arrival.getTeam().getId())) {
                throw new RuntimeException("غير مصرح لك بتعديل وصول فريق آخر.");
            }
        }

        arrival.setArrivedAt(OffsetDateTime.now());
        arrival.setConfirmedBy(confirmedByUser);

        return arrivalRepository.save(arrival);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Arrival> getTeamArrivals(UUID teamId) {
        return arrivalRepository.findByTeamId(teamId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Arrival> getStationArrivals(UUID stationId) {
        return arrivalRepository.findByStationId(stationId);
    }
}