# Use distroless as minimal base image to package the application
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
ARG TARGETPLATFORM
WORKDIR /
COPY bin/${TARGETPLATFORM}/smart-home-observer .
COPY bin/${TARGETPLATFORM}/healthcheck .
ENTRYPOINT ["/smart-home-observer", "run"]
