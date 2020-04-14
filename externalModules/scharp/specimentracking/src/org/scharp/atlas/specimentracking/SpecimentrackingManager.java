package org.scharp.atlas.specimentracking;

import org.apache.commons.beanutils.ConversionException;
import org.apache.log4j.Logger;
import org.labkey.api.data.Container;
import org.labkey.api.data.Filter;
import org.labkey.api.data.SimpleFilter;
import org.labkey.api.data.Sort;
import org.labkey.api.data.SqlSelector;
import org.labkey.api.data.Table;
import org.labkey.api.data.TableSelector;
import org.labkey.api.data.dialect.SqlDialect;
import org.labkey.api.query.FieldKey;
import org.labkey.api.security.User;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.DateUtil;
import org.scharp.atlas.specimentracking.model.ManifestSpecimens;
import org.scharp.atlas.specimentracking.model.Manifests;
import org.scharp.atlas.specimentracking.model.Sites;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class SpecimentrackingManager
{
    private static SpecimentrackingManager _instance;
    static Logger _log = Logger.getLogger(SpecimentrackingManager.class);
    private SpecimentrackingManager()
    {
        // prevent external construction with a private default constructor
    }

    public static synchronized SpecimentrackingManager getInstance()
    {
        if (_instance == null)
            _instance = new SpecimentrackingManager();
        return _instance;
    }

    public void deleteAllData(Container c) throws SQLException
    {
        Filter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), c.getId());

        Table.delete(SpecimentrackingSchema.getInstance().getTableInfoManifests(), containerFilter);
    }


    public ManifestSpecimens[] getManifestSpecimens(Container c, String mId)
    {
        SimpleFilter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), c.getId());
        containerFilter.addCondition(FieldKey.fromParts("ShipId"), mId);
        containerFilter.addCondition(FieldKey.fromParts("Reconciled"), false);

        return getManifestSpecimens(containerFilter);
    }

    public ResultSet getmSpecimens(Container c, String mId)
    {
        SimpleFilter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), c.getId());
        containerFilter.addCondition(FieldKey.fromParts("ShipId"), mId);
        return new TableSelector(SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens(),
                containerFilter, new Sort("RowId")).getResultSet();
    }

    private ManifestSpecimens[] getManifestSpecimens(Filter filter)
    {
        return new TableSelector(SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens(), filter, new Sort("RowId")).getArray(ManifestSpecimens.class);
    }

    public ManifestSpecimens getManifestSpecimen(Container c, String mId, String sId)
    {
        SqlDialect dialect = SpecimentrackingSchema.getInstance().getSqlDialect();
        SimpleFilter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), c.getId());
        containerFilter.addCondition(FieldKey.fromParts("ShipId"), mId);
        containerFilter.addCondition(FieldKey.fromParts("SpecimenId"), sId);

        return new TableSelector(SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens(),  containerFilter, null).getArray(ManifestSpecimens.class)[0];
    }

    public Manifests[] getManifests(Container c, User user)
    {
        SimpleFilter containerFilter = new SimpleFilter(FieldKey.fromParts("Container"), c.getId());
        if(!(c.hasPermission(user, AdminPermission.class)))
        {
            containerFilter.addCondition(FieldKey.fromParts("CreatedBy"), user.getUserId());
        }
        return getManifests(containerFilter);
    }

    private Manifests[] getManifests(Filter filter)
    {
        return new TableSelector(SpecimentrackingSchema.getInstance().getTableInfoManifests(), filter, new Sort("RowId")).getArray(Manifests.class);
    }

    public Manifests insertManifest(Container c, User user, Manifests manifests) throws SQLException
    {
        manifests.setContainer(c.getId());
        if(manifestExists(manifests.getShipId(),c))
        {
            return null;
        }
        else
        {
            return Table.insert(user,SpecimentrackingSchema.getInstance().getTableInfoManifests(),manifests);
        }
    }

    public static boolean specimenExists(String specimenId, Container c)
    {
        ManifestSpecimens m = getmanifestSpecimen(specimenId,c);
        return (null != m);
    }

    public static ManifestSpecimens getmanifestSpecimen(String specimenId, Container c)
    {
        ManifestSpecimens m = null;

        ManifestSpecimens[] specimens = new SqlSelector(SpecimentrackingSchema.getInstance().getSchema(), "SELECT * FROM " +
                SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens() + " WHERE SpecimenId=? and Container = ?", new Object[]{specimenId,c.getId()}).getArray(ManifestSpecimens.class);

        if (0 < specimens.length)
            m = specimens[0];

        return m;
    }

    public boolean manifestExists(String shipId, Container c)
    {
        Manifests m = getManifest(shipId,c);
        return (null != m);
    }
    public Manifests getManifest(String shipId, Container c)
    {
        Manifests m = null;

        Manifests[] manifests = new SqlSelector(SpecimentrackingSchema.getInstance().getSchema(), "SELECT * FROM " +
                SpecimentrackingSchema.getInstance().getTableInfoManifests() + " WHERE ShipId=? and Container = ?", new Object[]{shipId,c.getId()}).getArray(Manifests.class);

        if (0 < manifests.length)
            m = manifests[0];

        return m;
    }

    public ManifestSpecimens insertManifestSpecimen(Container c, User user, ManifestSpecimens mSpecimens) throws SQLException
    {
        mSpecimens.setContainer(c.getId());
        if(specimenExists(mSpecimens.getSpecimenId(),c))
        {
            return null;
        }
        else
        {
            return Table.insert(user, SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens(), mSpecimens);
        }
    }

    public ManifestSpecimens updateManifestSpecimen(Container c, User user, ManifestSpecimens mSpecimens) throws SQLException
    {
        mSpecimens.setContainer(c.getId());
        return Table.update(user, SpecimentrackingSchema.getInstance().getTableInfoManifestSpecimens(), mSpecimens, mSpecimens.getSpecimenId());
    }

    public Date isValidDate(String sDateIn)
    {
        try
        {
            Date dDate = new Date(DateUtil.parseDateTime(sDateIn));
            return dDate;
        }
        catch (ConversionException x)
        {
            return null;
        }
    }

    public Sites[] getLabs()
    {
        return new SqlSelector(SpecimentrackingSchema.getInstance().getStudySchema(), "select label,ldmslabcode from study.site where endpoint = true group by ldmslabcode,label").getArray(Sites.class);
    }
}