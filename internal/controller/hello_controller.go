package controller

import (
	"encoding/json"
	"net/http"
)

type HelloHandler struct{}

type HelloPerson struct {
	FirstName  string `json:"first-name"`
	FamilyName string `json:"family-name"`
}

func (h *HelloHandler) ServeHTTP(w http.ResponseWriter, r *http.Request) {
	person := HelloPerson{
		FirstName:  "Sascha",
		FamilyName: "Gärtner",
	}
	content, err := json.Marshal(person)
	if err != nil {
		w.WriteHeader(http.StatusInternalServerError)
		w.Write([]byte(err.Error()))
	} else {
		w.WriteHeader(http.StatusOK)
		w.Write(content)
	}
}
