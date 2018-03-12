package eu.tng.policymanager.Gpolicy;

public class GPolicyWalker extends GPolicyBaseListener {
    public void enterR(GPolicyParser.PolicyContext ctx) {
        //System.out.println("Entering line : " + ctx.policyname().toString());
    }

    public void exitR(GPolicyParser.PolicyContext ctx) {
        System.out.println("Exiting R");
    }
}