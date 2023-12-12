package data

import (
	"slices"
)

type MeasurementCache interface {
	UpdateClimateMeasurements(newMeasurements []ClimateMeasurement) []ClimateMeasurement
	UpdateConsumptionMeasurements(newMeasurements []ConsumptionMeasurement) []ConsumptionMeasurement
	GetClimateMeasurementBySensor(sensor string) *ClimateMeasurement
	GetClimateMeasurementsBySensors(sensors []string) []ClimateMeasurement
}

func NewMeasurementCache() MeasurementCache {
	return &MeasurementCacheImpl{}
}

type MeasurementCacheImpl struct {
	ClimateMeasurements     []ClimateMeasurement     `json:"climateMeasurements"`
	ConsumptionMeasurements []ConsumptionMeasurement `json:"consumptionMeasurements"`
}

// UpdateClimateMeasurements updates the climate measurements
// returns the measurements that have newer timestamps than the previously cached ones
func (c *MeasurementCacheImpl) UpdateClimateMeasurements(newMeasurements []ClimateMeasurement) []ClimateMeasurement {
	changedMeasurements := filterChangedMeasurements(c.ClimateMeasurements, newMeasurements)
	c.ClimateMeasurements = newMeasurements
	return changedMeasurements
}

// UpdateConsumptionMeasurements updates the consumption measurements
// returns the measurements that have newer timestamps than the previously cached ones
func (c *MeasurementCacheImpl) UpdateConsumptionMeasurements(newMeasurements []ConsumptionMeasurement) []ConsumptionMeasurement {
	changedMeasurements := filterChangedMeasurements(c.ConsumptionMeasurements, newMeasurements)
	c.ConsumptionMeasurements = newMeasurements
	return changedMeasurements
}

func filterChangedMeasurements[M ComparableMeasurement](oldMeasurements, newMeasurements []M) []M {
	var changedMeasurements []M
	for _, newMeasurement := range newMeasurements {
		oldMeasurement := findMeasurementBySensor(oldMeasurements, newMeasurement.GetSensor())
		if oldMeasurement != nil {
			if (*oldMeasurement).GetTime().Compare(newMeasurement.GetTime()) < 0 {
				changedMeasurements = append(changedMeasurements, newMeasurement)
			}
		} else {
			changedMeasurements = append(changedMeasurements, newMeasurement)
		}
	}
	return changedMeasurements
}

func findMeasurementBySensor[M ComparableMeasurement](measurements []M, sensor string) *M {
	for _, measurement := range measurements {
		if measurement.GetSensor() == sensor {
			return &measurement
		}
	}
	return nil
}

func (c *MeasurementCacheImpl) GetClimateMeasurementBySensor(sensor string) *ClimateMeasurement {
	for _, each := range c.ClimateMeasurements {
		if each.Sensor == sensor {
			return &each
		}
	}
	return nil
}

func (c *MeasurementCacheImpl) GetClimateMeasurementsBySensors(sensors []string) []ClimateMeasurement {
	var filteredMeasurements []ClimateMeasurement
	for _, measurement := range c.ClimateMeasurements {
		if slices.Contains(sensors, measurement.Sensor) {
			filteredMeasurements = append(filteredMeasurements, measurement)
		}
	}
	return filteredMeasurements
}
