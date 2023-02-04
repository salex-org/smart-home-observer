package config

import (
	"crypto/aes"
	"crypto/cipher"
	"crypto/rand"
	"errors"
	"fmt"
	goenv "github.com/caitlinelfring/go-env-default"
	"golang.org/x/term"
	"gopkg.in/yaml.v3"
	"io"
	"os"
	"sync"
)

type Configuration struct {
	database DatabaseConfiguration `yaml:"database"`
	mqtt     MQTTConfiguration     `yaml:"mqtt-broker"`
}

type DatabaseConfiguration struct {
	username string `yaml:"username"`
	password string `yaml:"password"`
}

type MQTTConfiguration struct {
	hostname string `yaml:"hostname"`
	username string `yaml:"username"`
	password string `yaml:"password"`
}

var (
	configuration Configuration
	once          sync.Once
)

func GetConfiguration() (*Configuration, error) {
	var err error
	once.Do(func() {
		shouldDecrypt := goenv.GetBoolDefault("CONFIG_DECRYPTION_ENABLED", true)
		raw, readErr := ReadConfiguration("/config/observer-config.yml", shouldDecrypt)
		if readErr != nil {
			err = readErr
		} else {
			err = yaml.Unmarshal(raw, &configuration)
		}
	})
	if err != nil {
		return nil, err
	} else {
		return &configuration, nil
	}
}

func ReadConfiguration(filename string, shouldDecrypt bool) ([]byte, error) {
	raw, readErr := os.ReadFile(filename)
	if readErr != nil {
		return nil, readErr
	}
	if shouldDecrypt {
		key, keyErr := readKey()
		if keyErr != nil {
			return nil, keyErr
		}
		return decrypt(raw, key)
	} else {
		return raw, nil
	}
}

func WriteConfiguration() {
	// TODO implement
}

func readKey() ([]byte, error) {
	// TODO use sync.Once and cache the key
	var key string
	for {
		fmt.Printf("\nEnter config key: ")
		buffer, err := term.ReadPassword(int(os.Stdin.Fd()))
		if err != nil {
			return nil, err
		} else {
			key = string(buffer)
			if len(key) > 0 {
				break
			}
		}
	}
	return []byte(key), nil
}

func encrypt(plaintext []byte, key []byte) ([]byte, error) {
	c, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	gcm, err := cipher.NewGCM(c)
	if err != nil {
		return nil, err
	}

	nonce := make([]byte, gcm.NonceSize())
	if _, err = io.ReadFull(rand.Reader, nonce); err != nil {
		return nil, err
	}

	return gcm.Seal(nonce, nonce, plaintext, nil), nil
}

func decrypt(ciphertext []byte, key []byte) ([]byte, error) {
	c, err := aes.NewCipher(key)
	if err != nil {
		return nil, err
	}

	gcm, err := cipher.NewGCM(c)
	if err != nil {
		return nil, err
	}

	nonceSize := gcm.NonceSize()
	if len(ciphertext) < nonceSize {
		return nil, errors.New("ciphertext too short")
	}

	nonce, ciphertext := ciphertext[:nonceSize], ciphertext[nonceSize:]
	return gcm.Open(nil, nonce, ciphertext, nil)
}
