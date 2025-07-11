# Build the manager binary
FROM golang:1.23 AS builder

WORKDIR /workspace

COPY go.mod go.mod
COPY go.sum go.sum
COPY cmd cmd/
COPY internal internal/
RUN CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o observer cmd/main.go
RUN chmod +x observer

# Use distroless as minimal base image to package the application
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
WORKDIR /
COPY --from=builder /workspace/observer .
USER 65532:65532
ENTRYPOINT ["/observer", "run"]
