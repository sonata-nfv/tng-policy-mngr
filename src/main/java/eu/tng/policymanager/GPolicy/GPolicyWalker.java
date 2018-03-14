package eu.tng.policymanager.GPolicy;

public class GPolicyWalker extends GPolicyBaseListener {

    public void getPolicyInfo(GPolicyParser.PolicyContext ctx) {
        
        
       System.out.println("Entering line : " + ctx.toString());
    }

    public void enterR(GPolicyParser.PolicyContext ctx) {
        //System.out.println("Entering line : " + ctx.policyname().toString());
    }

    public void exitR(GPolicyParser.PolicyContext ctx) {
        System.out.println("Exiting R");
    }
}
