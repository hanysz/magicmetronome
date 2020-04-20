prefix = net/hanysz/MM
audio = $(prefix)/audio
sounds = $(prefix)/audio/sounds
images = $(prefix)/images
help = $(prefix)/help

JAVAC= /usr/java/jdk1.5.0_09/bin/javac

PMobjects = $(prefix)/PM.class $(prefix)/MMConstants.class
eventsource = $(events)/*.java
eventobjects := $(patsubst %.java,%.class,$(wildcard $(events)/*.java))

audiosource = $(audio)/*.java
audioobjects := $(patsubst %.java,%.class,$(wildcard $(audio)/*.java))

guisource = $(prefix)/PMgui.java
gui = $(prefix)/PMgui.class $(prefix)/PMgui$$1.class

gui1 = $(prefix)/PMgui.class $(prefix)/PMgui\$$1.class

soundfiles = $(sounds)/*.wav $(sounds)/contents.txt
imagefiles = $(images)/*.gif
helpfiles = $(help)/*.html



PM.jar : $(audioobjects) $(gui) $(soundfiles) $(prefix)/Manifest.txt \
	$(PMobjects) $(imagefiles) $(helpfiles)
	jar cfm PM.jar $(prefix)/Manifest.txt $(audioobjects) $(gui1) \
	$(PMobjects) $(soundfiles) $(imagefiles) $(helpfiles)

PM : $(prefix)/PM.class

%.class : %.java
	$(JAVAC) $<

.PHONY: doc
doc:
	cd doc ; javadoc net.hanysz.MM net.hanysz.MM.audio

clean :
	rm $(gui1) $(PMobjects) $(audioobjects)

