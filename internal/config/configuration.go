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
	Database DatabaseConfiguration `yaml:"database"`
	MQTT     MQTTConfiguration     `yaml:"mqtt-broker"`
}

type DatabaseConfiguration struct {
	URL   string `yaml:"url"`
	Token string `yaml:"token"`
}

type MQTTConfiguration struct {
	URL      string `yaml:"url"`
	Username string `yaml:"username"`
	Password string `yaml:"password"`
}

var (
	configuration         Configuration
	readConfigurationOnce sync.Once

	key         []byte
	readKeyOnce sync.Once
)

func GetConfiguration() (*Configuration, error) {
	var err error
	readConfigurationOnce.Do(func() {
		shouldDecrypt := goenv.GetBoolDefault("CONFIG_DECRYPTION_ENABLED", true)
		raw, readErr := readConfiguration("/config/observer-config.yml", shouldDecrypt)
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

func readConfiguration(filename string, shouldDecrypt bool) ([]byte, error) {
	raw, readErr := os.ReadFile(filename)
	if readErr != nil {
		return nil, readErr
	}
	if shouldDecrypt {
		return Decrypt(raw)
	} else {
		return raw, nil
	}
}

func readKey() ([]byte, error) {
	var err error
	readKeyOnce.Do(func() {
		for {
			fmt.Printf("\nEnter config key: ")
			buffer, inputErr := term.ReadPassword(int(os.Stdin.Fd()))
			if inputErr != nil {
				err = inputErr
				break
			} else {
				if len(buffer) > 0 {
					key = buffer
					fmt.Println("\nThank you, please disconnect using 'CTRL-p CTRL-q'.")
					break
				}
			}
		}
	})
	if err != nil {
		return nil, err
	} else {
		return key, nil
	}
}

func Encrypt(plaintext []byte) ([]byte, error) {
	key, err := readKey()
	if err != nil {
		return nil, err
	}

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

func Decrypt(ciphertext []byte) ([]byte, error) {
	key, err := readKey()
	if err != nil {
		return nil, err
	}

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
