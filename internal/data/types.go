package data

import "time"

type Measurement struct {
	Time   time.Time `json:"time"`
	Sensor string    `json:"sensor"`
}

type ComparableMeasurement interface {
	GetTime() time.Time
	GetSensor() string
}

func (m Measurement) GetTime() time.Time {
	return m.Time
}

func (m Measurement) GetSensor() string {
	return m.Sensor
}

type ClimateMeasurement struct {
	Measurement `json:",inline"`
	Humidity    int     `json:"humidity"`
	Temperature float64 `json:"temperature"`
	VaporAmount float64 `json:"vaporAmount"`
}

type ConsumptionMeasurement struct {
	Measurement        `json:",inline"`
	CurrentConsumption float64 `json:"currentConsumption"`
}

type Health struct {
	Error  error
	Status string
}
