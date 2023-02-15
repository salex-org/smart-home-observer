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

type MQTTController struct {
	bucket api.WriteAPI
}

type Measurement struct {
	Timestamp   time.Time `json:"time"`
	Value       float64   `json:"value"`
	Sensor      string    `json:"sensor"`
	Measurement string    `json:"measurement"`
	Field       string    `json:"field"`
}

func NewMQTTController() (*MQTTController, error) {
	var controller MQTTController
	configuration, configErr := config.GetConfiguration()
	if configErr != nil {
		return nil, configErr
	}

	influx, connectErr := influx.ConnectToInflux()
	if connectErr != nil {
		return nil, connectErr
	}

	controller.bucket = influx.WriteAPI(configuration.Database.Org, configuration.Database.Bucket)
	fmt.Printf("Connected to InfluxDB (URL: %s, Org: %s, Bucket: %s)\n",
		influx.ServerURL(),
		configuration.Database.Org,
		configuration.Database.Bucket)
	return &controller, nil
}

func (c *MQTTController) HandleMessage(client mqtt.Client, message mqtt.Message) {
	var measurement Measurement
	jsonErr := json.Unmarshal(message.Payload(), &measurement)
	if jsonErr != nil {
		fmt.Printf("Error unmarshalling message: %v\n", jsonErr)
		return
	}
	point := influxdb2.NewPointWithMeasurement(measurement.Measurement).
		AddTag("sensor", measurement.Sensor).
		AddField(measurement.Field, measurement.Value).
		SetTime(measurement.Timestamp)
	c.bucket.WritePoint(point)
	c.bucket.Flush()
}

func (c *MQTTController) HandleConnect(client mqtt.Client) {
	configuration, confErr := config.GetConfiguration()
	if confErr != nil {
		fmt.Printf("Error reading configuration: %v\n", confErr)
		return
	}
	token := client.Subscribe(configuration.MQTT.Topic, 2, c.HandleMessage)
	if token.Wait() && token.Error() != nil {
		fmt.Printf("Error adding MQTT subscriber: %v\n", token.Error())
	}
	fmt.Printf("Added MQTT subscriber (Topic: %s)\n", configuration.MQTT.Topic)
}
