package main

import (
	"context"
	"fmt"
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
	asciiArt := `       _                                  
  ___ | |__  ___  ___ _ ____   _____ _ __ 
 / _ \| '_ \/ __|/ _ \ '__\ \ / / _ \ '__|
| (_) | |_) \__ \  __/ |   \ V /  __/ |   
 \___/|_.__/|___/\___|_|    \_/ \___|_| starting...`
	fmt.Printf("%s\n\n", asciiArt)
	err := startup()
	if err != nil {
		log.Fatalf("Error during startup: %v\n", err)
	}

	// Notification context for reacting on process termination - used by shutdown function
	ctx, stop := signal.NotifyContext(context.Background(), os.Interrupt, syscall.SIGTERM)
	defer stop()

	// Waiting group used to await finishing the shutdown process when stopping
	var wait sync.WaitGroup

	// Loop function for webserver
	wait.Add(1)
	go func() {
		defer wait.Done()
		fmt.Printf("Web server started\n")
		_ = webServer.Start()
	}()

	// Loop function for measuring
	wait.Add(1)
	go func() {
		defer wait.Done()
		fmt.Printf("Measuring started\n")
		_ = hmipClient.Start(handleClimateMeasurements, handleConsumptionMeasurements, handleSwitchStateChanges)
	}()

	// Loop function for photographer
	wait.Add(1)
	go func() {
		defer wait.Done()
		fmt.Printf("Photographer started\n")
		_ = photographer.Start()
	}()

	// Shutdown function waiting for the SIGTERM notification to start the shutdown process
	wait.Add(1)
	go func() {
		defer wait.Done()
		<-ctx.Done()
		fmt.Printf("\n\U0001F6D1 Observer shutting down...\n\n")
		shutdown()
	}()

	// Wait for all functions to end
	wait.Wait()
	fmt.Printf("\n\U0001F3C1 Observer shutdown finished\n")
	os.Exit(0)
}

func handleClimateMeasurements(climateMeasurements []data.ClimateMeasurement) error {
	updatedMeasurements := measurementCache.UpdateClimateMeasurements(climateMeasurements)
	if len(updatedMeasurements) > 0 {
		err := influxClient.SaveClimateMeasurements(updatedMeasurements)
		if err != nil {
			fmt.Printf("Error saving climate measurements in InfluxDB: %v\n", err)
			return err
		}
		fmt.Printf("New climate measurement data received and stored to InfluxDB\n")
		err = updateBlog()
		if err != nil {
			fmt.Printf("Error updating climate data on WordPress Blog: %v\n", err)
			return err
		}
		fmt.Printf("Climate data on WordPress Blog updated\n")
	}
	return nil
}

func handleConsumptionMeasurements(consumptionMeasurements []data.ConsumptionMeasurement) error {
	updatedMeasurements := measurementCache.UpdateConsumptionMeasurements(consumptionMeasurements)
	if len(updatedMeasurements) > 0 {
		err := influxClient.SaveConsumptionMeasurements(updatedMeasurements)
		if err != nil {
			fmt.Printf("Error saving consumption measurements in InfluxDB: %v\n", err)
			return err
		}
		fmt.Printf("New consumption measurement data received and stored to InfluxDB\n")
	}
	return nil
}

func handleSwitchStateChanges(switchStates []data.SwitchState) error {
	updatedSwitchStates := measurementCache.UpdateSwitchStates(switchStates)
	if len(updatedSwitchStates) > 0 {
		err := influxClient.SaveSwitchStates(updatedSwitchStates)
		if err != nil {
			fmt.Printf("Error saving switch states in InfluxDB: %v\n", err)
			return err
		}
		fmt.Printf("New switch states received and stored to InfluxDB\n")
	}
	return nil
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
	var err error
	time.Local, err = time.LoadLocation("CET")
	if err != nil {
		return err
	} else {
		fmt.Printf("Timezone CET loaded\n")
	}

	measurementCache = data.NewMeasurementCache()
	fmt.Printf("Measurement cache created\n")

	webServer = webserver.NewServer(healthCheck, &measurementCache)
	fmt.Printf("Web server created\n")

	photographer = photo.NewPhotographer(time.Minute * 10) // TODO make interval configurable
	fmt.Printf("Photographer created\n")

	hmipClient, err = hmip.NewClient()
	if err != nil {
		return err
	} else {
		fmt.Printf("HomematicIP client created\n")
	}

	wordpressClient = wordpress.NewClient()
	fmt.Printf("WordPress client created\n")

	wordpressRenderer, err = wordpress.NewRenderer()
	if err != nil {
		return err
	} else {
		fmt.Printf("WordPress renderer created\n")
	}

	influxClient, err = influx.NewClient()
	if err != nil {
		return err
	} else {
		fmt.Printf("Connected to the InfluxDB\n")
	}

	return nil
}

func shutdown() {
	err := photographer.Shutdown()
	if err != nil {
		fmt.Printf("Error stopping photographer: %v\n", err)
	} else {
		fmt.Printf("Photographer stopped\n")
	}

	err = hmipClient.Shutdown()
	if err != nil {
		fmt.Printf("Error stopping measuring: %v\n", err)
	} else {
		fmt.Printf("Measuring stopped\n")
	}

	err = influxClient.Shutdown()
	if err != nil {
		fmt.Printf("Error disconnecting from InfluxDB: %v\n", err)
	} else {
		fmt.Printf("Disconnected from InfluxDB\n")
	}

	err = webServer.Shutdown()
	if err != nil {
		fmt.Printf("Error stopping web server: %v\n", err)
	} else {
		fmt.Printf("Web server stopped\n")
	}
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
