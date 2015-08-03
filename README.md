# Information Security Android Porjects
Developed 3 Android apps that contained different kinds of malware. 

- Two apps that work together to extract all the images stored on the victims cellphone. The main idea was to show how apps can require minimum and unsuspicious permisions but, as they comunicate (via a service) with each other, they can 'combine' his permissions allowing us to perform the attack.

- An app that acts like a Parental Control app, but is really a RAT (Remote Access Tool). Using a server we developed, we can control the victims cellphone behaviour. We implemented this functionalities: make the phone vibrate, record the microphone, open the browser in any webpage, get the call history, get the victims GPS location, and execute a binary payload downloaded from our server.

The goal of this project, was to show different ways to exploit Android vulnerabilities (or design choices), and how can users easily fell on traps regarding Android permission system. Once the apps where developed, we wrote a report analysing Android Malware in general, focusing on the users side, and how to make them more aware of this problems.

This project was developed in the context of the Information Security course, dictated by the Computer Science Department, Faculty of Exact and Natural Sciences, University of Buenos Aires.

Authors:  
- [Santiago Alvarez Colombo](https://github.com/santialvarezcolombo)
- [Santiago Torres Batán](https://github.com/sansn) 	
- [Pablo Agustín Artuso](https://github.com/partu18)
