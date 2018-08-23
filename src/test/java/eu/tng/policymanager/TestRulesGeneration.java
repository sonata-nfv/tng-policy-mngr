/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager;

import eu.tng.policymanager.rules.generation.RepositoryUtil;
import java.util.logging.Logger;
import org.drools.compiler.lang.api.CEDescrBuilder;
import org.drools.compiler.lang.api.DescrFactory;
import org.drools.compiler.lang.api.PackageDescrBuilder;
import org.drools.compiler.lang.api.RuleDescrBuilder;
import org.drools.compiler.lang.descr.AndDescr;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
public class TestRulesGeneration {

    private static final Logger logger = Logger.getLogger(RepositoryUtil.class.getName());
    


    @Test
    public void test() throws JSONException {

        JSONObject samplepolicydemocomplex = new JSONObject("{\n"
                + "	\"condition\": \"AND\",\n"
                + "	\"rules\": [{\n"
                + "		\"id\": \"VirtualLinkA.EndToEndDelay\",\n"
                + "		\"field\": \"VirtualLinkA.EndToEndDelay\",\n"
                + "		\"type\": \"double\",\n"
                + "		\"input\": \"number\",\n"
                + "		\"operator\": \"less\",\n"
                + "		\"value\": \"3000\"\n"
                + "	}, {\n"
                + "		\"condition\": \"OR\",\n"
                + "		\"rules\": [{\n"
                + "			\"id\": \"vnf1.CPULoad\",\n"
                + "			\"field\": \"vnf1.CPULoad\",\n"
                + "			\"type\": \"integer\",\n"
                + "			\"input\": \"select\",\n"
                + "			\"operator\": \"greater\",\n"
                + "			\"value\": \"60\"\n"
                + "		}, {\n"
                + "			\"id\": \"vnf2.RAM\",\n"
                + "			\"field\": \"vnf2.RAM\",\n"
                + "			\"type\": \"integer\",\n"
                + "			\"input\": \"select\",\n"
                + "			\"operator\": \"equal\",\n"
                + "			\"value\": \"8\"\n"
                + "		}]\n"
                + "	}]\n"
                + "}");

         //TODO convert yml to drools
        PackageDescrBuilder packageDescrBuilder = DescrFactory.newPackage();
        RuleDescrBuilder droolrule = packageDescrBuilder.newRule().name("test");

        CEDescrBuilder<RuleDescrBuilder, AndDescr> when = droolrule.lhs();

        logger.info(RepositoryUtil.constructDroolsRule(when, samplepolicydemocomplex, "AND").toString());

    }

}
