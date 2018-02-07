/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.facts;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class Action {

    private String gsgid;

    private String nodeid;
    private RuleActionType ruleActionType;
    private String value;
    private String action;

    public Action(String gsgid, String nodeid, RuleActionType ruleActionType, String value, String action) {
        this.gsgid = gsgid;
        this.nodeid = nodeid;
        this.ruleActionType = ruleActionType;
        this.value = value;
        this.action = action;
    }

    public RuleActionType getRuleActionType() {
        return ruleActionType;
    }

    public void setRuleActionType(RuleActionType ruleActionType) {
        this.ruleActionType = ruleActionType;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getGsgid() {
        return gsgid;
    }

    public void setGsgid(String gsgid) {
        this.gsgid = gsgid;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }



    @Override
    public String toString() {
        return "Action: { RuleActionType=\"" + ruleActionType + "\"" + ", value=" + value
                + ", gsgid= \"" + gsgid + "\" , action = \"" + action + "\", nodeid = \"" + nodeid + "\"}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Action that = (Action) o;
        return this.value == that.value && this.ruleActionType.equals(that.ruleActionType);
    }

    @Override
    public int hashCode() {
        int result = ruleActionType.hashCode();
        result = 31 * result + value.hashCode();
        return result;
    }

}
