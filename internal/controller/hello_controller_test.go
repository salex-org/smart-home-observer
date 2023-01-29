package controller_test

import (
	. "github.com/onsi/ginkgo/v2"
	. "github.com/onsi/gomega"
	"github.com/salex-org/smart-home-observer/internal/controller"
	"net/http"
	"net/http/httptest"
)

var _ = Describe("HelloController", func() {
	Context("Handler", func() {
		It("should serve hello persons data", func() {
			handler := &controller.HelloHandler{}
			request, err := http.NewRequest(http.MethodGet, "/hello", nil)
			Expect(err).ShouldNot(HaveOccurred())
			recorder := httptest.NewRecorder()
			handler.ServeHTTP(recorder, request)
			Expect(recorder.Code).To(Equal(http.StatusOK))
			Expect(recorder.Body.String()).To(Equal("{\"first-name\":\"Sascha\",\"family-name\":\"Gäärtner\"}"))
		})
	})
})
