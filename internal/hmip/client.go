package hmip

import (
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/data"
	"github.com/salex-org/smart-home-observer/internal/util"
)

type ClimateMeasurementHandler func([]data.ClimateMeasurement) error

type ConsumptionMeasurementHandler func([]data.ConsumptionMeasurement) error

type SwitchStateChangedHandler func([]data.SwitchState) error

type Client interface {
	Start(climateMeasurementHandler ClimateMeasurementHandler, consumptionMeasurementHandler ConsumptionMeasurementHandler, switchStateChangedHandler SwitchStateChangedHandler) error
	Shutdown() error
	Health() error
}

type ClientImpl struct {
	client          hmip.Client
	processingError error
}

func NewClient() (Client, error) {
	client := ClientImpl{}
	config, err := hmip.GetConfig()
	if err != nil {
		return client, err
	}
	initializeConfig(config)
	client.client, err = hmip.GetClientWithConfig(config)
	return client, err
}

func initializeConfig(config *hmip.Config) {
	config.AccessPointSGTIN = util.ReadEnvVar("HMIP_AP_SGTIN")
	config.DeviceID = util.ReadEnvVar("HMIP_DEVICE_ID")
	config.ClientID = util.ReadEnvVar("HMIP_CLIENT_ID")
	config.ClientName = util.ReadEnvVar("HMIP_CLIENT_NAME")
	config.ClientAuthToken = util.ReadEnvVar("HMIP_CLIENT_AUTH_TOKEN")
	config.AuthToken = util.ReadEnvVar("HMIP_AUTH_TOKEN")
}

func (client ClientImpl) Shutdown() error {
	return client.client.StopEventListening()
}

func (client ClientImpl) Start(climateMeasurementHandler ClimateMeasurementHandler, consumptionMeasurementHandler ConsumptionMeasurementHandler, switchStateChangedHandler SwitchStateChangedHandler) error {
	// Register event handler for climate measuring
	client.client.RegisterEventHandler(func(event hmip.Event, _ hmip.Origin) {
		var climateMeasurements []data.ClimateMeasurement
		for _, channel := range event.GetFunctionalChannels(hmip.DEVICE_TYPE_TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR, hmip.CHANNEL_TYPE_CLIMATE_SENSOR) {
			climateMeasurements = append(climateMeasurements, createClimateMeasurement(*event.Device, channel))
		}
		client.processingError = climateMeasurementHandler(climateMeasurements)
	}, hmip.EVENT_TYPE_DEVICE_CHANGED)

	// Register event handler for consumption measuring
	client.client.RegisterEventHandler(func(event hmip.Event, _ hmip.Origin) {
		var consumptionMeasurements []data.ConsumptionMeasurement
		for _, channel := range event.GetFunctionalChannels(hmip.DEVICE_TYPE_PLUGABLE_SWITCH_MEASURING, hmip.CHANNEL_TYPE_SWITCH_MEASURING) {
			consumptionMeasurements = append(consumptionMeasurements, createConsumptionMeasurement(*event.Device, channel))
		}
		client.processingError = consumptionMeasurementHandler(consumptionMeasurements)
	}, hmip.EVENT_TYPE_DEVICE_CHANGED)

	// Register event handler for switch state
	client.client.RegisterEventHandler(func(event hmip.Event, _ hmip.Origin) {
		var switchStates []data.SwitchState
		for _, channel := range event.GetFunctionalChannels(hmip.DEVICE_TYPE_PLUGABLE_SWITCH_MEASURING, hmip.CHANNEL_TYPE_SWITCH_MEASURING) {
			switchStates = append(switchStates, createSwitchState(*event.Device, channel))
		}
		for _, channel := range event.GetFunctionalChannels(hmip.DEVICE_TYPE_PLUGABLE_SWITCH, hmip.CHANNEL_TYPE_SWITCH) {
			switchStates = append(switchStates, createSwitchState(*event.Device, channel))
		}
		client.processingError = switchStateChangedHandler(switchStates)
	}, hmip.EVENT_TYPE_DEVICE_CHANGED)

	// Read data initially
	var state *hmip.State
	state, client.processingError = client.client.LoadCurrentState()
	if client.processingError == nil {
		var climateMeasurements []data.ClimateMeasurement
		var consumptionMeasurements []data.ConsumptionMeasurement
		var switchStates []data.SwitchState
		for _, device := range state.GetDevicesByType(hmip.DEVICE_TYPE_TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR) {
			for _, channel := range device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_CLIMATE_SENSOR) {
				climateMeasurements = append(climateMeasurements, createClimateMeasurement(device, channel))
			}
		}
		for _, device := range state.GetDevicesByType(hmip.DEVICE_TYPE_PLUGABLE_SWITCH_MEASURING) {
			for _, channel := range device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_SWITCH_MEASURING) {
				consumptionMeasurements = append(consumptionMeasurements, createConsumptionMeasurement(device, channel))
				switchStates = append(switchStates, createSwitchState(device, channel))
			}
		}
		for _, device := range state.GetDevicesByType(hmip.DEVICE_TYPE_PLUGABLE_SWITCH) {
			for _, channel := range device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_SWITCH) {
				switchStates = append(switchStates, createSwitchState(device, channel))
			}
		}
		client.processingError = climateMeasurementHandler(climateMeasurements)
		client.processingError = consumptionMeasurementHandler(consumptionMeasurements)
		client.processingError = switchStateChangedHandler(switchStates)
	}

	// Start the event listening
	return client.client.ListenForEvents()
}

func createClimateMeasurement(device hmip.Device, channel hmip.FunctionalChannel) data.ClimateMeasurement {
	return data.ClimateMeasurement{
		Measurement: data.Measurement{
			Time:   device.LastStatusUpdate.Time,
			Sensor: device.Name,
		},
		Humidity:    channel.Humidity,
		Temperature: channel.Temperature,
		VaporAmount: channel.VapourAmount,
	}
}

func createConsumptionMeasurement(device hmip.Device, channel hmip.FunctionalChannel) data.ConsumptionMeasurement {
	return data.ConsumptionMeasurement{
		Measurement: data.Measurement{
			Time:   device.LastStatusUpdate.Time,
			Sensor: device.Name,
		},
		CurrentConsumption: channel.CurrentPowerConsumption,
	}
}

func createSwitchState(device hmip.Device, channel hmip.FunctionalChannel) data.SwitchState {
	return data.SwitchState{
		Measurement: data.Measurement{
			Time:   device.LastStatusUpdate.Time,
			Sensor: device.Name,
		},
		On: channel.SwitchedOn,
	}
}

func (client ClientImpl) Health() error {
	if client.processingError != nil {
		return client.processingError
	}
	return client.client.GetEventLoopState()
}
