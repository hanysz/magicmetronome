package net.hanysz.MM.misc;

// Thanks to http://blog.codebeach.com/2007/06/adding-file-filters-to-jfilechooser.html for this!

import javax.swing.*;
import java.io.*;

public class TextFileFilter extends javax.swing.filechooser.FileFilter {
     public boolean accept(File file)
     {
          //Convert to lower case before checking extension
         return (file.getName().toLowerCase().endsWith(".txt")  ||
            file.isDirectory());
    }

    public String getDescription()
    {
        return "Text File (*.txt)";
    }
}
