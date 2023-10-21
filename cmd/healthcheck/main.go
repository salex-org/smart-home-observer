package main

import (
	"log"
	"net/http"
)

func main() {
	response, err := http.Get("http://localhost:8080/health")
	if err != nil {
		log.Fatalf("Error during health check: %v\n", err)
	}
	if response.StatusCode != http.StatusOK {
		log.Fatalf("Observer is unhealthy\n")
	}
}
