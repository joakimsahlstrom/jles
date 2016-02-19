# jles
A lightweight event store primarily suitable for embedded applications

## Features / usage
### Stores POJOs as events, given a small set of rules
Example event:
```java
public class UserRegisteredEvent {
	private long id;
	private String userName;
	
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
	
	// Event reading is centered around java.lang.Iterable
	Iterable<Object> readEvents = eventStore.readEvents(EventQuery.select(UserRegisteredEvent.class));
	Iterator<Object> events = readEvents.iterator();
	UserRegisteredEvent event = (UserRegisteredEvent) events.next();
	assertEquals(1L, event.getId());
	assertEquals("test", event.getUserName());
	assertFalse(events.hasNext());
}
```

### Supports queries over many events to support complex aggregates
Given that we have this event
```java
public class ChangeUserNameEvent {
	private long id;
	private String userName;
	
	// constructors, getters & setters...
}
```
we can write queries like this
```java
eventStore.write(new UserRegisteredEvent(1L, "test"));
eventStore.write(new ChangeUserNameEvent(1L, "test2"));

Iterator<Object> events = eventStore.readEvents(EventQuery
	.select(UserRegisteredEvent.class)
	.and(ChangeUserNameEvent.class))
	.iterator();
assertEquals("test", ((UserRegisteredEvent) events.next()).getUserName());
assertEquals("test2", ((ChangeUserNameEvent) events.next()).getUserName());
assertFalse(events.hasNext());
```
Event order is guaranteed to remain as they were received by (the first sync point in) jles

### Simple query language for event retrieval
We can add field comparisons to our queries, given the example above it is likely that we would query like this:
```java
Iterable<Object> readEvents = eventStore.readEvents(EventQuery
	// capital I in Id as everything in the method name after get... is matches as is
	.select(UserRegisteredEvent.class).where("Id").is(1L)
	.and(ChangeUserNameEvent.class).where("Id").is(1L));
```

In addition to .is(Object) these comparisons are supported; 
* .in(Object...) 
* .isLessThan(Number) 
* .isGreaterThan(Number)

Field comparison type is not verified. Null values are supported for boxed types and thus .is(null) is supported. 

### Iterators are "live" 
I.e. changes to underlying EventStore are reflected in active iterators
```java
Iterator<Object> events = eventStore.readEvents(EventQuery
	.select(UserRegisteredEvent.class).where("Id").is(1L)
	.and(ChangeUserNameEvent.class).where("Id").is(1L))
	.iterator();
assertEquals("test", ((UserRegisteredEvent) events.next()).getUserName());
assertFalse(events.hasNext()); // no more events

eventStore.write(new ChangeUserNameEvent(1L, "test2"));
assertTrue(events.hasNext()); // more events!
assertEquals("test2", ((ChangeUserNameEvent) events.next()).getUserName());
assertFalse(events.hasNext());
```

### Event indexing gives good overall performance
Events are indexed by type by default, this provides reasonable performance out of the box with minimal disc footprint


### Event fields can be indexed individually for improved performance
```java
EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer()
	.addIndexing(UserRegisteredEvent.class, "Id")
	.addIndexing(ChangeUserNameEvent.class, "Id")
	.configure();

// Iterating over this query will now execute faster
Iterator<Object> events = eventStore.readEvents(EventQuery
	.select(UserRegisteredEvent.class).where("Id").is(1L)
	.and(ChangeUserNameEvent.class).where("Id").is(1L))
	.iterator();
```

Indexing can be added at any time, the index data will then be constructed on event store startup

For high performance, in-memory indexing can be used
```java
EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer()
	.addInMemoryIndexing(UserRegisteredEvent.class, "Id")
	.addInMemoryIndexing(ChangeUserNameEvent.class, "Id")
	.configure();
```

This indexing is loaded eagerly in a background thread at event store startup

### Can run on multiple platforms by providing different FileChannelFactories
Provide custom FileChannelFactory:s to give jles file system access
```java
EventStore eventStore = EventStoreConfigurer.createFileBasedConfigurer(myFileChannelFactory).configure();
```

### Guarantees data integrity by storing event definitions
Whenever jles encounters an event type that is new to it, the definition of the event is then written to file.
These definitions are then loaded the next time the event store is started. If the current class definitions does
not confirm to the definitions that are stored the event store will throw an exception and exit.

### Support event class evolution by convention based serialization support
todo

### Add-only data store reduces error rates
todo

### Can be configured to run in single/multi-threaded environment
Jles expects a multithreaded environment by default, can be configured to expect single threaded environment.
Results in slightly better performance but no protection against multithreading issues.

```java
EventStore eventStore = EventStoreConfigurer.createMemoryOnlyConfigurer().singleThreadedEnvironment().configure()
```

## Todo
* Support multiple requirements in queries
* Allow events to contain arrays of primitives
