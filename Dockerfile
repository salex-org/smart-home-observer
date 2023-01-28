FROM alpine:3.16 as builder
ARG TARGETPLATFORM
WORKDIR /workspace
COPY bin/${TARGETPLATFORM}/smart-home-observer .
RUN chmod +x smart-home-observer

# Use distroless as minimal base image to package the manager binary
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
WORKDIR /
COPY --from=builder /workspace/smart-home-observer .
USER 65532:65532
ENTRYPOINT ["/smart-home-observer"]
