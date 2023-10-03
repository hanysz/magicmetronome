*As of October 2023, I am no longer updating this repository.  Check https://hanysz.net/software.html to see if there are any more recent versions.*

# Magic Metronome and Polymetronome

Magic Metronome is a programmable software metronome.  It will play a sequence of sounds according to a script. As well as doing the things you'd expect from a normal metronome, Magic Metronome can also play scripts with changing time signatures, rests and pauses, sudden or gradual tempo changes, and even polyphonic scripts with two or more tracks simultaneously.

Polymetronome is the world's first polyphonic software metronome.  Do you need help learning four-against-five polyrhythms? Or have you always wanted to create your own electronic rendition of Ligeti's "Symphonic Poem for 100 Metronomes"? Polymetronome can do all of this and more!

(Actually I don't know for sure that it's a world first. But I haven't managed to find anything else quite like it. If you do know of a competitor, please tell me about it!)

# Installation

[Download java](https://www.java.com/en/download/) if you don't already have it.  Then go to the [releases page](https://github.com/hanysz/magicmetronome/releases) and download magicmetronome.jar and/or polymetronome.jar.  You should be able to double-click on the downloaded file to run the software.  (You may see some security warnings, depending on how your computer is set up.  You can safely ignore them, assuming you trust me.  Or if you don't trust me, you can read the source code and build the application from source.)

# Build from source

Yes, there's a Makefile.  Don't judge me, I started this project back in 2006 and didn't have a big choice of build tools back then.

Install your favourite Java development kit, then just cd to the the top level directory for your choice of Magic Metronome or Polymetronome, and type "make".  If the previous sentence is gobbledygook to you, then you probably don't want to be building from source.
