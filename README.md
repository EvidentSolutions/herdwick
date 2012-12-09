# Herdwick

The Herdwick is a breed of domestic sheep native to the Lake District of Cumbria in North West England.
The name "Herdwick" is derived from the Old Norse herdvyck, meaning sheep pasture.

Herdwick is also a library for easily populating databases with dummy data. It analyses database schema
for foreign key references and other constraints to make generation of data as painless as possible.

## Example

Consider the following database schema:

    :::sql
    create table department (
        id serial primary key,
        name varchar(20) not null unique,
    );

    create table employee (
        id serial primary key,
        department_id int not null references department,
        name varchar(50) not null unique
    );

To automatically populate the schema with valid values, you can use the following code:

    :::java
    Populator populator = Populator.forUrlAndCredentials("jdbc:example-url", "login", "password");

    populator.populate("department", 50);
    populator.populate("employee", 10000);

The `populator.populate("department", 50)` call will generate 50 random departments whose names
will be unique and `populator.populate("employee", 10000)` will generate 10000 employees randomly
assigned to those departments.

## Integration with Dalesbred

If you are using [Dalesbred](https://bitbucket.org/evidentsolutions/dalesbred), you can construct a new
populator using your `Database`-instance:

    :::java
    Database db = ...
    Populator populator = new Populator(db);

Like this you can integrate population with your existing transactions.

# Attributions

Image of herdwick used on the website is by [hollidaypics on Flickr](http://www.flickr.com/photos/83025884@N00/505902438)
and is used by [CC BY 2.0](http://creativecommons.org/licenses/by/2.0/).
