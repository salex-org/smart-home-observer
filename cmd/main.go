package main

import (
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"net/http"
)

func main() {
	port := 8080
	mux := http.NewServeMux()
	mux.Handle("/hello", &controller.HelloHandler{})
	mux.HandleFunc("/", handle404)
	fmt.Printf("Starting server on port %d", port)
	http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
}

func handle404(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s Hamwanich!", r.URL.Path)
}
