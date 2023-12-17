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
		processingError: nil,
		interval:        interval,
	}
}

type PhotographerImpl struct {
	done            chan bool
	processingError error
	interval        time.Duration
}

func (p *PhotographerImpl) Start() error {
	ticker := time.NewTicker(p.interval)
	p.done = make(chan bool)
	defer ticker.Stop()
	for {
		select {
		case <-p.done:
			return nil
		case t := <-ticker.C:
			p.processingError = p.takePhoto(t)
		}
	}
}

func (p *PhotographerImpl) Shutdown() error {
	if p.done != nil {
		close(p.done)
	}
	return nil
}

func (p *PhotographerImpl) Health() error {
	return p.processingError
}

func (p *PhotographerImpl) takePhoto(t time.Time) error {
	// TODO implement
	fmt.Printf("Would have taken new photo at %v\n", t)
	return nil
}
