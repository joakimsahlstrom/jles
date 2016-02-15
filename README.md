# jles
Joakims Lightweight Event Store
A lightweight event store suiteable for embedded applications

This framework is very much a work in progress but is currently used for one android app.

## Features / usage
### Stores POJOs as events, given a small set of rules
No-args constructor
Getter/Setter
### Event indexing gives good overall performance
Events are indexed by type by default
### Event fields can be indexed individually for improved performance
Can also be indexed in-memory
### Simple query language for event retrieval
Iterator will have "next" items once written to the store
### Supports queries over many events to support complex aggregates
Guarantees event order
### Guaranteeing data integrity by storing event definitions
### Support event class evolution by convention based serialization support
### Totally immutable data store reduces error rates
### Supports testing by providing an in-memory file repository
### Can be configured to run in single/multi-threaded environment

## Todo
* Allow events to contain arrays of primitives