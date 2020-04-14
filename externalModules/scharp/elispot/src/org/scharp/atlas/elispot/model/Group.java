package org.scharp.atlas.elispot.model;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 8, 2007
 * Time: 2:37:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class Group {
    private Integer userid;
    private String name;

    public Group() {
    }

    public Integer getUserid() {
        return userid;
    }

    public void setUserid(Integer userid) {
        this.userid = userid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
