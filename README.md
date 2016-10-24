# Polito_ClassAid

This repo contains the files to implement the ClassAid application on an android tablet.
This project was developed as part of bachelor degree in computer engineering and is not maintained. Retrospectively, today i would have done the entire thing in a different way but it was a learning experience.

The project offers the capability of upgrading static class schedules, that are placed outside of every class in the university, into an intellligent schedule, comprised of a tablet and a smart card reader,.
The tablet staticly displays class schedule and highlights if the class is empty or occupied by activating a Hue Light strip module.
The system decides whether a class is empty or not by crossing schedule information with sensed from a microphone, assumming an occupied class will be noisier.
The system is able to dim or brighten depending if it senses (by camera) someone in proximity.
A student is able to pass his student id card and obtain daily class schedule, along with a map to the next class location.


The Implementation would require to import this project to Android Supporting Eclipse distro or to Android Studio.

The application can run on an android tablet, running build 17 of android or newer (this is very, very old, Ice cream sandwich, Ancient).

To run the application with the current setup, some files should be placed on the target device, those files are in the directory /dependencies.
The important files are Rooms.txt, schedule.txt, students.txt and they should be placed at the root of the device.
These files include sample information about schedule and login information from several years ago.

The application is setup in a way to not load the data from querying the Polito servers, but instead to load the sample the data from these files.
Loading from the servers is done by html scraping of a University website that queries this data and is not guaranteed to function well, in any case, is a long process.

The application will run as expected, but without the card reader functionality,
For this, need to connect both the Target device and a raspberry pi with a very specific card reader.
The pi runs a python code that monitors requests from the card reader and notifies the Target device of someone that passes the card.
The user will then be verified with a database (file) and the relevant user output (personal schedule) will pop up.

For further information (if anyone is interested) post Issues.


