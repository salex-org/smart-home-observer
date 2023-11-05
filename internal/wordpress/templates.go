package wordpress

import (
	"bytes"
	"github.com/salex-org/smart-home-observer/internal/hmip"
	"text/template"
)

var (
	overviewTemplate = `
<span class="salex_no-series-meta-information">
	{{range .}}
	<p style="text-align: left;">{{.Sensor}}: {{template "temperature" .Temperature}} bei {{template "humidity" .Humidity}}</p>
	<p style="text-align: left;"><span style="color: #808080;">Gemessen am {{template "timestamp" .Time}}</span></p>
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
	RenderOverview(measurements []hmip.ClimateMeasurement) (string, error)
}

type WordpressRenderer struct {
	overviewTemplate *template.Template
}

func NewRenderer() (Renderer, error) {
	overviewTemplate, err := initializeOverviewTemplate()
	if err != nil {
		return nil, err
	}
	renderer := &WordpressRenderer{
		overviewTemplate: overviewTemplate,
	}
	return renderer, nil
}

func (r *WordpressRenderer) RenderOverview(measurements []hmip.ClimateMeasurement) (string, error) {
	var buffer bytes.Buffer
	err := r.overviewTemplate.Execute(&buffer, measurements)
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
