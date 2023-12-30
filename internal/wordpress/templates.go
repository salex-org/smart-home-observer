package wordpress

import (
	"bytes"
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/cache"
	"slices"
	"text/template"
	"time"
)

var (
	overviewTemplate = `
<span class="salex_no-series-meta-information">
	{{range .}}
	<p style="text-align: left;">{{.getName()}}: {{template "temperature" .getTemperature()}} bei {{template "humidity" .getHumidity()}}</p>
	<p style="text-align: left;"><span style="color: #808080;">Gemessen am {{template "timestamp" .getTime()}}</span></p>
	{{end}}
</span>
`

	timestampTemplate = `{{ .Format "02.01.2006" }} um {{ .Format "15:04:05" }}`

	temperatureTemplate      = `<strong><span style="color: {{template "temperatureColor" .}}">{{printf "%.1f" .}}&nbsp;&deg;C</span></strong>`
	temperatureColorTemplate = `{{if le . 0.0}}#180795;{{else if lt . 10.0}}#0056d6;{{else if lt . 25.0}}#099f23;{{else if lt . 35.0}}#dd7b1d;{{else}}#d60a13;{{end}}`

	humidityTemplate      = `<span style="color: {{template "humidityColor" .}}">{{printf "%d" .}}&nbsp;%</span>`
	humidityColorTemplate = `{{if or (lt . 10) (gt . 90)}}#d60a13;{{else if or (lt . 25) (gt . 75)}}#dd7b1d;{{else}}#099f23;{{end}}`
)

type Renderer interface {
	RenderOverview(deviceNames []string) (string, error)
}

type renderer struct {
	overviewTemplate *template.Template
	devicesCache     cache.Cache[hmip.Device]
}

type climateDataEntry struct {
	name        string
	humidity    int
	temperature float64
	timestamp   time.Time
}

func (cde climateDataEntry) getName() string {
	return cde.name
}
func (cde climateDataEntry) getHumidity() int {
	return cde.humidity
}
func (cde climateDataEntry) getTemperature() float64 {
	return cde.temperature
}
func (cde climateDataEntry) getTime() time.Time {
	return cde.timestamp
}

func (r *renderer) getClimateDataEntries(deviceNames []string) []climateDataEntry {
	var entries []climateDataEntry
	for _, device := range r.devicesCache.GetAllEntries() {
		if slices.Contains(deviceNames, device.GetName()) && device.GetType() == hmip.DEVICE_TYPE_TEMPERATURE_HUMIDITY_SENSOR_OUTDOOR {
			entries = append(entries, getClimateDataEntry(device))
		}
	}
	return entries
}
func getClimateDataEntry(device hmip.Device) climateDataEntry {
	cde := climateDataEntry{
		name:      device.GetName(),
		timestamp: device.GetLastUpdated(),
	}
	for _, base := range device.GetFunctionalChannels() {
		switch channel := base.(type) {
		case hmip.ClimateSensorChannel:
			cde.humidity = channel.GetHumidity()
			cde.temperature = channel.GetActualTemperature()
		}
	}
	return cde
}

func NewRenderer(devicesCache cache.Cache[hmip.Device]) (Renderer, error) {
	overviewTemplate, err := initializeOverviewTemplate()
	if err != nil {
		return nil, err
	}
	renderer := &renderer{
		overviewTemplate: overviewTemplate,
		devicesCache:     devicesCache,
	}
	return renderer, nil
}

func (r *renderer) RenderOverview(deviceNames []string) (string, error) {
	var buffer bytes.Buffer
	err := r.overviewTemplate.Execute(&buffer, r.getClimateDataEntries(deviceNames))
	if err != nil {
		return "", err
	}
	return buffer.String(), nil
}

func initializeOverviewTemplate() (*template.Template, error) {
	overviewTemplate, err := template.New("overview").Parse(overviewTemplate)
	if err != nil {
		return overviewTemplate, err
	}
	_, err = overviewTemplate.New("timestamp").Parse(timestampTemplate)
	if err != nil {
		return overviewTemplate, err
	}
	_, err = overviewTemplate.New("temperature").Parse(temperatureTemplate)
	if err != nil {
		return overviewTemplate, err
	}
	_, err = overviewTemplate.New("temperatureColor").Parse(temperatureColorTemplate)
	if err != nil {
		return overviewTemplate, err
	}
	_, err = overviewTemplate.New("humidity").Parse(humidityTemplate)
	if err != nil {
		return overviewTemplate, err
	}
	_, err = overviewTemplate.New("humidityColor").Parse(humidityColorTemplate)
	return overviewTemplate, err
}
