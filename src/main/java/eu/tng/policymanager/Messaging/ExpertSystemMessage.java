package eu.tng.policymanager.Messaging;

import java.io.Serializable;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ExpertSystemMessage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String ggid;
    private String nsid;
    private String cid;
    private String nodeid;
    private String ruleActionType;
    private String action;
    private String confParameter;
    private String value;

    public String getGgid() {
        return ggid;
    }

    public void setGgid(String ggid) {
        this.ggid = ggid;
    }

    public String getCid() {
        return cid;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getConfParameter() {
        return confParameter;
    }

    public void setConfParameter(String confParameter) {
        this.confParameter = confParameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getNodeid() {
        return nodeid;
    }

    public void setNodeid(String nodeid) {
        this.nodeid = nodeid;
    }

    public String getRuleActionType() {
        return ruleActionType;
    }

    public void setRuleActionType(String ruleActionType) {
        this.ruleActionType = ruleActionType;
    }

    public String getNsid() {
        return nsid;
    }

    public void setNsid(String nsid) {
        this.nsid = nsid;
    }

    @Override
    public String toString() {
        return "ExpertSystemMessage: { ggid=\"" + ggid + "\""
                + ",nsid=\"" + nsid + "\""
                + ", cid=" + cid
                + ", nodeid=" + nodeid
                + ", ruleActionType=" + ruleActionType
                + ", action=" + action
                + ", confParameter=" + confParameter
                + ", value=" + value + "\"}";
    }

}
