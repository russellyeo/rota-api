# Rota API

A simple rota management application, written in Scala.

## Installation (macOS)

1. Install Scala and SBT

```shell
brew install coursier/formulas/coursier && cs setup && brew install sbt
```

2. Install dependencies and start the local server:

```shell
sbt run -jvm-debug 9999
```

The API should now be running on `http://localhost:3000`, and the debugger available on `localhost:9999`.

## Tests

```shell
sbt test
```

## Deploy

The application is packaged into a fat JAR using sbt-assembly, then a Docker image is built from this JAR and deployed to a fly.io instance using the fly CLI tool.

## Authentication

This API does not currently support authentication or authorization.