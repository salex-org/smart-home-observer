package photo

import (
	"fmt"
	"time"
)

type Photographer interface {
	Start() error
	Shutdown() error
	Health() error
}

func NewPhotographer(interval time.Duration) Photographer {
	return &PhotographerImpl{
		stopChannel:     make(chan bool),
		processingError: nil,
		interval:        interval,
	}
}

type PhotographerImpl struct {
	stopChannel     chan bool
	processingError error
	interval        time.Duration
}

func (p *PhotographerImpl) Start() error {
	fmt.Printf("Photographer starting\n")
	ticker := time.NewTicker(p.interval)
	defer ticker.Stop()
	for {
		select {
		case <-p.stopChannel:
			fmt.Printf("Photographer stopped\n")
			return nil
		case t := <-ticker.C:
			p.processingError = p.takePhoto(t)
		}
	}
}

func (p *PhotographerImpl) Shutdown() error {
	p.stopChannel <- true
	return nil
}

func (p *PhotographerImpl) Health() error {
	return p.processingError
}

func (p *PhotographerImpl) takePhoto(t time.Time) error {
	// TODO implement
	fmt.Printf("Taking photo at %v", t)
	return nil
}
