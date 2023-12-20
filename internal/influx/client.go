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
	SaveClimateMeasurement(measurement data.ClimateMeasurement) error
	SaveClimateMeasurements(measurements []data.ClimateMeasurement) error
	SaveConsumptionMeasurement(measurement data.ConsumptionMeasurement) error
	SaveConsumptionMeasurements(measurements []data.ConsumptionMeasurement) error
	SaveSwitchState(state data.SwitchState) error
	SaveSwitchStates(states []data.SwitchState) error
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

func (client InfluxClient) Shutdown() error {
	client.client.Close()
	return nil
}

func (client InfluxClient) SaveClimateMeasurement(measurement data.ClimateMeasurement) error {
	api := client.client.WriteAPIBlocking(client.organization, client.bucket)
	point := influxdb2.NewPointWithMeasurement("climate")
	point.SetTime(measurement.Time)
	point.AddField("temperature", measurement.Temperature)
	point.AddField("humidity", measurement.Humidity)
	point.AddField("vapor_amount", measurement.VaporAmount)
	point.AddTag("sensor", measurement.Sensor)
	point.AddTag("device", measurement.DeviceID)
	client.processingError = api.WritePoint(context.Background(), point)
	return client.processingError
}

func (client InfluxClient) SaveClimateMeasurements(measurements []data.ClimateMeasurement) error {
	var err error
	for _, measurement := range measurements {
		err = client.SaveClimateMeasurement(measurement)
		if err != nil {
			return err
		}
	}
	return nil
}

func (client InfluxClient) SaveConsumptionMeasurement(measurement data.ConsumptionMeasurement) error {
	api := client.client.WriteAPIBlocking(client.organization, client.bucket)
	point := influxdb2.NewPointWithMeasurement("consumption")
	point.SetTime(measurement.Time)
	point.AddField("electricity", measurement.CurrentConsumption)
	point.AddTag("sensor", measurement.Sensor)
	point.AddTag("device", measurement.DeviceID)
	client.processingError = api.WritePoint(context.Background(), point)
	return client.processingError
}

func (client InfluxClient) SaveConsumptionMeasurements(measurements []data.ConsumptionMeasurement) error {
	var err error
	for _, measurement := range measurements {
		err = client.SaveConsumptionMeasurement(measurement)
		if err != nil {
			return err
		}
	}
	return nil
}

func (client InfluxClient) SaveSwitchState(state data.SwitchState) error {
	api := client.client.WriteAPIBlocking(client.organization, client.bucket)
	point := influxdb2.NewPointWithMeasurement("switch")
	point.SetTime(state.Time)
	point.AddField("on", state.On)
	point.AddTag("sensor", state.Sensor)
	point.AddTag("device", state.DeviceID)
	client.processingError = api.WritePoint(context.Background(), point)
	return client.processingError
}

func (client InfluxClient) SaveSwitchStates(states []data.SwitchState) error {
	var err error
	for _, state := range states {
		err = client.SaveSwitchState(state)
		if err != nil {
			return err
		}
	}
	return nil
}

func (client InfluxClient) Health() error {
	return client.processingError
}
