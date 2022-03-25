package org.salex.hmip.observer.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;
import java.util.List;

public interface OperatingMeasurementRepository extends JpaRepository<OperatingMeasurement, Long> {
}
