prefix = net/hanysz/MM
events = $(prefix)/events
audio = $(prefix)/audio
misc = $(prefix)/misc
parser = $(prefix)/parser
sounds = $(prefix)/audio/sounds
examples = $(prefix)/examples
help = $(prefix)/help
messages = $(prefix)/messages

#JAVAC= /usr/java/jdk1.5.0_09/bin/javac
# Let's try with a newer compiler!
JAVAC= /usr/bin/javac

MMobjects = $(prefix)/MM.class $(prefix)/MMConstants.class
eventsource = $(events)/*.java
eventobjects := $(patsubst %.java,%.class,$(wildcard $(events)/*.java))

audiosource = $(audio)/*.java
audioobjects := $(patsubst %.java,%.class,$(wildcard $(audio)/*.java))

miscsource = $(misc)/*.java
miscobjects := $(patsubst %.java,%.class,$(wildcard $(misc)/*.java))

parsersource = $(parser)/MMScriptParserConstants.java \
	$(parser)/MMScriptParser.java \
	$(parser)/MMScriptParserTokenManager.java \
	$(parser)/ParseException.java \
	$(parser)/SimpleCharStream.java \
	$(parser)/Token.java \
	$(parser)/TokenMgrError.java

parsersourcenames = MMScriptParserConstants.java \
	MMScriptParser.java \
	MMScriptParserTokenManager.java \
	ParseException.java \
	SimpleCharStream.java \
	Token.java \
	TokenMgrError.java

parserobjects = $(parser)/MMScriptParser.class \
	$(parser)/MMScriptParserConstants.class \
	$(parser)/MMScriptParserTokenManager.class \
	$(parser)/ParseException.class \
	$(parser)/SimpleCharStream.class \
	$(parser)/Token.class \
	$(parser)/TokenMgrError.class \
	$(parser)/SyntaxChecker.class \
	$(parser)/SyntaxException.class

guisource = $(prefix)/MMgui.java
gui = $(prefix)/MMgui.class $(prefix)/MMgui$$1.class $(prefix)/MMgui$$2.class

gui1 = $(prefix)/MMgui.class $(prefix)/MMgui\$$1.class $(prefix)/MMgui\$$2.class

soundfiles = $(sounds)/*.wav $(sounds)/contents.txt
helpfiles = $(help)/*.html
examplefiles = $(examples)/*
messagetext = $(messages)/*


MM.jar : $(audioobjects) $(gui) $(soundfiles) $(prefix)/Manifest.txt \
	parser $(MMobjects) $(eventobjects) $(parser)/MMPreProcessor.class \
	$(miscobjects) $(helpfiles) $(examplefiles) $(messagetext)
	jar cfm MM.jar $(prefix)/Manifest.txt $(miscobjects) $(audioobjects) $(gui1) \
	$(MMobjects) $(soundfiles) $(eventobjects) $(parserobjects) \
	$(parser)/MMPreProcessor.class $(messagetext) $(helpfiles) $(examplefiles)

MM : $(prefix)/MM.class

%.class : %.java
	$(JAVAC) $<

events: $(eventsource) $(eventobjects)
	$(JAVAC) $(eventsource)

script: $(audio)/ScriptPlayer.class

$(parser)/MMScriptParser.class: $(parser)/mmparser.jj
	rm $(parsersource)
	cd $(parser) ; javacc mmparser.jj ; \
	for x in $(parsersourcenames) ; do mv $$x tempfile; \
	cat package_declaration.txt tempfile > $$x; done; \
	rm tempfile
	$(JAVAC) $(parser)/MMScriptParser.java

# parser : $(parser)/MMScriptParser.class $(prefix)/ParserTest.java 
#	$(JAVAC) $(prefix)/ParserTest.java

parser : $(parser)/MMScriptParser.class $(parser)/MMPreProcessor.class

.PHONY: doc
doc:
	cd doc ; javadoc -source 1.5.0 -sourcepath .. net.hanysz.MM net.hanysz.MM.audio \
	net.hanysz.MM.events net.hanysz.MM.parser

clean :
	rm $(MMobjects) $(gui1) $(eventobjects) $(parserobjects) \
	 $(miscobjects) $(audioobjects) $(parser)/MMPreProcessor.class $(prefix)/MMgui$$2.class

