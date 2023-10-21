package util

import (
	"fmt"
	"os"
)

func ReadEnvVar(name string) string {
	value, present := os.LookupEnv(name)
	if !present {
		fmt.Printf("Warning: Environment variable %s not set.\n", name)
	}
	return value
}
