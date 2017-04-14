# Pawpads
furry geo locator chat app.
http://www.pawpadsapp.com/


## About Pawpads:
Pawpads is a furry fandom centric geolocation chat application. put simply the app lists out who is near by from closest to farthest and lets you chat with other furries in a one on one setting.

**click here to get pawpads now!**
https://play.google.com/store/apps/details?id=saberapplications.pawpads

### Security:
**I kept secutiy in mind when developing this app**
* By default ALL Quickblox API calls are made over SSL with logins using token authentication post credential verification.
* Locations are by default sent over SSL with accuracy modifications in the medium setting.
* Chats are SSL/TCP based


### Licenced under the MIT licence:
https://en.wikipedia.org/wiki/MIT_License



## Future feature requests:
* Friendslist
* Search for users
* Group chats
* Posting mapped location to chat
* World map with pins where everyone is
* Send to feature (submenu outside of app)
* Filterable results


## Our Patreon:
https://www.patreon.com/user?u=774078
All this development takes time and some of the work is even outsourced to help where information is lacking. this costs money and this project will only exist as long as the community wants it to. our patreon is where you can donate to support the future development. It is with your support that this project continues onward. You can also order ad space through patreon as well. 
**conventions are encouraged to reach out to blazecollie directly as our full page ads are open to Ad swaps.**


### IOS Development:
At this time we are working to develop a xamarin project ported of this project.

### Platforms:
* iOS and windows mobile platforms are currently in volunteer development and staging.
* We will **not** be developing a web platform at this time due to how the location is gathered.


### 3rd party API integrations:
**We will have stickers!** After doing some research I found a decent sticker engine: http://imoji.io/
this allows you to make stickers of any image and even import your previous sticker packs you already commissioned from artists for places like telegram and use them directly in the app. It also has a very large library of stickers for you to use free of charge!

**We will have Gif support!** As it seems to become industry standard we incorporated the Giphy animated gif engine for use. You can search for new animated Gifs through Giphy and upload your own animated Gifs if you want. https://api.giphy.com/


### Our backend and chat system:
**Provided by Quickblox**. A 3rd party API and SDK for quickly implementing a chat service. It is scaleable and secure allow you to have peace of mind when chatting with others



# How to get up and running:

## Edit the following with your own info
#### Util.java
* Quickblox keys
* GCM ID's

#### PawPadsApplication.java
* twitter "Consumer Key (API Key)"
* twitter "Consumer Secret (API Secret)"

