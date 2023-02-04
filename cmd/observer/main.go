package main

import (
	"fmt"
	goenv "github.com/caitlinelfring/go-env-default"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"golang.org/x/term"
	"gopkg.in/yaml.v2"
	"log"
	"net/http"
	"os"
)

func main() {
	rawConfig, err := os.ReadFile("/config/observer-config.yml")
	var decryptedConfig []byte
	if err != nil {
		fmt.Println(err)
		return
	}

	decryptConfig := goenv.GetBoolDefault("CONFIG_DECRYPTION_ENABLED", true)

	if decryptConfig {
		var key string
		for {
			fmt.Printf("Enter config key: ")
			buffer, err := term.ReadPassword(int(os.Stdin.Fd()))
			if err == nil {
				key = string(buffer)
				if len(key) != 0 {
					break
				}
			}
		}
		fmt.Println("\nDecrypting configuration...")
		// TODO Decrypting with 'key' using https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/09.6.html
	} else {
		fmt.Println("Decrypting configuration skipped")
		decryptedConfig = rawConfig
	}

	var config Configuration

	err = yaml.Unmarshal(decryptedConfig, &config)
	if err != nil {
		fmt.Println(err)
		return
	}

	port := 8080
	mux := http.NewServeMux()
	mux.Handle("/hello", &controller.HelloHandler{})
	mux.HandleFunc("/", handle404)
	fmt.Printf("Starting server on port %d\n", port)
	http.ListenAndServe(fmt.Sprintf(":%d", port), mux)
}

func handle404(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "%s Hamwanich!", r.URL.Path)
	log.Printf("Answering 404 on %s\n", r.URL.Path)
}
