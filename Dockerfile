FROM golang:1.19 as builder

WORKDIR /workspace

COPY go.mod go.mod
COPY go.sum go.sum
COPY cmd/ cmd/
COPY internal/ internal/

RUN CGO_ENABLED=0 GOOS=linux GOARCH=arm GOARM=7 go build -a -o smart-home-observer cmd/main.go

# Use distroless as minimal base image to package the manager binary
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static-debian11
COPY --from=builder /workspace/smart-home-observer /
CMD ["/smart-home-observer"]
