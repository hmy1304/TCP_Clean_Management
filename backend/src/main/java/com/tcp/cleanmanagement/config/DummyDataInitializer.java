package com.tcp.cleanmanagement.config;

import com.tcp.cleanmanagement.entity.Zone;
import com.tcp.cleanmanagement.enums.ZoneType;
import com.tcp.cleanmanagement.repository.ZoneRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class DummyDataInitializer implements CommandLineRunner {

    private final ZoneRepository zoneRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (zoneRepository.count() == 0) {
            Zone zone1 = Zone.builder()
                    .name("Gangnam Station Exit 1 Public Toilet")
                    .address("101 Teheran-ro, Gangnam-gu, Seoul")
                    .latitude(37.4979)
                    .longitude(127.0276)
                    .zoneType(ZoneType.TOILET)
                    .isOurSolution(true)
                    .pleasantThreshold(50.0f)
                    .build();

            Zone zone2 = Zone.builder()
                    .name("Yeoksam Station Exit 3 Toilet")
                    .address("152 Teheran-ro, Gangnam-gu, Seoul")
                    .latitude(37.5006)
                    .longitude(127.0364)
                    .zoneType(ZoneType.TOILET)
                    .isOurSolution(false)
                    .pleasantThreshold(50.0f)
                    .build();

            zoneRepository.save(zone1);
            zoneRepository.save(zone2);
            
            System.out.println("[DummyDataInitializer] Dummy data generated.");
        }
    }
}
