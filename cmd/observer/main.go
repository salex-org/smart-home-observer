package main

import (
	"context"
	"github.com/salex-org/smart-home-observer/internal/data"
	"github.com/salex-org/smart-home-observer/internal/hmip"
	"github.com/salex-org/smart-home-observer/internal/influx"
	"github.com/salex-org/smart-home-observer/internal/photo"
	"github.com/salex-org/smart-home-observer/internal/webserver"
	"github.com/salex-org/smart-home-observer/internal/wordpress"
	"log"
	"os"
	"os/signal"
	"sync"
	"syscall"
	"time"
)

var (
	hmipClient        hmip.Client
	influxClient      influx.Client
	wordpressClient   wordpress.Client
	wordpressRenderer wordpress.Renderer
	webServer         webserver.Server
	photographer      photo.Photographer
	measurementCache  data.MeasurementCache
)

func main() {
	// Startup function
	err := startup()
	if err != nil {
		log.Fatalf("Error during startup: %v", err)
	}

	// Notification context for reacting on process termination - used by shutdown function
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// Waiting group used to await finishing the shutdown process when stopping
	var wait sync.WaitGroup

	// Loop function for photographer
	wait.Add(1)
	go func() {
		defer wait.Done()
		_ = photographer.Start()
	}()

	// Loop function for measuring
	wait.Add(1)
	go func() {
		defer wait.Done()
		_ = hmipClient.Start(handleClimateMeasurements, handleConsumptionMeasurements)
	}()

	// Loop function for webserver
	wait.Add(1)
	go func() {
		defer wait.Done()
		_ = webServer.Start()
	}()

	// Shutdown function waiting for the SIGTERM notification to start the shutdown process
	wait.Add(1)
	go func() {
		defer wait.Done()
		<-ctx.Done()
		log.Printf("\n\U0001F6D1 Shutdown started\n")
		shutdown()
	}()

	// Wait for all functions to end
	wait.Wait()
	log.Printf("\U0001F3C1 Shutdown finished\n")
	os.Exit(0)
}

func handleClimateMeasurements(climateMeasurements []data.ClimateMeasurement) error {
	updatedMeasurements := measurementCache.UpdateClimateMeasurements(climateMeasurements)
	err := influxClient.SaveClimateMeasurements(updatedMeasurements)
	if err != nil {
		return err
	}
	return updateBlog()
}

func handleConsumptionMeasurements(consumptionMeasurements []data.ConsumptionMeasurement) error {
	updatedMeasurements := measurementCache.UpdateConsumptionMeasurements(consumptionMeasurements)
	return influxClient.SaveConsumptionMeasurements(updatedMeasurements)
}

func updateBlog() error {
	post, err := wordpressClient.GetPost(wordpress.OverviewID, wordpress.OverviewType)
	if err != nil {
		return err
	}
	post.Content.Rendered, err = wordpressRenderer.RenderOverview(measurementCache.GetClimateMeasurementsBySensors(([]string{"Maschinenraum", "Bankraum"})))
	if err != nil {
		return err
	}
	return wordpressClient.UpdatePost(post)
}

func startup() error {
	measurementCache = data.NewMeasurementCache()

	webServer = webserver.NewServer(healthCheck, &measurementCache)

	photographer = photo.NewPhotographer(time.Minute * 10) // TODO make interval configurable

	var err error
	time.Local, err = time.LoadLocation("CET")
	if err != nil {
		return err
	} else {
		log.Printf("Successfully loaded timezone CET\n")
	}

	hmipClient, err = hmip.NewClient()
	if err != nil {
		return err
	} else {
		log.Printf("Successfully connected to the HomematicIP Cloud\n")
	}

	influxClient, err = influx.NewClient()
	if err != nil {
		return err
	} else {
		log.Printf("Successfully connected to the InfluxDB\n")
	}

	wordpressClient = wordpress.NewClient()

	wordpressRenderer, err = wordpress.NewRenderer()
	if err != nil {
		return err
	} else {
		log.Printf("Successfully created wordpress renderer\n")
	}

	return nil
}

func shutdown() {
	_ = photographer.Shutdown()
	_ = hmipClient.Shutdown()
	_ = influxClient.Shutdown()
	_ = webServer.Shutdown()
}

func healthCheck() map[string]error {
	errors := make(map[string]error)
	if err := influxClient.Health(); err != nil {
		errors["InfluxDB Client"] = err
	}
	if err := hmipClient.Health(); err != nil {
		errors["HomematicIP Client"] = err
	}
	if err := wordpressClient.Health(); err != nil {
		errors["WordPress Client"] = err
	}
	if err := photographer.Health(); err != nil {
		errors["Photographer"] = err
	}
	return errors
}
