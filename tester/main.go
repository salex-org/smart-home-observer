package main

import (
	"encoding/json"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"math/rand"
	"time"
)

type Measurement struct {
	Timestamp time.Time `json:"time"`
	Value     float64   `json:"value"`
	Unit      string    `json:"unit"`
	Sensor    string    `json:"sensor"`
	Kind      string    `json:"kind"`
}

func main() {
	options := mqtt.NewClientOptions()
	// broker IP and port
	options.AddBroker("tcp://localhost:1883")
	options.SetUsername("local")
	options.SetPassword("localsecret")
	options.SetClientID("smart-home-observer-tester")
	client := mqtt.NewClient(options)

	token := client.Connect()
	token.Wait()
	if token.Error() != nil {
		panic(token.Error())
	}

	// Add a random value between 3.0 and 6.0 every 5 to 10 seconds :-)
	for {
		measurement := Measurement{
			Timestamp: time.Now(),
			Value:     (rand.Float64() * 3) + 3,
			Unit:      "kWh",
			Kind:      "electricity",
			Sensor:    "main",
		}
		value, _ := json.Marshal(&measurement)
		token = client.Publish("consumption", 2, false, value)
		token.Wait()
		if token.Error() != nil {
			panic(token.Error())
		}
		waiting := (rand.Intn(5) + 5)
		time.Sleep(time.Duration(waiting) * time.Second)
	}

}
