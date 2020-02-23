sudo dpkg --add-architecture i386
wget -qO - https://dl.winehq.org/wine-builds/winehq.key | sudo apt-key add -

#Use one of the following commands to enable the Wine apt repository in your system based on your operating system and version.

###  Ubuntu 19.10
sudo apt-add-repository 'deb https://dl.winehq.org/wine-builds/ubuntu/ eoan main'

###  Ubuntu 18.04
sudo apt-add-repository 'deb https://dl.winehq.org/wine-builds/ubuntu/ bionic main'
sudo add-apt-repository ppa:cybermax-dexter/sdl2-backport


###  Ubuntu 16.04
sudo apt-add-repository 'deb https://dl.winehq.org/wine-builds/ubuntu/ xenial main'

#Step 2 – Install Wine on Ubuntu
#Use below commands to install Wine packages from the apt repository. The –install-recommends option will install all the recommended packages by winehq stable versions on your Ubuntu system.

sudo apt update
sudo apt install --install-recommends winehq-stable
