# Build the manager binary
FROM golang:1.19 as builder

WORKDIR /workspace

COPY go.mod go.mod
COPY go.sum go.sum
COPY cmd cmd/
COPY internal internal/
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o smart-home-observer cmd/observer/main.go
RUN chmod +x smart-home-observer

# Use distroless as minimal base image to package the application
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
WORKDIR /
COPY --from=builder /workspace/smart-home-observer .
USER 65532:65532
ENTRYPOINT ["/smart-home-observer"]
