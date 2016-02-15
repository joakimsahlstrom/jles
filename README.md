# jles
Joakims Lightweight Event Store
A lightweight event store suiteable for embedded applications

This framework is very much a work in progress but is currently used for one android app.

## Features / usage
### Stores POJOs as events, given a small set of rules
### Event indexing gives good overall performance
### Event fields can be indexed individually to improve performance for time consuming queries
### (Simple) Query language for event retrieval
### Supports queries over many events to support complex aggregates
### Guaranteeing data integrity by storing event definitions
### Support event evolution be convention based serialization support
### Totally immutable data store reduces error rates
### Supports testing by providing an in-memory file repository

### Can be configured to run in single/multi-threaded environment

## Todo
* Allow events to contain arrays of primitives