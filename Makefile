# Package the application and deploy to fly.io production instance
deploy_production:
	sbt packageApplication && fly deploy --config fly.production.toml

# Package the application and deploy to fly.io staging instance
deploy_staging:
	sbt packageApplication && fly deploy --config fly.staging.toml