package main

import (
	"encoding/json"
	"flag"
	"fmt"
	mqtt "github.com/eclipse/paho.mqtt.golang"
	influxdb2 "github.com/influxdata/influxdb-client-go/v2"
	"github.com/influxdata/influxdb-client-go/v2/api"
	"github.com/salex-org/smart-home-observer/internal/config"
	"github.com/salex-org/smart-home-observer/internal/influx"
	mqtti "github.com/salex-org/smart-home-observer/internal/mqtt"
	"net/http"
	"os"
	"time"
)

var (
	inputArg          = ""
	outputArg         = ""
	consumptionBucket api.WriteAPI
)

type Measurement struct {
	Timestamp time.Time `json:"time"`
	Value     float64   `json:"value"`
}

func main() {
	flag.StringVar(&inputArg, "i", "", "")
	flag.StringVar(&outputArg, "o", "", "")
	flag.Parse()
	if len(flag.Args()) < 1 {
		printUsageAndExit()
	}
	switch flag.Arg(0) {
	case "run":
		runObserver()
	case "encrypt-config":
		processConfig(config.Encrypt, 0600)
	case "decrypt-config":
		processConfig(config.Decrypt, 0660)
	default:
		printUsageAndExit()
	}
}

func printUsageAndExit() {
	fmt.Println("Usage:")
	fmt.Println("\tsmart-home-observer run")
	fmt.Println("\tsmart-home-observer -i <input-file> -o <output-file> encrypt-config")
	fmt.Println("\tsmart-home-observer -i <input-file> -o <output-file> decrypt-config")
	fmt.Println("\nCommands:")
	fmt.Println("\trun\t\tRun the observer in operating mode")
	fmt.Println("\tencrypt-config\t\tEncrypts the config reading from input-file and writing to output-file")
	fmt.Println("\tdecrypt-config\t\tDecrypts the config reading from input-file and writing to output-file")
	fmt.Println("\nOptions:")
	fmt.Println("\t-i\tThe input file for encryption/decryption")
	fmt.Println("\t-o\tThe output file for encryption/decryption")
	os.Exit(1)
}

func runObserver() {
	configuration, confErr := config.GetConfiguration()
	if confErr != nil {
		fmt.Printf("Error reading configuration: %v", confErr)
		return
	}

	db, dbErr := influx.ConnectToInflux()
	if dbErr != nil {
		fmt.Printf("Error connecting to database: %v", dbErr)
		return
	}
	consumptionBucket = db.WriteAPI(configuration.Database.Org, configuration.Database.Buckets.Consumption)
	_, brokerErr := mqtti.ConnectToMQTT(handleOnConnect)
	if brokerErr != nil {
		fmt.Printf("Error connecting to MQTT broker: %v", brokerErr)
		return
	}

	port := 8080
	mux := http.NewServeMux()
	mux.HandleFunc("/", handle404)
	http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
}

var handleOnConnect mqtt.OnConnectHandler = func(client mqtt.Client) {
	configuration, confErr := config.GetConfiguration()
	if confErr != nil {
		fmt.Printf("Error reading configuration: %v", confErr)
		return
	}
	token := client.Subscribe(configuration.MQTT.Topics.Consumption.Electricity, 2, handleConsumptionMessage)
	if token.Wait() && token.Error() != nil {
		fmt.Printf("Error adding MQTT subscriber: %v", token.Error())
	}
}

var handleConsumptionMessage mqtt.MessageHandler = func(client mqtt.Client, message mqtt.Message) {

	var measurement Measurement
	jsonErr := json.Unmarshal(message.Payload(), &measurement)
	if jsonErr != nil {
		fmt.Printf("Error unmarchalling json from MQTT message: %v\n", jsonErr)
		return
	}
	fmt.Printf("Measurement %v received on topic %s\n", measurement, message.Topic())

	point := influxdb2.NewPointWithMeasurement("electricity").
		AddTag("unit", "KWh").
		AddTag("sensor", "main").
		AddField("avg", measurement.Value).
		SetTime(measurement.Timestamp)
	fmt.Printf("Created point from message: %v\n", point)

	consumptionBucket.WritePoint(point)
	consumptionBucket.Flush()
}

func processConfig(processor func([]byte) ([]byte, error), perm os.FileMode) {
	if len(inputArg) == 0 {
		fmt.Printf("\nError: No input filename specified\n")
		return
	}
	if len(outputArg) == 0 {
		fmt.Printf("\nError: No output filename specified\n")
		return
	}
	input, inputErr := os.ReadFile(inputArg)
	if inputErr != nil {
		fmt.Printf("\nError reading input: %v\n", inputErr)
		return
	}
	output, processErr := processor(input)
	if processErr != nil {
		fmt.Printf("\nError processing: %v\n", processErr)
		return
	}
	outputErr := os.WriteFile(outputArg, output, perm)
	if outputErr != nil {
		fmt.Printf("\nError writing output: %v\n", outputErr)
		return
	}
}

func handle404(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s not found.", r.URL.Path)
}
