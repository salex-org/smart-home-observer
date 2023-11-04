package wordpress

import (
	"bytes"
	"encoding/json"
	"errors"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/util"
	"io"
	"net/http"
	"time"
)

type Client interface {
	GetPost(id int, t Type) (*Post, error)
	UpdatePost(post *Post) error
}

type WordpressClient struct {
	httpClient *http.Client
	endpoint   string
}

func NewClient() Client {
	client := &WordpressClient{
		httpClient: &http.Client{
			Transport: &WordpressRoundTripper{
				Origin: http.DefaultTransport,
				user:   util.ReadEnvVar("WORDPRESS_USERNAME"),
				token:  util.ReadEnvVar("WORDPRESS_TOKEN"),
			},
			Timeout: 30 * time.Second,
		},
		endpoint: util.ReadEnvVar("WORDPRESS_ENDPOINT"),
	}
	return client
}

type WordpressRoundTripper struct {
	Origin http.RoundTripper
	user   string
	token  string
}

func (r *WordpressRoundTripper) RoundTrip(request *http.Request) (*http.Response, error) {
	request.Header.Set("Content-Type", "application/json")
	request.SetBasicAuth(r.user, r.token)
	return r.Origin.RoundTrip(request)
}

func (c *WordpressClient) GetPost(id int, t Type) (*Post, error) {
	url := fmt.Sprintf("%s/%s/%s/%d", c.endpoint, API_PATH, t, id)
	var response *http.Response
	request, err := http.NewRequest("GET", url, nil)
	if err != nil {
		return nil, err
	}
	response, err = c.httpClient.Do(request)
	if err != nil {
		return nil, err
	}
	if response.StatusCode != 200 {
		return nil, errors.New(fmt.Sprintf("Error reading post (%s)", response.Status))
	}
	defer func(Body io.ReadCloser) {
		_ = Body.Close()
	}(response.Body)
	responseBody, _ := io.ReadAll(response.Body)
	post := Post{}
	err = json.Unmarshal(responseBody, &post)
	if err != nil {
		return nil, err
	}
	return &post, nil
}

func (c *WordpressClient) UpdatePost(post *Post) error {
	url := fmt.Sprintf("%s/%s/%s/%d", c.endpoint, API_PATH, post.Type, post.ID)
	requestBody, _ := json.Marshal(post)
	var response *http.Response
	request, err := http.NewRequest("POST", url, bytes.NewReader(requestBody))
	if err != nil {
		return err
	}
	response, err = c.httpClient.Do(request)
	if err != nil {
		return err
	}
	if response.StatusCode != 200 {
		return errors.New(fmt.Sprintf("Error updating post (%s)", response.Status))
	}
	return nil
}
