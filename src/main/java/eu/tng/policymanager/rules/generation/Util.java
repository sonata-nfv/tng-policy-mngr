/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.rules.generation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import eu.tng.policymanager.Messaging.DeployedNSListener;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
@Component
public class Util {

    public static String convertYamlToJson(String yaml) {
        try {
            ObjectMapper yamlReader = new ObjectMapper(new YAMLFactory());
            Object obj = yamlReader.readValue(yaml, Object.class);

            ObjectMapper jsonWriter = new ObjectMapper();
            return jsonWriter.writeValueAsString(obj);
        } catch (IOException ex) {
            Logger.getLogger(DeployedNSListener.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }

    public static String jsonToYaml(JSONObject jsonobject) {
        Yaml yaml = new Yaml();

        // get json string
        String prettyJSONString = jsonobject.toString(4);
        // mapping
        Map<String, Object> map = (Map<String, Object>) yaml.load(prettyJSONString);
        // convert to yaml string (yaml formatted string)
        String output = yaml.dump(map);
        //logger.info(output);
        return output;
    }

}
