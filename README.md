# fbiwantedclj

This repository is used to retrieve the data from the FBI most wanted list. 

## Prerequisites
### To run the application on your local environment, you will need
1. [Leiningen][] 2.0.0 or above installed.
2. JDK 1.8(preferred for this implementation) or later
3. An IDE of your choice
4. Postgres 13 or later

[leiningen]: https://github.com/technomancy/leiningen

## Running
This application has two sets of config files - a .config and a .json one

1. Make sure that you have created a database called "db_fbilist" and added the necessary tables.
2. Make sure that the credentials that have been set up on your postgres instance are in the JSON config file as well.
3. Add the path to your JSON config file to the .config file
4. Set up an environmental variable called "fbiservice" on Windows that points to the .config file or just run "export fbiservice=/path/to/config.config" on Linux

To start a web server for the application, run:

    lein ring server-headless 3000

The application should now be available on this port. 
You should get a "Hello World" response once you enter this address (https://localhost:3000/) on your browser.

## Compiling
Simply run

    `lein ring uberjar`

in your terminal and you should have a .jar file compiled. You can either run this as a .jar file, or just run the docker file instead

## License



Copyright © 2025 FBIList
