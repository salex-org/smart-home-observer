package controller

import (
	"encoding/json"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/influxdata/influxdb-client-go/v2/api"
	"github.com/salex-org/smart-home-observer/internal/config"
	"github.com/salex-org/smart-home-observer/internal/influx"
	"time"
)

type ConsumptionController struct {
	bucket api.WriteAPI
}

type Measurement struct {
	Timestamp time.Time `json:"time"`
	Value     float64   `json:"value"`
	Unit      string    `json:"unit"`
	Sensor    string    `json:"sensor"`
	Kind      string    `json:"kind"`
}

func NewConsumptionController() (*ConsumptionController, error) {
	var controller ConsumptionController
	configuration, configErr := config.GetConfiguration()
	if configErr != nil {
		return nil, configErr
	}

	influx, connectErr := influx.ConnectToInflux()
	if connectErr != nil {
		return nil, connectErr
	}

	controller.bucket = influx.WriteAPI(configuration.Database.Org, configuration.Database.Buckets.Consumption)
	fmt.Printf("Connected to InfluxDB (URL: %s, Org: %s, Bucket: %s)\n",
		influx.ServerURL(),
		configuration.Database.Org,
		configuration.Database.Buckets.Consumption)
	return &controller, nil
}

func (c *ConsumptionController) HandleMessage(client mqtt.Client, message mqtt.Message) {
	var measurement Measurement
	jsonErr := json.Unmarshal(message.Payload(), &measurement)
	if jsonErr != nil {
		fmt.Printf("Error unmarshalling message: %v\n", jsonErr)
		return
	}
	point := influxdb2.NewPointWithMeasurement(measurement.Kind).
		AddTag("unit", measurement.Unit).
		AddTag("sensor", measurement.Sensor).
		AddField("current", measurement.Value).
		SetTime(measurement.Timestamp)
	c.bucket.WritePoint(point)
	c.bucket.Flush()
}

func (c *ConsumptionController) HandleConnect(client mqtt.Client) {
	configuration, confErr := config.GetConfiguration()
	if confErr != nil {
		fmt.Printf("Error reading configuration: %v\n", confErr)
		return
	}
	token := client.Subscribe(configuration.MQTT.Topics.Consumption, 2, c.HandleMessage)
	if token.Wait() && token.Error() != nil {
		fmt.Printf("Error adding MQTT subscriber: %v\n", token.Error())
	}
	fmt.Printf("Added MQTT subscriber (Topic: %s)\n", configuration.MQTT.Topics.Consumption)
}
