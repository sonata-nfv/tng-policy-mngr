/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.GPolicy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;

import java.io.FileInputStream;
import java.util.List;
import java.util.ListIterator;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class GPolicy {

    private static final String current_dir = System.getProperty("user.dir");

    public void validateGpolicyClasses(File dslfile) {
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(dslfile);

            ANTLRInputStream antlrInputStream = new ANTLRInputStream(fis);
            System.out.println("antlrInputStream" + antlrInputStream);
            GPolicyLexer lexer = new GPolicyLexer(antlrInputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GPolicyParser parser = new GPolicyParser(tokens);
            ParseTree tree = parser.policy();
            ParseTreeWalker walker = new ParseTreeWalker();
            GPolicyWalker gpolcywalker = new GPolicyWalker();
            walker.walk(gpolcywalker, tree);
            
           
         

            System.out.println("Entering line 1: " + parser.toString());
            
            System.out.println("Entering line 2: " + parser.action().toString());

            System.out.println("Entering line 3: " + parser.policyrule().getChildCount());
            
            System.out.println("Entering line 4: " + parser.getGrammarFileName());
            
            
            ListIterator<GPolicyParser.WhenpartContext> whenpartlist = parser.policyrule().whenpart().listIterator();
            
           
            
             System.out.println("Entering line 5: " + parser.getNumberOfSyntaxErrors());
            
         
            //gpolcywalker.getPolicyInfo(parser.policy());
        } catch (FileNotFoundException ex) {
            Logger.getLogger(GPolicy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(GPolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
