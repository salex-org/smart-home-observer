package data

import (
	"time"
)

type Cacheable interface {
	GetTime() time.Time
	GetID() string
}

type Device interface {
	Cacheable
	GetName() string
	GetType() string
	GetMetaGroup() Group
	GetLowBattery() bool
	GetUnreached() bool
	GetConnectionQuality() int
}

type Switchable interface {
	IsSwitchedIn() bool
}

type ConsumptionMeasuring interface {
	GetConsumption() float64
}

type ClimateMeasuring interface {
	GetTemperature() float64
	GetHumidity() int
	GetVaporAmount() float64
}

type Group interface {
	Cacheable
	GetName() string
	GetType() string
}

type Status struct {
	ID   string    `json:"id"`
	Time time.Time `json:"updated"`
}

func (s Status) GetTime() time.Time {
	return s.Time
}

func (s Status) GetID() string {
	return s.ID
}

type BaseDevice struct {
	Status            `json:",inline"`
	Name              string `json:"name"`
	Type              string `json:"type"`
	MetaGroup         Group  `json:"metaGroup"`
	LowBattery        bool   `json:"lowBattery"`
	Unreached         bool   `json:"unreached"`
	ConnectionQuality int    `json:"connectionQuality"`
}

func (b BaseDevice) GetName() string {
	return b.Name
}

func (b BaseDevice) GetType() string {
	return b.Type
}

func (b BaseDevice) GetMetaGroup() Group {
	return b.MetaGroup
}

func (b BaseDevice) GetLowBattery() bool {
	return b.LowBattery
}

func (b BaseDevice) GetUnreached() bool {
	return b.Unreached
}

func (b BaseDevice) GetConnectionQuality() int {
	return b.ConnectionQuality
}

type ClimateDevice struct {
	BaseDevice  `json:",inline"`
	Humidity    int     `json:"humidity"`
	Temperature float64 `json:"temperature"`
	VaporAmount float64 `json:"vaporAmount"`
}

func (cd ClimateDevice) GetHumidity() int {
	return cd.Humidity
}

func (cd ClimateDevice) GetTemperature() float64 {
	return cd.Temperature
}

func (cd ClimateDevice) GetVaporAmount() float64 {
	return cd.VaporAmount
}

type SwitchingDevice struct {
	BaseDevice `json:",inline"`
	SwitchedOn bool `json:"switchedOn"`
}

func (sd SwitchingDevice) IsSwitchedIn() bool {
	return sd.SwitchedOn
}

type SwitchingMeasuringDevice struct {
	BaseDevice         `json:",inline"`
	SwitchedOn         bool    `json:"switchedOn"`
	CurrentConsumption float64 `json:"currentConsumption"`
}

func (smd SwitchingMeasuringDevice) GetConsumption() float64 {
	return smd.CurrentConsumption
}

func (smd SwitchingMeasuringDevice) IsSwitchedIn() bool {
	return smd.SwitchedOn
}

type MetaGroup struct {
	Status `json:",inline"`
	Name   string `json:"name"`
	Type   string `json:"type"`
}

func (mg MetaGroup) GetName() string {
	return mg.Name
}

func (mg MetaGroup) GetType() string {
	return mg.Type
}

type Health struct {
	Error  error
	Status string
}
