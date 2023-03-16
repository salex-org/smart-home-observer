package hmip

type Config struct {
	AccessPointSGTIN string `yaml:"accessPointSGTIN"`
	DeviceID         string `yaml:"deviceID"`
	ClientID         string `yaml:"clientID"`
	ClientAuthToken  string `yaml:"clientAuthToken"`
	AuthToken        string `yaml:"authToken"`
	LookupURL        string `yaml:"lookupURL"`
}

type GetHostsRequest struct {
	AccessPointSGTIN      string                `json:"id"`
	ClientCharacteristics ClientCharacteristics `json:"clientCharacteristics"`
}

type GetHostsResponse struct {
	RestURL               string `json:"urlREST"`
	WebSocketURL          string `json:"urlWebSocket"`
	ApiVersion            string `json:"apiVersion"`
	PrimaryAccessPoint    string `json:"primaryAccessPointId"`
	RequestingAccessPoint string `json:"requestingAccessPointId"`
}

type ClientCharacteristics struct {
	APIVersion         string `json:"apiVersion"`
	ClientName         string `json:"applicationIdentifier"`
	ClientVersion      string `json:"applicationVersion"`
	DeviceManufacturer string `json:"deviceManufacturer"`
	DeviceType         string `json:"deviceType"`
	Language           string `json:"language"`
	OSType             string `json:"osType"`
	OSVersion          string `json:"osVersion"`
}
