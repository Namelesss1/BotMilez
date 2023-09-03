## BotMilez
A Discord bot that has many features related to language, Mario Kart Wii, and random generation mainly made for fun with a small group of friends.

This bot is created using [JDA 5](https://github.com/discord-jda/JDA) which is an API that allows Java interaction with Discord services. All programming is done in Java. Data management and such is stored through text and JSON files.
Eventually, SQL programming will be used for larger-scale input and reading specifically for the training data that will be used by the random message generation command as the bot features become more sophisticated.

## Current Commands
Commands are primarily slash commands, some accepting optional arguments or further input for more customization. 

* Hi Command (`/hi`): The bot responds back with a simple greeting
* Bubble Wrap (`/bubblewrap`): The bot sends a grid of NxN "bubbles" consisting of the word "pop!" surrounded by discord spoiler markdown texts. This command can accept a number as an argument specifying a preferred grid size.
* Help (`/help`): A command that describes the features of each command in detail as well as how to use the commands
* Quote (`/quotes`): A command that allows users to manage funny, interesting, or notable things that have been said by other server members. Adding, viewing, searching for, and removing quotes are supported.
* RNG command (`/rng`): Sends a random number to the user. A customizable range can be specified i.e. a random number between 9-13. This command can also choose one option randomly from a list of strings.
* RNG Message Command (`/rng-message`): Sends a randomly-generated message. This is either generated through training data (like server history) or by other means as chosen by the user.
* RNG Mario Kart Wii Command (`/rng-mkw`): This command randomly selects a character/vehicle combination for use in the game Mario Kart Wii. If desired, a user can randomly generate from a specific subset of characters/vehicles (e.g.: Randomly select from all lightweight bikes)
* RNG Username Command (`/rng-username`): This command randomly generates a plausible-sounding username.
* MKW Stats Command (`/mkwstats`): This command displays all the stats and information relating to every character and vehicle in the game Mario Kart Wii. The user can select which specific character/vehicle stats to display.

## Planned Features
* A music command that allows the bot to play music in a voice channel
* A command that translates a message in an intentionally-bad manner by running it through a translator many times.
* A math command with various options such as simplification, arithmetic solver, exact number from an expression, etc.
* Individual changes and fixes to existing commands based on their issues or room for growth.
* A new sentence generation option in RNG-Message command using syntactic rules instead of training data.
* A more customizable Hi command that allows for custom messages
* A gimmick where the bot changes its username and profile to impersonate other members jokingly with a custom "Message of the day command" (Only in one specfic server)
* Any other features upon the request of friends or that I come up with.

## Notes
This bot is not always online 24/7 given limited hardware resources. The bot is also a major Work-In-Progress currently.

## Discord Invite
Click on this [Invite Link](https://discord.com/api/oauth2/authorize?client_id=1104079527770063000&permissions=277062199872&scope=bot%20applications.commands) and follow the instructions :)
