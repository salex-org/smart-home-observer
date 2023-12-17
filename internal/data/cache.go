package data

import (
	"slices"
)

type MeasurementCache interface {
	UpdateClimateMeasurements(newMeasurements []ClimateMeasurement) []ClimateMeasurement
	UpdateConsumptionMeasurements(newMeasurements []ConsumptionMeasurement) []ConsumptionMeasurement
	UpdateSwitchStates(newSwitchStates []SwitchState) []SwitchState
	GetClimateMeasurementBySensor(sensor string) *ClimateMeasurement
	GetClimateMeasurementsBySensors(sensors []string) []ClimateMeasurement
}

func NewMeasurementCache() MeasurementCache {
	return &MeasurementCacheImpl{
		ClimateMeasurements:     make(map[string]ClimateMeasurement),
		ConsumptionMeasurements: make(map[string]ConsumptionMeasurement),
	}
}

type MeasurementCacheImpl struct {
	ClimateMeasurements     map[string]ClimateMeasurement     `json:"climateMeasurements"`
	ConsumptionMeasurements map[string]ConsumptionMeasurement `json:"consumptionMeasurements"`
	SwitchStates            map[string]SwitchState            `json:"swtichStates"`
}

// UpdateClimateMeasurements updates the climate measurements
// returns the measurements that have newer timestamps than the previously cached ones
func (c *MeasurementCacheImpl) UpdateClimateMeasurements(newMeasurements []ClimateMeasurement) []ClimateMeasurement {
	changedMeasurements := filterChangedMeasurements(c.ClimateMeasurements, newMeasurements)
	for _, eachMeasurement := range changedMeasurements {
		c.ClimateMeasurements[eachMeasurement.DeviceID] = eachMeasurement
	}
	return changedMeasurements
}

// UpdateConsumptionMeasurements updates the consumption measurements
// returns the measurements that have newer timestamps than the previously cached ones
func (c *MeasurementCacheImpl) UpdateConsumptionMeasurements(newMeasurements []ConsumptionMeasurement) []ConsumptionMeasurement {
	changedMeasurements := filterChangedMeasurements(c.ConsumptionMeasurements, newMeasurements)
	for _, eachMeasurement := range changedMeasurements {
		c.ConsumptionMeasurements[eachMeasurement.DeviceID] = eachMeasurement
	}
	return changedMeasurements
}

// UpdateSwitchStates updates the switch states
// returns the switch states that have newer timestamps than the previously cached ones
func (c *MeasurementCacheImpl) UpdateSwitchStates(newSwitchStates []SwitchState) []SwitchState {
	changedSwitchStates := filterChangedMeasurements(c.SwitchStates, newSwitchStates)
	for _, eachState := range changedSwitchStates {
		c.SwitchStates[eachState.DeviceID] = eachState
	}
	return changedSwitchStates
}

func filterChangedMeasurements[M ComparableMeasurement](oldMeasurements map[string]M, newMeasurements []M) []M {
	var changedMeasurements []M
	for _, newMeasurement := range newMeasurements {
		oldMeasurement, found := oldMeasurements[newMeasurement.GetDeviceID()]
		if found {
			if oldMeasurement.GetTime().Compare(newMeasurement.GetTime()) < 0 {
				changedMeasurements = append(changedMeasurements, newMeasurement)
			}
		} else {
			changedMeasurements = append(changedMeasurements, newMeasurement)
		}
	}
	return changedMeasurements
}

func (c *MeasurementCacheImpl) GetClimateMeasurementBySensor(sensor string) *ClimateMeasurement {
	measurement, found := c.ClimateMeasurements[sensor]
	if found {
		return &measurement
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
