BIN ?= bin

APP ?= smart-home-observer
VERSION ?= 0.0.0

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

.PHONY: build-cet
build-cet: | $(BIN)
	go build -o $(BIN)/cet cmd/config/main.go

.PHONY: build
build: | $(BIN)
	go build -o $(EXECUTABLE) cmd/observer/main.go

.PHONY: run
run:
	go run cmd/observer/main.go

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

.PHONY: docker-run
docker-run: docker-build
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho up --detach

.PHONY: docker-stop
docker-stop:
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho down

.PHONY: docker-remove
docker-remove:
	docker image rm ${IMAGE}
