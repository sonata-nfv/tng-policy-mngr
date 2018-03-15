/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.rules.generation;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public enum Tags {

    DRL_EQUALS_TAG("=="),
    DRL_NOT_EQUALS_TAG("=="),
    DRL_LESS_TAG("<"),
    DRL_LESS_OR_EQUAL_TAG("<="),
    DRL_GREATER_TAG(">"),
    DRL_GREATER_OR_EQUAL_TAG(">="),
    EB_STAR_TAG("<"),
    EB_END_TAG(">"),
    COMMA(","),
    SPACE(" "),
    QUOTE("\""),
    NOT("NOT"),
    AND("AND"),
    OR("OR"),
    LBRACKET("["),
    RBRACKET("]"),
    LPARENTHESIS("("),
    RPARENTHESIS(")"),
    EMPTY("");

    private final String tag;

    private Tags(String _tag) {
        tag = _tag;
    }

    /**
     * Get the actual value of the represented Tag
     *
     * @return String
     */
    public String value() {
        return tag;
    }
}
