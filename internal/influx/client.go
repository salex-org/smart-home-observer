package influx

import (
	"context"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	config "github.com/salex-org/smart-home-observer/internal/config"
)

func ConnectToInflux() (influxdb2.Client, error) {
	configuration, configErr := config.GetConfiguration()
	if configErr != nil {
		return nil, configErr
	}
	client := influxdb2.NewClient(configuration.Database.URL, configuration.Database.Token)
	_, connectErr := client.Health(context.Background())
	return client, connectErr
}
