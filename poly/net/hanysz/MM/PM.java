package net.hanysz.MM;

import net.hanysz.MM.audio.*;
import javax.sound.sampled.*;
import java.io.*;

class PM implements net.hanysz.MM.MMConstants {

    public static void main(String[] args)
    throws InterruptedException {
	if (args.length==0) {
	    PMgui.main(args);
	}
        else {  // later, maybe add some options to handle command line arguments!
            PMgui.main(args);
        }
    } // end main
} // end class
