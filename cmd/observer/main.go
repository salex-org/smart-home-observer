package main

import (
	"encoding/json"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/hmip"
	"github.com/salex-org/smart-home-observer/internal/influx"
	"github.com/salex-org/smart-home-observer/internal/util"
	"log"
	"net/http"
	"strconv"
	"time"
)

var (
	health = Health{
		Status: "starting",
	}
	hmipClient   hmip.Client
	influxClient influx.Client
	measurements []hmip.ClimateMeasurement
)

type Health struct {
	Error  error
	Status string
}

func main() {
	hmipClient, health.Error = hmip.NewClient()
	if health.Error != nil {
		log.Fatalf("Error connecting to the HomematicIP Cloud: %v\n", health.Error)
	} else {
		log.Printf("Successfully connected to the HomematicIP Cloud\n")
	}

	influxClient, health.Error = influx.NewClient()
	if health.Error != nil {
		log.Fatalf("Error connecting to the InfluxDB: %v\n", health.Error)
	} else {
		log.Printf("Successfully connected to the InfluxDB\n")
	}

	rate, err := strconv.Atoi(util.ReadEnvVarWithDefault("PROCESS_INTERVAL", "10"))
	if err != nil {
		log.Fatalf("Error reading process interval: %v\n", err)
	} else {
		log.Printf("Processing every %d mionutes\n", rate)
	}

	ticker := time.NewTicker(time.Minute * time.Duration(rate))
	done := make(chan bool)
	go func() {
		for {
			select {
			case <-done:
				return
			case t := <-ticker.C:
				process(t)
			}
		}
	}()
	port := 8080
	mux := http.NewServeMux()
	mux.HandleFunc("/health", handleHealth)
	mux.HandleFunc("/data", handleData)
	mux.HandleFunc("/", handle404)
	fmt.Printf("Started HTTP server (Port: %d)\n", port)
	health.Status = "ok"
	err = http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
	if err != nil {
		log.Fatalf("Error starting HTTP server: %v\n", health.Error)
	}
}

func handle404(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusNotFound)
	_, _ = fmt.Fprintf(w, "%s not found.", r.URL.Path)
}

func handleHealth(w http.ResponseWriter, r *http.Request) {
	if health.Error == nil {
		w.WriteHeader(http.StatusOK)
		_, _ = fmt.Fprint(w, health.Status)
	} else {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "%v", health.Error)
	}
}

func handleData(w http.ResponseWriter, r *http.Request) {
	var data []byte
	data, health.Error = json.Marshal(measurements)
	if health.Error == nil {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = fmt.Fprint(w, string(data))
	} else {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling measurements: %v", health.Error)
	}
}

func process(time time.Time) {
	measurements, health.Error = hmipClient.ReadMeasurements()
	if health.Error != nil {
		fmt.Printf("Error reading measurements from the HomematicIP Cloud: %v", health.Error)
	} else {
		health.Error = influxClient.SaveMeasurements(measurements)
		if health.Error != nil {
			fmt.Printf("Error saving measurements to the InfluxDB: %v", health.Error)
		}
	}
}
