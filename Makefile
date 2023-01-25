BIN ?= bin

APP ?= smart-home-observer
VERSION ?= 0.0.1

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

.PHONY: clean
clean:
	$(CLEAN_CMD)

$(BIN):
	mkdir $(BIN)

# ===================================
# Build and run docker image
# ===================================

.PHONY: docker-build
docker-build:
	docker build -t ${IMAGE} .

.PHONY: docker-run
docker-run: docker-build
	docker run -d --name ${APP} ${IMAGE}

.PHONY: docker-remove
docker-remove:
	docker stop ${APP}
	docker rm ${APP}
	docker image rm ${IMAGE}

# =====================================
# Start and stop local test environment
# =====================================

.PHONY: local-bootstrap
local-bootstrap:
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho up --detach

.PHONY: local-teardown
local-teardown:
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho down
