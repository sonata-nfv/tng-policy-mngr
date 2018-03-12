/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.Gpolicy;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.antlr.v4.runtime.ANTLRInputStream;

import java.io.FileInputStream;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class Gpolicy {

    private static final String current_dir = System.getProperty("user.dir");

    public void validateGpolicyClasses() {
        try {
            FileInputStream fis = null;
            fis = new FileInputStream(current_dir + "/dsl/policy.txt");
            
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(fis);
            System.out.println("antlrInputStream" + antlrInputStream);
            GPolicyLexer lexer = new GPolicyLexer(antlrInputStream);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            GPolicyParser parser = new GPolicyParser(tokens);
            ParseTree tree = parser.policy();
            ParseTreeWalker walker = new ParseTreeWalker();
            walker.walk(new GPolicyWalker(), tree);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Gpolicy.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Gpolicy.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   

}
