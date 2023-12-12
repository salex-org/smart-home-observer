package influx

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/salex-org/smart-home-observer/internal/data"
	"github.com/salex-org/smart-home-observer/internal/util"
	"log"
	"os"
)

type Client interface {
	SaveClimateMeasurement(measurement data.ClimateMeasurement) error
	SaveClimateMeasurements(measurements []data.ClimateMeasurement) error
	SaveConsumptionMeasurement(measurement data.ConsumptionMeasurement) error
	SaveConsumptionMeasurements(measurements []data.ConsumptionMeasurement) error
	Shutdown() error
	Health() error
}

type InfluxClient struct {
	client                                         influxdb2.Client
	organization, consumptionBucket, climateBucket string
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
			log.Fatalf("Error reading additional root CA: %v\n", err)
		} else {
			rootCAs.AppendCertsFromPEM(cert)
			log.Printf("Using additional root CA from %s\n", certFilename)
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
		organization:      util.ReadEnvVar("INFLUX_ORGANIZATION"),
		climateBucket:     util.ReadEnvVar("INFLUX_CLIMATE_BUCKET"),
		consumptionBucket: util.ReadEnvVar("INFLUX_CONSUMPTION_BUCKET"),
	}
	_, err := client.client.Health(context.Background())
	return client, err
}

func (client InfluxClient) Shutdown() error {
	return client.Shutdown()
}

func (client InfluxClient) SaveClimateMeasurement(measurement data.ClimateMeasurement) error {
	api := client.client.WriteAPIBlocking(client.organization, client.climateBucket)
	point := influxdb2.NewPointWithMeasurement("climate")
	point.SetTime(measurement.Time)
	point.AddField("temperature", measurement.Temperature)
	point.AddField("humidity", measurement.Humidity)
	point.AddField("vapor_amount", measurement.VaporAmount)
	point.AddTag("sensor", measurement.Sensor)
	return api.WritePoint(context.Background(), point)
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
	api := client.client.WriteAPIBlocking(client.organization, client.consumptionBucket)
	point := influxdb2.NewPointWithMeasurement("consumption")
	point.SetTime(measurement.Time)
	point.AddField("electricity", measurement.CurrentConsumption)
	point.AddTag("sensor", measurement.Sensor)
	return api.WritePoint(context.Background(), point)
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

func (client InfluxClient) Health() error {
	// TODO implement
	return nil
}
