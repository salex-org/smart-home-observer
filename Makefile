.PHONY: local-bootstrap
local-bootstrap:
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho up --detach

.PHONY: local-teardown
local-teardown:
	docker compose --file ./docker/local/docker-compose.yml --project-name local-sho down
