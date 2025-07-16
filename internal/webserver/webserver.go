package webserver

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/salex-org/hmip-go-client/pkg/hmip"
	"github.com/salex-org/smart-home-observer/internal/cache"
	"net/http"
)

type Server interface {
	Start() error
	Shutdown() error
}

type HealthCheck func() map[string]error

type ReadyStatus struct {
	Message string           `json:"status"`
	Errors  map[string]error `json:"errors"`
}

func NewServer(healthCheck HealthCheck, devicesCache cache.Cache[hmip.Device], groupsCache cache.Cache[hmip.Group]) Server {
	server := ServerImpl{
		healthCheck:  healthCheck,
		devicesCache: devicesCache,
		groupsCache:  groupsCache,
	}
	port := 8080
	mux := http.NewServeMux()
	mux.HandleFunc("/alive", server.handleAlive)
	mux.HandleFunc("/ready", server.handleReady)
	mux.HandleFunc("/data", server.handleData)
	mux.HandleFunc("/", server.handle404)
	server.httpServer = http.Server{
		Addr:    fmt.Sprintf(":%d", port),
		Handler: mux,
	}
	return &server
}

type ServerImpl struct {
	healthCheck  HealthCheck
	httpServer   http.Server
	devicesCache cache.Cache[hmip.Device]
	groupsCache  cache.Cache[hmip.Group]
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

func (s *ServerImpl) handleAlive(w http.ResponseWriter, r *http.Request) {
	w.WriteHeader(http.StatusOK)
	_, _ = fmt.Fprint(w, "alive")
}

func (s *ServerImpl) handleReady(w http.ResponseWriter, _ *http.Request) {
	status := ReadyStatus{
		Errors: s.healthCheck(),
	}
	if len(status.Errors) == 0 {
		w.WriteHeader(http.StatusOK)
		status.Message = "ready"
	} else {
		w.WriteHeader(http.StatusInternalServerError)
		status.Message = "not ready"
	}
	response, err := json.Marshal(status)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling ready status: %v", err)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	_, _ = fmt.Fprintf(w, "%s", string(response))
}

func (s *ServerImpl) handleData(w http.ResponseWriter, _ *http.Request) {
	groupData, err := json.Marshal(s.groupsCache)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling groups cache: %v", err)
		return
	}
	deviceData, err := json.Marshal(s.devicesCache)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		_, _ = fmt.Fprintf(w, "Error marshaling devices cache: %v", err)
		return
	}
	w.Header().Set("Content-Type", "application/json")
	w.WriteHeader(http.StatusOK)
	_, _ = fmt.Fprintf(w, "{ \"groups\": %s, \"devices\": %s }", string(groupData), string(deviceData))
}
