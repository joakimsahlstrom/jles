# jles
Joakims Lightweight Event Store
A lightweight event store suiteable for embedded applications

This framework is very much a work in progress but is currently used for one android app.

## Features / usage
### Stores POJOs as events, given a small set of rules
Example event:
```java
public class UserRegisteredEvent {
	long id;
	String userName;
	
	public UserRegisteredEvent() { // Required by jles
	}
	
	public UserRegisteredEvent(long id, String userName) {
		this.id = id;
		this.userName = userName;
	}
	
	public long getId() { // getter and setter must be present for every field of an event
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getUserName() { // all primitive datatypes and java.lang.String are supported
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
}
```
The boxed versions of the primitive types are also supported, these are allowed to be null.

Events are registered and accessed in jles like this:
```java
@Test
public void writeReadEvent() throws Exception {
	EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().configure(); // Testing config
	eventStore.write(new UserRegisteredEvent(1L, "test"));
	
	Iterable<Object> readEvents = eventStore.readEvents(EventQuery.select(UserRegisteredEvent.class));
	Iterator<Object> events = readEvents.iterator();
	UserRegisteredEvent event = (UserRegisteredEvent) events.next();
	assertEquals(1L, event.getId());
	assertEquals("test", event.getUserName());
	assertFalse(events.hasNext());
}
```

### Simple query language for event retrieval
Multiple event types can be selected

### Supports queries over many events to support complex aggregates
Guarantees event order

### Iterators are "live" i.e. changes to underlying EventStore are reflected in active iterators

### Can run on multiple platforms by providing different FileChannelFactories
Ex: android vs windows

### Event indexing gives good overall performance
Events are indexed by type by default
### Event fields can be indexed individually for improved performance
Can also be indexed in-memory
### Guaranteeing data integrity by storing event definitions
### Support event class evolution by convention based serialization support
### Totally immutable data store reduces error rates
### Can be configured to run in single/multi-threaded environment

## Todo
* Allow events to contain arrays of primitives