# Efteling simulator
Repo to learn all kinds of development. Set up in a domain driven way. **Note that this has no relationship with Efteling itself, this is a project where the theme park is used as the domain to work with.**

# How to run locally

The project consists of two parts: The front-end and the back-end. So those need to be started separately

## Start and stop back-end

Requirements: Docker and Java SDK (17)

- In a console, go to the root directory of the project (where `docker-compose.yml` is included)
- Run `docker compose build`
- Run `docker compose up`

To stop, run `docker compose down`

## Start and stop front-end

Requirements: NodeJS

- In a console, go to the folder called `efteling-frontend`
- Run `npm install`
- Run `npm start`

The React application will start now and should automatically open the browser. If not, the link should be `http://localhost:3000/`. It could take a minute when it is starting.

To stop, just press `control+c`

# Issues and possible future work

* There is no path finding implemented yet, so the visitors will walk into a straight line. I am thinking about implementing some path finding.
* The different location coordinates should be owned by the park, so that the park decides where it is located. Rides, fairy tales and stands register at the park and are then positioned.
* Performance of front end is still too slow when there are many visitors walking around. So now I've limited it to 2000 visitors, but that is already a bit slow. Need to find a way to change this.
* I was thinking that some rides have separate carts. Now it is just implemented as that a ride have a maximum amount of visitors per "round" and the round takes an amount of time. This should go better
* Started with an implementation of the employees, but didn't finish it yet.