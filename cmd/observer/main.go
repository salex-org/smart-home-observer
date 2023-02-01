package main

import (
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"gopkg.in/yaml.v2"
	"log"
	"net/http"
	"os"
)

type Configuration struct {
	database DatabaseConfiguration `yaml:"database"`
	mqtt     MQTTConfiguration     `yaml:"mqtt-broker"`
}

type DatabaseConfiguration struct {
	username string `yaml:"username"`
	password string `yaml:"password"`
}

type MQTTConfiguration struct {
	hostname string `yaml:"hostname"`
	username string `yaml:"username"`
	password string `yaml:"password"`
}

func main() {
	rawConfig, err := os.ReadFile("/config/observer-config.yml")
	if err != nil {
		fmt.Println(err)
		return
	}

	var config Configuration

	err = yaml.Unmarshal(rawConfig, &config)
	if err != nil {
		fmt.Println(err)
		return
	}

	port := 8080
	mux := http.NewServeMux()
	mux.Handle("/hello", &controller.HelloHandler{})
	mux.HandleFunc("/", handle404)
	fmt.Printf("Starting server on port %d\n", port)
	http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
}

func handle404(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s Hamwanich!", r.URL.Path)
	log.Printf("Answering 404 on %s\n", r.URL.Path)
}
