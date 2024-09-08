# Package the application and deploy to fly.io production instance
deploy_production:
	sbt packageApplication && fly deploy --config fly.production.toml

# Package the application and deploy to fly.io staging instance
deploy_staging:
	sbt packageApplication
	fly deploy --config fly.staging.toml

# Package the application, build and run as local docker containers
deploy_local:
	supabase start 
	sbt packageApplication
	-docker stop rota-api-local && docker rm rota-api-local
	-docker image rm rota-api-local
	docker build --tag rota-api-local .
	docker run --detach --publish 9000:9000 --env-file .env.local --name rota-api-local rota-api-local

# Stop the local docker containers
stop_local:
	supabase stop
	-docker stop rota-api-local && docker rm rota-api-local
	-docker image rm rota-api-local