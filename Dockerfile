# Use distroless as minimal base image to package the manager binary
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static:nonroot
ARG TARGETPLATFORM
WORKDIR /
COPY bin/${TARGETPLATFORM}/smart-home-observer .
USER 65532:65532
ENTRYPOINT ["/smart-home-observer"]
