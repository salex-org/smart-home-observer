package hmip

import (
	"fmt"
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/cache"
	"github.com/salex-org/smart-home-observer/internal/util"
)

type DeviceChangedHandler func(device hmip.Device) error

type Client interface {
	Start(deviceChangedHandler DeviceChangedHandler) error
	Shutdown() error
	Health() error
}

type client struct {
	homematic       hmip.Homematic
	processingError error
	devicesCache    cache.Cache[hmip.Device]
	groupsCache     cache.Cache[hmip.Group]
}

func NewClient(devicesCache cache.Cache[hmip.Device], groupsCache cache.Cache[hmip.Group]) (Client, error) {
	client := client{
		devicesCache: devicesCache,
		groupsCache:  groupsCache,
	}
	config, err := hmip.GetConfig()
	if err != nil {
		return client, err
	}
	initializeConfig(config)
	client.homematic, err = hmip.GetClientWithConfig(config)
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

func (client client) Shutdown() error {
	return client.homematic.StopEventListening()
}

func (client client) Start(deviceChangedHandler DeviceChangedHandler) error {
	// Register event handler changes
	client.homematic.RegisterEventHandler(func(baseEvent hmip.Event, _ hmip.Origin) {
		switch event := baseEvent.(type) {
		case hmip.DeviceChangedEvent:
			if client.devicesCache.UpdateEntry(event.GetDevice()) {
				client.processingError = deviceChangedHandler(event.GetDevice())
			}
		case hmip.GroupChangedEvent:
			_ = client.groupsCache.UpdateEntry(event.GetGroup())
		}
	}, hmip.EVENT_TYPE_DEVICE_CHANGED, hmip.EVENT_TYPE_GROUP_CHANGED)
	fmt.Printf("HmIP: Event handler registered\n")

	// Read data initially
	var state hmip.State
	state, client.processingError = client.homematic.LoadCurrentState()
	if client.processingError == nil {
		fmt.Printf("HmIP: Reading intial state successful\n")
		for _, each := range state.GetGroups() {
			_ = client.groupsCache.UpdateEntry(each)
		}
		for _, each := range state.GetDevices() {
			if client.devicesCache.UpdateEntry(each) {
				client.processingError = deviceChangedHandler(each)
			}
		}
	} else {
		fmt.Printf("HmIP: Reading intial state failed %v\n", client.processingError)
		return client.processingError
	}

	// Start the event listening
	err := client.homematic.ListenForEvents()
	if err != nil {
		fmt.Printf("HmIP: Start event listening failed %v\n", err)
		return err
	}

	return nil
}

func (client client) Health() error {
	if client.processingError != nil {
		return client.processingError
	}
	return client.homematic.GetEventLoopState()
}
