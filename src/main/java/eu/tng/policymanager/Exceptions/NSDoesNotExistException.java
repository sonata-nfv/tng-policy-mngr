/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.Exceptions;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class NSDoesNotExistException extends Exception {

    // Parameterless Constructor
    public NSDoesNotExistException() {
    }

    // Constructor that accepts a message
    public NSDoesNotExistException(String message) {
        super(message);
    }
}
