# Use distroless as minimal base image to package the manager binary
# Refer to https://github.com/GoogleContainerTools/distroless for more details
FROM gcr.io/distroless/static-debian11
ARG TARGETPLATFORM
ADD /bin/${TARGETPLATFORM} /
CMD ["/smart-home-observer"]
