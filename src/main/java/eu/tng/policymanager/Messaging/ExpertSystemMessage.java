package eu.tng.policymanager.Messaging;



import eu.tng.policymanager.facts.RuleActionType;
import java.io.Serializable;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class ExpertSystemMessage implements Serializable{

    private static final long serialVersionUID = 1L;

    private String ggid;
    private String gname;
    private String cid;
    private String nodeid;
    private RuleActionType ruleActionType;
    private String action;
    private String confParameter;
    private String value;
    private String username;

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

    public RuleActionType getRuleActionType() {
        return ruleActionType;
    }

    public void setRuleActionType(RuleActionType ruleActionType) {
        this.ruleActionType = ruleActionType;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGname() {
        return gname;
    }

    public void setGname(String gname) {
        this.gname = gname;
    }
    
    
    
    @Override
    public String toString() {
        return "ExpertSystemMessage: { ggid=\"" + ggid + "\""
                + ",gname=\"" + gname + "\""
                + ", cid=" + cid
                + ", nodeid=" + nodeid
                + ", ruleActionType=" + ruleActionType
                + ", action=" + action
                + ", confParameter=" + confParameter
                + ", value=" + value                
                + ",username=\"" + username + "\"}";
    }

}
