package influx

import (
	"context"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/salex-org/smart-home-observer/internal/hmip"
	"github.com/salex-org/smart-home-observer/internal/util"
)

type Client interface {
	SaveMeasurement(measurement hmip.ClimateMeasurement) error
	SaveMeasurements(measurements []hmip.ClimateMeasurement) error
}

type InfluxClient struct {
	client               influxdb2.Client
	organization, bucket string
}

func NewClient() (Client, error) {
	client := InfluxClient{
		client:       influxdb2.NewClient(util.ReadEnvVar("INFLUX_URL"), util.ReadEnvVar("INFLUX_TOKEN")),
		organization: util.ReadEnvVar("INFLUX_ORGANIZATION"),
		bucket:       util.ReadEnvVar("INFLUX_BUCKET"),
	}
	_, err := client.client.Health(context.Background())
	return client, err
}

func (client InfluxClient) SaveMeasurement(measurement hmip.ClimateMeasurement) error {
	api := client.client.WriteAPIBlocking(client.organization, client.bucket)
	point := influxdb2.NewPointWithMeasurement("climate")
	point.SetTime(measurement.Time)
	point.AddField("temperature", measurement.Temperature)
	point.AddField("humidity", measurement.Humidity)
	point.AddTag("sensor", measurement.Sensor)
	return api.WritePoint(context.Background(), point)
}

func (client InfluxClient) SaveMeasurements(measurements []hmip.ClimateMeasurement) error {
	var err error
	for _, measurement := range measurements {
		err = client.SaveMeasurement(measurement)
		if err != nil {
			return err
		}
	}
	return nil
}
