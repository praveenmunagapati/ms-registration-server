package org.scharp.atlas.specimentracking.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Jan 22, 2007
 * Time: 10:22:01 AM
 * To change this template use File | Settings | File Templates.
 */
public class Sites {
    private  String label;
    private int ldmslabcode;

    public Sites() {
    }

    public Sites(String label, int ldmslabcode) {
        this.label = label;
        this.ldmslabcode = ldmslabcode;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getLdmslabcode() {
        return ldmslabcode;
    }

    public void setLdmslabcode(int ldmslabcode) {
        this.ldmslabcode = ldmslabcode;
    }
}
