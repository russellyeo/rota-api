# Rota API

A simple rota management application, written in Scala.

## Installation (macOS)

1. Install Scala and SBT

```sh
brew install coursier/formulas/coursier && cs setup && brew install sbt
```

2. Install dependencies and start the local server:

```sh
sbt run
```

The API should now be running on `http://localhost:3000`

## Tests

```sh
sbt test
```

## Deploy
```
sbt packageApplication && fly deploy
```

## Authentication

This API does not currently support authentication or authorization.