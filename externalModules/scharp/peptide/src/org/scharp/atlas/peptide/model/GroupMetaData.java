package org.scharp.atlas.peptide.model;

import org.labkey.api.data.Entity;

import java.sql.Date;

/**
 * Created by IntelliJ IDEA.
 * User: sravani
 * Date: Aug 11, 2008
 * Time: 2:49:26 PM
 * To change this template use File | Settings | File Templates.
 */
public class GroupMetaData extends Entity
{
    private String peptide_group_id;
    private String ptid;
    private String draw_date;
    private String study;
    private Integer visit_no;

    public String getPeptide_group_id()
    {
        return peptide_group_id;
    }

    public void setPeptide_group_id(String peptide_group_id)
    {
        this.peptide_group_id = peptide_group_id;
    }

    public String getPtid()
    {
        return ptid;
    }

    public void setPtid(String ptid)
    {
        this.ptid = ptid;
    }

    public String getDraw_date()
    {
        return draw_date;
    }

    public void setDraw_date(String draw_date)
    {
        this.draw_date = draw_date;
    }

    public String getStudy()
    {
        return study;
    }

    public void setStudy(String study)
    {
        this.study = study;
    }

    public Integer getVisit_no()
    {
        return visit_no;
    }

    public void setVisit_no(Integer visit_no)
    {
        this.visit_no = visit_no;
    }
}
