package cache

import "github.com/salex-org/hmip-go-client/pkg/hmip"

type Cache[M hmip.Stateful] interface {
	UpdateEntry(entry M) bool
	GetAllEntries() []M
	GetEntryByID(id string) M
}

func NewCache[M hmip.Stateful]() Cache[M] {
	return &cache[M]{
		Entries: make(map[string]M),
	}
}

type cache[M hmip.Stateful] struct {
	Entries map[string]M `json:"entries"`
}

// UpdateEntry updates the given entry in the cache
// returns true, if the given entry is new or has newer status than the previously cached one and false otherwise
func (c *cache[M]) UpdateEntry(newEntry M) bool {
	oldEntry, exists := c.Entries[newEntry.GetID()]
	if exists {
		if !newEntry.GetLastUpdated().After(oldEntry.GetLastUpdated()) {
			return false
		}
	}
	c.Entries[newEntry.GetID()] = newEntry
	return true
}

func (c *cache[M]) GetAllEntries() []M {
	var entries []M
	for _, entry := range c.Entries {
		entries = append(entries, entry)
	}
	return entries
}

func (c *cache[M]) GetEntryByID(id string) M {
	group, _ := c.Entries[id]
	return group
}
