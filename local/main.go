package main

import (
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/config"
	"github.com/salex-org/smart-home-observer/internal/hmip"
)

func main() {
	configuration, err := config.GetConfiguration("local/config.yml")
	if err != nil {
		fmt.Printf("error: %v\n", err)
		return
	}
	var client *hmip.Client
	client, err = hmip.NewClient(&configuration.HomematicIP)
	if err != nil {
		fmt.Printf("error: %v\n", err)
		return
	}
	err = client.Connect()
	if err != nil {
		fmt.Printf("error: %v\n", err)
		return
	}
}
