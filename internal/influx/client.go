package influx

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/cache"
	"github.com/salex-org/smart-home-observer/internal/util"
	"os"
)

type Client interface {
	SaveDeviceStates(devices hmip.Devices) error
	SaveDeviceState(device hmip.Device) error
	Shutdown() error
	Health() error
}

type client struct {
	client               influxdb2.Client
	organization, bucket string
	processingError      error
	groupsCache          cache.Cache[hmip.Group]
}

func NewClient(groupsCache cache.Cache[hmip.Group]) (Client, error) {
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
	client := client{
		client: influxdb2.NewClientWithOptions(
			util.ReadEnvVar("INFLUX_URL"),
			util.ReadEnvVar("INFLUX_TOKEN"),
			influxdb2.DefaultOptions().
				SetTLSConfig(&tls.Config{
					RootCAs: rootCAs,
				})),
		organization: util.ReadEnvVar("INFLUX_ORGANIZATION"),
		bucket:       util.ReadEnvVar("INFLUX_BUCKET"),
		groupsCache:  groupsCache,
	}
	_, err := client.client.Health(context.Background())
	return client, err
}

func (c client) SaveDeviceStates(devices hmip.Devices) error {
	var err error
	for _, device := range devices {
		err = c.SaveDeviceState(device)
		if err != nil {
			return err
		}
	}
	return nil
}

func (c client) SaveDeviceState(device hmip.Device) error {
	api := c.client.WriteAPIBlocking(c.organization, c.bucket)
	point := influxdb2.NewPointWithMeasurement("device")
	point.SetTime(device.GetLastUpdated())
	point.AddTag("device_name", device.GetName())
	point.AddTag("device_type", device.GetType())
	point.AddTag("device_id", device.GetID())
	hasBase := false
	for _, base := range device.GetFunctionalChannels() {
		if channel, implemented := base.(hmip.BaseDeviceChannel); implemented {
			hasBase = true
			point.AddField("connection_quality", calculateConnectionQualityFromChannel(channel))
			point.AddField("unreached", channel.IsUnreached())
			point.AddField("low_battery", channel.HasLowBattery())
			point.AddField("under_voltage", channel.HasUnderVoltage())
			point.AddField("overheated", channel.IsOverheated())
			metaGroup := c.getMetaGroupFromChannel(channel)
			if metaGroup != nil {
				point.AddTag("group_name", metaGroup.GetName())
				point.AddTag("group_id", metaGroup.GetID())
			}
		}
		if channel, implemented := base.(hmip.Switchable); implemented {
			point.AddField("switched_on", channel.IsSwitchedOn())
		}
		if channel, implemented := base.(hmip.PowerConsumptionMeasuring); implemented {
			point.AddField("current_power_consumption", channel.GetCurrentPowerConsumption())
		}
		if channel, implemented := base.(hmip.ClimateMeasuring); implemented {
			point.AddField("actual_temperature", channel.GetActualTemperature())
			point.AddField("humidity", channel.GetHumidity())
			point.AddField("vapour_amount", channel.GetVapourAmount())
		}
	}
	if hasBase {
		c.processingError = api.WritePoint(context.Background(), point)
	}
	return c.processingError
}

func (c client) Shutdown() error {
	c.client.Close()
	return nil
}

func (c client) Health() error {
	return c.processingError
}

func calculateConnectionQualityFromChannel(channel hmip.BaseDeviceChannel) int {
	rssi := channel.GetRSSIValue()
	if rssi < -100 || rssi > 0 {
		return 0
	}
	return rssi + 100
}

func (c client) getMetaGroupFromChannel(channel hmip.BaseDeviceChannel) hmip.MetaGroup {
	for _, groupID := range channel.GetGroups() {
		switch group := c.groupsCache.GetEntryByID(groupID).(type) {
		case hmip.MetaGroup:
			return group
		}
	}
	return nil
}
