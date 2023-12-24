package data

type Cache[M Cacheable] interface {
	UpdateEntry(entry M) bool
	GetAllEntries() []M
	GetEntryByID(id string) M
}

func NewCache[M Cacheable]() Cache[M] {
	return &CacheImpl[M]{
		Entries: make(map[string]M),
	}
}

type CacheImpl[M Cacheable] struct {
	Entries map[string]M `json:"entries"`
}

// UpdateEntry updates the given entry in the cache
// returns true, if the given entry is new or has newer status than the previously cached one and false otherwise
func (c *CacheImpl[M]) UpdateEntry(newEntry M) bool {
	oldEntry, exists := c.Entries[newEntry.GetID()]
	if exists {
		if !newEntry.GetTime().After(oldEntry.GetTime()) {
			return false
		}
	}
	c.Entries[newEntry.GetID()] = newEntry
	return true
}

func (c *CacheImpl[M]) GetAllEntries() []M {
	var entries []M
	for _, entry := range c.Entries {
		entries = append(entries, entry)
	}
	return entries
}

func (c *CacheImpl[M]) GetEntryByID(id string) M {
	group, _ := c.Entries[id]
	return group
}
