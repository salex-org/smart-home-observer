# Build the manager binary
FROM golang:1.21 as builder

WORKDIR /workspace

COPY go.mod go.mod
COPY go.sum go.sum
COPY cmd cmd/
COPY internal internal/
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o observer cmd/observer/main.go
RUN chmod +x observer
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o healthcheck cmd/healthcheck/main.go
RUN chmod +x healthcheck

# Use distroless as minimal base image to package the application
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
WORKDIR /
COPY --from=builder /workspace/observer .
COPY --from=builder /workspace/healthcheck .
USER 65532:65532
ENTRYPOINT ["/observer", "run"]
