# Use distroless as minimal base image to package the manager binary
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static-debian11
ARG TARGETPLATFORM
COPY /bin/${TARGETPLATFORM}/smart-home-observer /
CMD ["/smart-home-observer"]
