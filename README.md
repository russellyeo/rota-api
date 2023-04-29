# Rota API

This API provides CRUD (Create, Read, Update, Delete) functionality for managing team rotas. It allows users to create, view, update, and delete rotas for your team.

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

## Authentication

This API does not currently support authentication or authorization.