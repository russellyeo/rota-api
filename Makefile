# Package the application and deploy to fly.io
deploy:
	sbt packageApplication && fly deploy