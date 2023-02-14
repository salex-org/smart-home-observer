package mqtt

import (
	mqtt "github.com/eclipse/paho.mqtt.golang"
	"github.com/salex-org/smart-home-observer/internal/config"
)

type Client struct {
	mqtt *mqtt.Client
}

func ConnectToMQTT(onConnectHandler mqtt.OnConnectHandler) (*Client, error) {
	configuration, configErr := config.GetConfiguration()
	if configErr != nil {
		return nil, configErr
	}

	options := mqtt.NewClientOptions()
	options.AddBroker(configuration.MQTT.URL)
	options.SetUsername(configuration.MQTT.Username)
	options.SetPassword(configuration.MQTT.Password)
	options.SetClientID("smart-home-observer")
	options.SetOnConnectHandler(onConnectHandler)
	client := mqtt.NewClient(options)
	token := client.Connect()
	if token.Wait() && token.Error() != nil {
		return nil, token.Error()
	}

	return &Client{
		mqtt: &client,
	}, nil
}
