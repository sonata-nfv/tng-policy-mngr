/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.rules.generation;

import java.util.LinkedList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.logging.Logger;
import org.drools.compiler.lang.api.CEDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.AndDescr;
import org.drools.compiler.lang.descr.OrDescr;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class RepositoryUtil {

    private static final Logger logger = Logger.getLogger(RepositoryUtil.class.getName());
    List<String> myParams = new LinkedList<String>();

    public static CEDescrBuilder<RuleDescrBuilder, AndDescr> constructDroolsRule(CEDescrBuilder<RuleDescrBuilder, AndDescr> whendroolrule, Object expressionObject, String logicaloperator) {

        final String TIME_WINDOW_DROOLS = "70s";
        //String  TIME_WINDOW_DROOLS = time_window;

        List<String> myParams = new LinkedList<String>();
        myParams.add(TIME_WINDOW_DROOLS);

        //End of expression
        if (null == expressionObject) {
            //logger.info("i am null here");
            if (logicaloperator == null) {
                return whendroolrule;
            }
            whendroolrule.end();
            return whendroolrule;
        }
        if (expressionObject instanceof JSONObject) {
            //logger.info("i am object: " + expressionObject.toString());
            JSONObject tmpObject = ((JSONObject) expressionObject);
            whendroolrule = constructDroolsRule(whendroolrule, tmpObject.getJSONArray("rules"), tmpObject.getString("condition"));
            expressionObject = null;

        } else if (expressionObject instanceof JSONArray) {

            //logger.info("i am array: " + expressionObject.toString());

            JSONArray tmpArray = ((JSONArray) expressionObject);
            CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, AndDescr> tempAND = null;
            CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, OrDescr> tempOR = null;
            for (int i = 0; i < tmpArray.length(); i++) {

                if (i == 0) {

                    JSONObject jObj = tmpArray.getJSONObject(i);
                    if (logicaloperator.equalsIgnoreCase("AND")) {
                        tempAND = whendroolrule.and();
                    } else if (logicaloperator.equalsIgnoreCase("OR")) {
                        tempOR = whendroolrule.or();
                    }

                    //Multi level case
                    if (jObj.length() == 2) {
                        constructDroolsRule(whendroolrule, jObj.getJSONArray("rules"), jObj.getString("condition"));
                    } //Single level case
                    else {

                        if (null != tempAND) {

                            tempAND = createdrlANDCondition(tempAND, i, jObj);

                        } else {

                            tempOR = createdrlORCondition(tempOR, i, jObj);

                        }

                    }
                } else {

                    JSONObject jObj = tmpArray.getJSONObject(i);

                    if (tempAND == null && tempOR == null) {

                        if (logicaloperator.equalsIgnoreCase("AND")) {
                            tempAND.and();
                        } else if (logicaloperator.equalsIgnoreCase("OR")) {
                            tempOR.or();
                        }

                    }

                    //Multi level case
                    if (jObj.length() == 2) {
                        constructDroolsRule(whendroolrule, jObj.getJSONArray("rules"), jObj.getString("condition"));
                    } //Single level case
                    else {
                        if (null != tempAND) {
                            tempAND = createdrlANDCondition(tempAND, i, jObj);

                        } else {
                            tempOR = createdrlORCondition(tempOR, i, jObj);
                        }

                    }
                }

            }
            expressionObject = null;

        }

        return constructDroolsRule(whendroolrule, expressionObject, logicaloperator);
    }

    private static CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, AndDescr> createdrlANDCondition(CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, AndDescr> tempAND, int i, JSONObject jObj) {

        final String TIME_WINDOW_DROOLS = "70s";
        //String  TIME_WINDOW_DROOLS = time_window;

        List<String> myParams = new LinkedList<String>();
        myParams.add(TIME_WINDOW_DROOLS);

        JSONObject expression = createdrlExpression(jObj);
        if (expression.getString("eventType").equalsIgnoreCase("MonitoredComponent")) {

            tempAND.pattern().id("$tot" + i, true).type(Double.class.getName()).constraint("$tot" + i + " " + expression.getString("operator")).from()
                    .accumulate().source().pattern("MonitoredComponent").id("$m" + i, true).constraint("name== \"" + expression.getString("pattern") + "\" && " + expression.getString("constraint"))
                    .from().entryPoint("MonitoringStream").behavior().type("window", "time").parameters(myParams).end().end().end()
                    .function("average", null, false, "$m" + i + ".getValue()")
                    .end()
                    .end();
        } else if (expression.getString("eventType").equalsIgnoreCase("LogMetric")) {
            tempAND.pattern("LogMetric").constraint("vnf_name== \"" + expression.getString("pattern") + "\" && " + expression.getString("constraint"))
                    .from().entryPoint("MonitoringStream").end().end();
        }

        return tempAND;

    }

    private static CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, OrDescr> createdrlORCondition(CEDescrBuilder<CEDescrBuilder<RuleDescrBuilder, AndDescr>, OrDescr> tempOR, int i, JSONObject jObj) {

        final String TIME_WINDOW_DROOLS = "70s";
        //String  TIME_WINDOW_DROOLS = time_window;

        List<String> myParams = new LinkedList<String>();
        myParams.add(TIME_WINDOW_DROOLS);

        JSONObject expression = createdrlExpression(jObj);
        if (expression.getString("eventType").equalsIgnoreCase("MonitoredComponent")) {
            tempOR.pattern().id("$tot" + i, true).type(Double.class.getName()).constraint("$tot" + i + " " + expression.getString("operator")).from()
                    .accumulate().source().pattern("MonitoredComponent").id("$m" + i, true).constraint("name== \"" + expression.getString("pattern") + "\" && " + expression.getString("constraint"))
                    .from().entryPoint("MonitoringStream").behavior().type("window", "time").parameters(myParams).end().end().end()
                    .function("average", null, false, "$m" + i + ".getValue()")
                    .end()
                    .end();
        } else if (expression.getString("eventType").equalsIgnoreCase("LogMetric")) {
            tempOR.pattern("LogMetric").constraint("vnf_name== \"" + expression.getString("pattern") + "\" && " + expression.getString("constraint"))
                    .from().entryPoint("MonitoringStream").end().end();

        }
        return tempOR;

    }

    private static JSONObject createdrlExpression(JSONObject jsonObject) {

        //String jsonfield = jsonObject.getString("field").replace("rateSlider.", "");
        JSONObject expression = new JSONObject();
        String[] fields = jsonObject.getString("field").split("\\.");

        String component = fields[0];
        //System.out.println("component name" + component);

        String metric = fields[1];
        //System.out.println("metric " + metric);

        String operator = null;
        String conditionoperator = jsonObject.getString("operator");
        if (jsonObject.getString("type").equals("integer") || jsonObject.getString("type").equals("double")) {
            expression.put("eventType", "MonitoredComponent");
            expression.put("constraint", "metric== \"" + metric + "\"");

            if (conditionoperator.equalsIgnoreCase("less")) {
                operator = Tags.DRL_LESS_TAG.value() + (jsonObject.getString("type").equals("double") ? jsonObject.getDouble("value") : String.valueOf(jsonObject.getInt("value")));

            } else if (conditionoperator.equalsIgnoreCase("greater")) {
                operator = Tags.DRL_GREATER_TAG.value() + (jsonObject.getString("type").equals("double") ? jsonObject.getDouble("value") : String.valueOf(jsonObject.getInt("value")));
            } else if (conditionoperator.equalsIgnoreCase("equal")) {
                operator = Tags.DRL_EQUALS_TAG.value() + (jsonObject.getString("type").equals("double") ? jsonObject.getDouble("value") : String.valueOf(jsonObject.getInt("value")));
            }

        } else if (jsonObject.getString("type").equals("string")) {

            expression.put("eventType", "LogMetric");
            expression.put("constraint", "value== \"" + jsonObject.getString("value") + "\"");
            if (conditionoperator.equalsIgnoreCase("equal")) {
                operator = Tags.DRL_EQUALS_TAG.value() + Tags.QUOTE.value() + jsonObject.getString("value") + Tags.QUOTE.value();
            } else if (conditionoperator.equalsIgnoreCase("not_equal")) {
                operator = Tags.DRL_NOT_EQUALS_TAG.value() + Tags.QUOTE.value() + jsonObject.getString("value") + Tags.QUOTE.value();
            }

        }

        expression.put("pattern", component);

        expression.put("operator", operator);
        return expression;
    }
}
