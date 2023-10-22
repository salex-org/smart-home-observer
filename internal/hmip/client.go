package hmip

import (
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/util"
	"time"
)

type Client interface {
	ReadMeasurements() ([]ClimateMeasurement, error)
}

type HmIPClient struct {
	client hmip.Client
}

type ClimateMeasurement struct {
	Time        time.Time `json:"time"`
	Sensor      string    `json:"sensor"`
	Humidity    int       `json:"humidity"`
	Temperature float64   `json:"temperature"`
	VaporAmount float64   `json:"vaporAmount"`
}

func NewClient() (Client, error) {
	client := HmIPClient{}
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

func (client HmIPClient) ReadMeasurements() ([]ClimateMeasurement, error) {
	measurements := []ClimateMeasurement{}
	state, err := client.client.LoadCurrentState()
	if err != nil {
		return measurements, err
	}
	for _, device := range state.Devices {
		if device.Type == "TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR" {
			for _, channel := range device.Channels {
				if channel.Type == "CLIMATE_SENSOR_CHANNEL" {
					measurement := ClimateMeasurement{
						Time:        device.LastStatusUpdate.Time,
						Sensor:      device.Name,
						Humidity:    channel.Humidity,
						Temperature: channel.Temperature,
						VaporAmount: channel.VapourAmount,
					}
					measurements = append(measurements, measurement)
				}
			}
		}
	}
	return measurements, nil
}
