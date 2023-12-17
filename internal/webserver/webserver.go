package webserver

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/data"
	"net/http"
)

type Server interface {
	Start() error
	Shutdown() error
}

type HealthCheck func() map[string]error

type HealthStatus struct {
	Message string           `json:"status"`
	Errors  map[string]error `json:"errors"`
}

func NewServer(healthCheck HealthCheck, measurementCache *data.MeasurementCache) Server {
	server := ServerImpl{
		healthCheck:      healthCheck,
		measurementCache: measurementCache,
	}
	port := 8080
	mux := http.NewServeMux()
	mux.HandleFunc("/health", server.handleHealth)
	mux.HandleFunc("/data", server.handleData)
	mux.HandleFunc("/", server.handle404)
	server.httpServer = http.Server{
		Addr:    fmt.Sprintf(":%d", port),
		Handler: mux,
	}
	return &server
}

type ServerImpl struct {
	healthCheck      HealthCheck
	measurementCache *data.MeasurementCache
	httpServer       http.Server
}

func (s *ServerImpl) Start() error {
	return s.httpServer.ListenAndServe()
}

func (s *ServerImpl) Shutdown() error {
	err := s.httpServer.Shutdown(context.Background())
	if err != nil {
		return err
	}
	return nil
}

func (s *ServerImpl) handle404(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusNotFound)
	_, _ = fmt.Fprintf(w, "%s not found.", r.URL.Path)
}

func (s *ServerImpl) handleHealth(w http.ResponseWriter, _ *http.Request) {
	status := HealthStatus{
		Errors: s.healthCheck(),
	}
	setMessage(&status)
	response, err := json.Marshal(status)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling health status: %v", err)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	if len(status.Errors) == 0 {
		w.WriteHeader(http.StatusOK)
	} else {
		w.WriteHeader(http.StatusInternalServerError)
	}
	_, _ = fmt.Fprintf(w, "%s", string(response))
}

func (s *ServerImpl) handleData(w http.ResponseWriter, _ *http.Request) {
	jsonData, err := json.Marshal(s.measurementCache)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling measurements: %v", err)
		return
	}
	if err == nil {
		w.Header().Set("Content-Type", "application/json")
		w.WriteHeader(http.StatusOK)
		_, _ = fmt.Fprint(w, string(jsonData))
	} else {
	}
}

func setMessage(status *HealthStatus) {
	if len(status.Errors) == 0 {
		status.Message = "healthy"
	} else {
		status.Message = "unhealthy"
	}
}
