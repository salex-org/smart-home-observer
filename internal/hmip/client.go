package hmip

import (
	"bytes"
	"encoding/json"
	"fmt"
	"io"
	"net/http"
	"regexp"
	"runtime"
	"time"
)

type Client struct {
	config     *Config
	httpClient *http.Client
}

func NewClient(config *Config) (*Client, error) {
	return &Client{
		config: config,
		httpClient: &http.Client{
			Timeout: time.Duration(1) * time.Minute,
		},
	}, nil
}

func (client *Client) GetCurrentState() error {
	return nil
}

func trimSGTIN(sgtin string) string {
	expression, _ := regexp.Compile("[^a-fA-F0-9]")
	return expression.ReplaceAllString(sgtin, "")
}

func (client *Client) Connect() error {
	fmt.Printf("Connecting to Homematic-IP Cloud...")
	var err error
	var requestBodyJSON, responseBodyJSON []byte
	var requestBody = GetHostsRequest{
		AccessPointSGTIN: trimSGTIN(client.config.AccessPointSGTIN),
		ClientCharacteristics: ClientCharacteristics{
			APIVersion:         "12",
			ClientName:         "GoLang Client",
			ClientVersion:      "1.0.0",
			DeviceManufacturer: "none",
			DeviceType:         "computer",
			Language:           "de-DE",
			OSType:             runtime.GOOS,
			OSVersion:          "",
		},
	}
	var response *http.Response
	requestBodyJSON, err = json.Marshal(requestBody)
	if err != nil {
		return err
	}
	response, err = client.httpClient.Post(client.config.LookupURL, "application/json", bytes.NewBuffer(requestBodyJSON))
	if err != nil {
		return err
	}
	defer response.Body.Close()
	responseBodyJSON, err = io.ReadAll(response.Body)
	if err != nil {
		return err
	}
	responseBody := &GetHostsResponse{}
	err = json.Unmarshal(responseBodyJSON, responseBody)
	if err != nil {
		return err
	}
	fmt.Printf("success\nUsing REST-API at %s\n", responseBody.RestURL)
	return nil
}
