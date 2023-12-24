package hmip

import (
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/data"
	"github.com/salex-org/smart-home-observer/internal/util"
	"slices"
	"time"
)

type DeviceChangedHandler func(data.Device) error

type Client interface {
	Start(deviceChangedHandler DeviceChangedHandler) error
	GetCachedData() map[string]any
	GetCachedDeviceClimateData(sensorNames []string) []data.ClimateMeasuring
	Shutdown() error
	Health() error
}

type ClientImpl struct {
	client          hmip.Client
	processingError error
	devicesCache    data.Cache[data.Device]
	groupsCache     data.Cache[data.Group]
}

func NewClient() (Client, error) {
	client := ClientImpl{
		devicesCache: data.NewCache[data.Device](),
		groupsCache:  data.NewCache[data.Group](),
	}
	config, err := hmip.GetConfig()
	if err != nil {
		return client, err
	}
	initializeConfig(config)
	client.client, err = hmip.GetClientWithConfig(config)
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

func (client ClientImpl) Shutdown() error {
	return client.client.StopEventListening()
}

func (client ClientImpl) Start(deviceChangedHandler DeviceChangedHandler) error {
	// Register event handler for device changes
	client.client.RegisterEventHandler(func(event hmip.Event, _ hmip.Origin) {
		device, updated := client.updateDevice(*event.Device)
		if updated {
			client.processingError = deviceChangedHandler(device)
		}
	}, hmip.EVENT_TYPE_DEVICE_CHANGED)

	// Register event handler for group changes
	client.client.RegisterEventHandler(func(event hmip.Event, _ hmip.Origin) {
		_, _ = client.updateGroup(*event.Group)
	}, hmip.EVENT_TYPE_GROUP_CHANGED)

	// Read data initially
	var state *hmip.State
	state, client.processingError = client.client.LoadCurrentState()
	if client.processingError == nil {
		for _, each := range state.Groups {
			_, _ = client.updateGroup(each)
		}
		for _, each := range state.Devices {
			device, updated := client.updateDevice(each)
			if updated {
				client.processingError = deviceChangedHandler(device)
			}
		}
	}

	// Start the event listening
	return client.client.ListenForEvents()
}

// updateDevice transforms the given device into a data.Device and
// updates the entry in the devices cache. Returns true, if the device was added
// or updated in the cache, otherwise return false.
func (client ClientImpl) updateDevice(device hmip.Device) (data.Device, bool) {
	var base data.BaseDevice
	baseChannels := device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_DEVICE_BASE)
	if len(baseChannels) > 0 {
		base = data.BaseDevice{
			Status: data.Status{
				ID:   device.ID,
				Time: device.LastStatusUpdate.Time,
			},
			Type: device.Type,
			Name: device.Name,
		}
		base.LowBattery = baseChannels[0].LowBattery
		base.Unreached = baseChannels[0].Unreached
		base.ConnectionQuality = baseChannels[0].RSSIValue // TODO calculate
		if len(baseChannels[0].Groups) > 0 {
			base.MetaGroup = client.groupsCache.GetEntryByID(baseChannels[0].Groups[0])
		}
	}
	switch device.Type {
	case hmip.DEVICE_TYPE_TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR:
		climate := data.ClimateDevice{
			BaseDevice: base,
		}
		channels := device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_CLIMATE_SENSOR)
		if len(channels) > 0 {
			climate.Temperature = channels[0].Temperature
			climate.Humidity = channels[0].Humidity
			climate.VaporAmount = channels[0].VapourAmount
		}
		return climate, client.devicesCache.UpdateEntry(climate)
	case hmip.DEVICE_TYPE_PLUGABLE_SWITCH:
		switchable := data.SwitchingDevice{
			BaseDevice: base,
		}
		channels := device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_SWITCH)
		if len(channels) > 0 {
			switchable.SwitchedOn = channels[0].SwitchedOn
		}
		return switchable, client.devicesCache.UpdateEntry(switchable)
	case hmip.DEVICE_TYPE_PLUGABLE_SWITCH_MEASURING:
		switchableMeasuring := data.SwitchingMeasuringDevice{
			BaseDevice: base,
		}
		channels := device.GetFunctionalChannelsByType(hmip.CHANNEL_TYPE_SWITCH_MEASURING)
		if len(channels) > 0 {
			switchableMeasuring.SwitchedOn = channels[0].SwitchedOn
			switchableMeasuring.CurrentConsumption = channels[0].CurrentPowerConsumption
		}
		return switchableMeasuring, client.devicesCache.UpdateEntry(switchableMeasuring)
	default:
		return base, client.devicesCache.UpdateEntry(base)
	}
}

// updateGroup transforms the given group into a data.Group and
// updates the entry in the groups cache. Returns true, if the device was added
// or updated in the cache, otherwise return false.
func (client ClientImpl) updateGroup(group hmip.Group) (data.Group, bool) {
	cacheable := data.MetaGroup{
		Status: data.Status{
			ID:   group.ID,
			Time: time.Now(), // To always update the cache, use current timstamp
		},
		Type: group.Type,
		Name: group.Name,
	}
	return cacheable, client.groupsCache.UpdateEntry(cacheable)
}

func (client ClientImpl) Health() error {
	if client.processingError != nil {
		return client.processingError
	}
	return client.client.GetEventLoopState()
}

func (client ClientImpl) GetCachedData() map[string]any {
	cache := make(map[string]any)
	cache["devices"] = client.devicesCache
	cache["groups"] = client.groupsCache
	return cache
}

func (client ClientImpl) GetCachedDeviceClimateData(sensorNames []string) []data.ClimateMeasuring {
	var devices []data.ClimateMeasuring
	for _, device := range client.devicesCache.GetAllEntries() {
		if slices.Contains(sensorNames, device.GetName()) {
			switch device := device.(type) {
			case data.ClimateMeasuring:
				devices = append(devices, device)
			}
		}
	}
	return devices
}
