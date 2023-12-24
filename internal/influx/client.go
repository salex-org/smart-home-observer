package influx

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/salex-org/smart-home-observer/internal/data"
	"github.com/salex-org/smart-home-observer/internal/util"
	"os"
)

type Client interface {
	SaveDeviceStates(devices []data.Device) error
	SaveDeviceState(device data.Device) error
	Shutdown() error
	Health() error
}

type InfluxClient struct {
	client               influxdb2.Client
	organization, bucket string
	processingError      error
}

func NewClient() (Client, error) {
	certFilename, hasAdditionalRootCA := os.LookupEnv("ADDITIONAL_ROOT_CA")
	rootCAs, _ := x509.SystemCertPool()
	if rootCAs == nil {
		rootCAs = x509.NewCertPool()
	}
	if hasAdditionalRootCA {
		cert, err := os.ReadFile(certFilename)
		if err != nil {
			return nil, err
		} else {
			rootCAs.AppendCertsFromPEM(cert)
		}
	}
	client := InfluxClient{
		client: influxdb2.NewClientWithOptions(
			util.ReadEnvVar("INFLUX_URL"),
			util.ReadEnvVar("INFLUX_TOKEN"),
			influxdb2.DefaultOptions().
				SetTLSConfig(&tls.Config{
					RootCAs: rootCAs,
				})),
		organization: util.ReadEnvVar("INFLUX_ORGANIZATION"),
		bucket:       util.ReadEnvVar("INFLUX_BUCKET"),
	}
	_, err := client.client.Health(context.Background())
	return client, err
}

func (c InfluxClient) SaveDeviceStates(devices []data.Device) error {
	var err error
	for _, device := range devices {
		err = c.SaveDeviceState(device)
		if err != nil {
			return err
		}
	}
	return nil
}

func (c InfluxClient) SaveDeviceState(device data.Device) error {
	api := c.client.WriteAPIBlocking(c.organization, c.bucket)
	point := influxdb2.NewPointWithMeasurement("device")
	point.SetTime(device.GetTime())
	point.AddField("connection_quality", device.GetConnectionQuality())
	point.AddField("unreached", device.GetUnreached())
	point.AddField("low_battery", device.GetLowBattery())
	point.AddTag("device_name", device.GetName())
	point.AddTag("device_type", device.GetType())
	point.AddTag("device_id", device.GetID())
	point.AddTag("group_name", device.GetMetaGroup().GetName())
	point.AddTag("group_id", device.GetMetaGroup().GetID())
	switch device := device.(type) {
	case data.Switchable:
		point.AddField("switched_on", device.IsSwitchedIn())
	case data.ConsumptionMeasuring:
		point.AddField("current_consumption", device.GetConsumption())
	case data.ClimateMeasuring:
		point.AddField("temperature", device.GetTemperature())
		point.AddField("humidity", device.GetHumidity())
		point.AddField("vapor_amount", device.GetVaporAmount())
	}
	c.processingError = api.WritePoint(context.Background(), point)
	return c.processingError
}

func (c InfluxClient) Shutdown() error {
	c.client.Close()
	return nil
}

func (client InfluxClient) Health() error {
	return client.processingError
}
