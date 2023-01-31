package main

import (
	"bufio"
	"fmt"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"log"
	"net/http"
	"os"
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

func main() {
	scanner := bufio.NewScanner(os.Stdin)
	key := ""
	for {
		fmt.Printf("Enter config key: ")
		scanner.Scan()
		key = scanner.Text()
		if len(key) != 0 {
			break
		}
	}
	fmt.Printf("Thank you, using key '%s' for decrypting the configuration...\n", key)

	//data, err := os.ReadFile("/config/observer-config.yaml")
	//if err != nil {
	//	fmt.Println(err)
	//	return
	//}
	//var config Configuration
	//err = yaml.Unmarshal(data, &config)
	//if err != nil {
	//	fmt.Println(err)
	//	return
	//}
	// TODO Decrypting with key using https://astaxie.gitbooks.io/build-web-application-with-golang/content/en/09.6.html

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
