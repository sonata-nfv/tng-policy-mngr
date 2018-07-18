/*
* Copyright (c) 2015 SONATA-NFV, 2017 5GTANGO [, ANY ADDITIONAL AFFILIATION]
* ALL RIGHTS RESERVED.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*
* Neither the name of the SONATA-NFV, 5GTANGO [, ANY ADDITIONAL AFFILIATION]
* nor the names of its contributors may be used to endorse or promote
* products derived from this software without specific prior written
* permission.
*
* This work has been performed in the framework of the SONATA project,
* funded by the European Commission under Grant number 671517 through
* the Horizon 2020 and 5G-PPP programmes. The authors would like to
* acknowledge the contributions of their colleagues of the SONATA
* partner consortium (www.sonata-nfv.eu).
*
* This work has been performed in the framework of the 5GTANGO project,
* funded by the European Commission under Grant number 761493 through
* the Horizon 2020 and 5G-PPP programmes. The authors would like to
* acknowledge the contributions of their colleagues of the 5GTANGO
* partner consortium (www.5gtango.eu).
*/
package eu.tng.policymanager.Messaging;

import java.io.Serializable;


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
