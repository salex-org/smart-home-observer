package main

import (
	"flag"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/config"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"log"
	"net/http"
	"os"
)

var (
	inputArg  = ""
	outputArg = ""
)

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
		processConfig(config.Encrypt)
	case "decrypt-config":
		processConfig(config.Decrypt)
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
	conf, err := config.GetConfiguration()
	if err != nil {
		fmt.Errorf("Error reading configuration: %v", err)
		return
	}
	fmt.Printf("Using configuration - Database user is %s", conf.Database.Username)
	port := 8080
	mux := http.NewServeMux()
	mux.Handle("/hello", &controller.HelloHandler{})
	mux.HandleFunc("/", handle404)
	fmt.Printf("Starting server on port %d\n", port)
	http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
}

func processConfig(processor func([]byte) ([]byte, error)) {
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
	outputErr := os.WriteFile(outputArg, output, 0600)
	if outputErr != nil {
		fmt.Printf("\nError writing output: %v\n", outputErr)
		return
	}
}

func handle404(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s Hamwanich!", r.URL.Path)
	log.Printf("Answering 404 on %s\n", r.URL.Path)
}
