# Indexing-service

A standalone service which receives messages about changes in pid records, maps them to a common format and ingests them into engines like elasticsearch.

![Visualization of use case structure.](use-case.drawio.svg)


## How to build

Dependencies that are needed to build and are not being downloaded via gradle:

- OpenJDK 11

`./gradlew -Pclean-release build`

## How to start

> TODO This section is a placeholder. It still needs to be written properly.

### Prerequisites

You might want to take a look at testbed4inf, which should make it easy to satisfy those.

- Gemma?
- a RabbitMQ instance
- an elasticsearch instance

### Setup

## More information

## License

See [LICENSE file in this repository](LICENSE).