package com.tcp.cleanmanagement.service;

import com.tcp.cleanmanagement.dto.ZonePublicResponse;
import com.tcp.cleanmanagement.dto.ZoneStatusResponse;
import com.tcp.cleanmanagement.entity.SensorDataRaw;
import com.tcp.cleanmanagement.entity.Zone;
import com.tcp.cleanmanagement.enums.ZoneType;
import com.tcp.cleanmanagement.repository.SensorDataRawRepository;
import com.tcp.cleanmanagement.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PublicZoneService {
    private final ZoneRepository zoneRepository;
    private final SensorDataRawRepository sensorDataRawRepository;

    @Transactional(readOnly = true)
    public List<ZonePublicResponse> getZones(ZoneType type, Double lat, Double lng, Double radius) {
        List<Zone> zones;
        
        if (lat != null && lng != null && radius != null) {
            if (type != null) {
                zones = zoneRepository.findZonesByTypeAndRadius(type.name(), lat, lng, radius);
            } else {
                zones = zoneRepository.findZonesWithinRadius(lat, lng, radius);
            }
        } else {
            if (type != null) {
                zones = zoneRepository.findByZoneType(type);
            } else {
                zones = zoneRepository.findAll();
            }
        }

        return zones.stream()
            .map(this::mapToResponseWithSensorData)
            .collect(Collectors.toList());
    }

    private ZonePublicResponse mapToResponseWithSensorData(Zone zone) {
        ZonePublicResponse.ZonePublicResponseBuilder builder = ZonePublicResponse.builder()
                .zoneId(zone.getId())
                .name(zone.getName())
                .latitude(zone.getLatitude())
                .longitude(zone.getLongitude())
                .isOurSolution(zone.getIsOurSolution());

        Optional<SensorDataRaw> latestData = sensorDataRawRepository.findFirstBySensorZoneIdOrderByMeasuredAtDesc(zone.getId());
        
        if (latestData.isPresent()) {
            SensorDataRaw data = latestData.get();
            // Assuming value1 is gas/odor level for simplicity in public UI
            builder.gasLevel(data.getValue1());
            builder.temp(22.0f); // Default temp
            builder.humidity(50.0f); // Default humidity
            
            if (zone.getZoneType() == ZoneType.TOILET) {
                boolean isClean = (data.getValue1() == null || data.getValue1() < zone.getPleasantThreshold());
                builder.status(isClean ? "Clean" : "Needs Cleaning");
                builder.color(isClean ? "#10b981" : "#ef4444");
            } else {
                boolean isCrowded = (data.getValue1() != null && data.getValue1() > 200.0f);
                builder.status(isCrowded ? "Crowded" : "Spacious");
                builder.color(isCrowded ? "#f59e0b" : "#3b82f6");
            }
        } else {
            builder.status("No Data");
            builder.color("#9ca3af"); 
        }
        
        return builder.build();
    }

    @Transactional(readOnly = true)
    public ZoneStatusResponse getZoneStatus(Long zoneId) {
        Zone zone = zoneRepository.findById(zoneId)
                .orElseThrow(() -> new IllegalArgumentException("Zone not found"));

        return ZoneStatusResponse.builder()
                .zoneId(zone.getId())
                .name(zone.getName())
                .status("NORMAL") 
                .statusMessage("Available")
                .build();
    }
}
