/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tng.policymanager.response;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 */
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Eleni Fotopoulou <efotopoulou@ubitech.eu>
 * @param <C>
 * @param <R>
 */
public class PolicyRestResponse<C extends Enum<? extends ResponseCode>, R> {

    private C code;
    private String message;
    private R returnobject;

    public PolicyRestResponse(C code, String message, R returnobject) {
        this.message = message;
        this.code = code;
        this.returnobject = returnobject;
    }

    public PolicyRestResponse() {

    }

    public C getCode() {
        return code;
    }

    public void setCode(C code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public R getReturnobject() {
        return returnobject;
    }

    public void setReturnobject(R returnobject) {
        this.returnobject = returnobject;
    }

    public JSONObject buildJSONObjectResponse(C code, String message, String returnobject) {

        JSONObject response = new JSONObject();

        response.put("code", code);
        response.put("message", message);
        response.put("returnobject", new JSONObject(returnobject));

        return response;

    }

    public JSONObject buildJSONArrayResponse(C code, String message, String returnobject) {

        JSONObject response = new JSONObject();

        response.put("code", code);
        response.put("message", message);
        response.put("returnobject", new JSONArray(returnobject));

        return response;

    }

}