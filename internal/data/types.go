package data

import "time"

type Measurement struct {
	Time     time.Time `json:"time"`
	Sensor   string    `json:"sensor"`
	DeviceID string    `json:"deviceID"`
}

type ComparableMeasurement interface {
	GetTime() time.Time
	GetDeviceID() string
}

func (m Measurement) GetTime() time.Time {
	return m.Time
}

func (m Measurement) GetDeviceID() string {
	return m.DeviceID
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

type SwitchState struct {
	Measurement `json:",inline"`
	On          bool `json:"on"`
}

type Health struct {
	Error  error
	Status string
}
