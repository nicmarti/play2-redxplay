# RedXPlay

RedXPlay is a sample application I wrote to test and to demonstrate Play2 framework streaming capabilities. 
Basically, it's a good documentation of how to write a realtime web application (RWA) as described by Sadache Drobi, Zenexity CTO.

## How can I test?

First, you'll need a redis server to connect to. Redis (stands for remote dictionary) is a key-value in-memory datastore. It offers advanced data structures, and is very easy to use.

Redis can be installed with one of the following:

- With Homebrew, execute "brew install redis"
- With Linux, "apt-get install redis-server"
- Download, Download, extract and compile Redis with:

    $ wget http://redis.googlecode.com/files/redis-2.4.16.tar.gz
    $ tar xzf redis-2.4.16.tar.gz
    $ cd redis-2.4.16
    $ make

The binaries that are now compiled are available in the src directory. Run Redis with:

    $ src/redis-server

You can interact with Redis using the built-in client:

    $ src/redis-cli
    redis> set jsf c_de_la_merde
    OK
    redis> get jsf
    "c_de_la_merde"

## Install Play2 Framework

Download the Play2 framework from the official play framework web site (http://www.playframework.org/)
Unzip the archive
Add the "play" command to your path
Check that a valid Java Development Kit is also installed.

## Install RedXPlay

Download a version of RedXPlay from Github https://github.com/nicmarti/play2-redxplay/downloads
Extract the archive
Go to play2-redxplay folder
Launch the application with "play run"
Open a web browser and connect to http://localhost:9000

If your redis-server is up and running, you should be able to connect on your local redis server.

Once connected, check the realtime updates sent by the server to the browser. Play2 implements Server sent event, with advanced Enumeratee/Iterator no-blocking I/O.






