BIN ?= bin

APP ?= smart-home-observer
VERSION ?= 0.0.0
KIND-CLUSTER ?= salex

IMAGE ?= $(APP):$(VERSION)
ifeq ($(OS),Windows_NT)
	CLEAN_CMD ?= rmdir $(BIN) /s /q
	EXECUTABLE := $(BIN)/$(APP).exe
else
	CLEAN_CMD ?= rm -rf $(BIN)
	EXECUTABLE := $(BIN)/$(APP)
endif

# ===================================
# Build and run local
# ===================================

.PHONY: build
build: | $(BIN)
	go build -o $(EXECUTABLE) cmd/main.go

.PHONY: run
run:
	go run cmd/main.go

.PHONY: test
test:
	go test -v ./...

.PHONY: clean
clean:
	$(CLEAN_CMD)

$(BIN):
	mkdir $(BIN)

# ===================================
# Build and run docker image in local test environment
# ===================================

.PHONY: docker-build
docker-build:
	docker build -f local.Dockerfile -t ${IMAGE} .

.PHONY: docker-remove
docker-remove:
	docker image rm ${IMAGE}

# ===================================
# Deploy to local kind
# ===================================

.PHONY: kind-deploy
kind-deploy: kind-load-image
	helm upgrade -i ${APP} deployments/kubernetes/helm/observer --set "name=${APP},container.image=${IMAGE}" --kube-context kind-${KIND-CLUSTER}

.PHONY: kind-undeploy
kind-undeploy:
	helm delete ${APP} --kube-context kind-${KIND-CLUSTER} --wait
	docker exec -it salex-control-plane crictl rmi ${IMAGE}

.PHONY: kind-load-image
kind-load-image: docker-build
	kind load docker-image ${IMAGE} --name ${KIND-CLUSTER}