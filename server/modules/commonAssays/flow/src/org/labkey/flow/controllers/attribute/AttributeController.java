/*
 * Copyright (c) 2014-2019 LabKey Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.labkey.flow.controllers.attribute;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.labkey.api.action.ConfirmAction;
import org.labkey.api.action.FormViewAction;
import org.labkey.api.action.ReturnUrlForm;
import org.labkey.api.action.SimpleViewAction;
import org.labkey.api.data.Container;
import org.labkey.api.security.RequiresPermission;
import org.labkey.api.security.permissions.AdminPermission;
import org.labkey.api.util.URLHelper;
import org.labkey.api.view.ActionURL;
import org.labkey.api.view.HtmlView;
import org.labkey.api.view.JspView;
import org.labkey.api.view.NavTree;
import org.labkey.api.view.NotFoundException;
import org.labkey.api.view.template.PageConfig;
import org.labkey.flow.analysis.model.FlowException;
import org.labkey.flow.controllers.BaseFlowController;
import org.labkey.flow.controllers.protocol.ProtocolController;
import org.labkey.flow.data.AttributeType;
import org.labkey.flow.data.FlowDataObject;
import org.labkey.flow.data.FlowProtocol;
import org.labkey.flow.persist.AttributeCache;
import org.labkey.flow.persist.FlowCasingMismatchException;
import org.labkey.flow.persist.FlowManager;
import org.springframework.validation.BindException;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collection;

/**
 * User: kevink
 * Date: 1/27/14
 */
public class AttributeController extends BaseFlowController
{
    private static final Logger _log = Logger.getLogger(AttributeController.class);

    private static final DefaultActionResolver _actionResolver = new DefaultActionResolver(AttributeController.class);

    public AttributeController()
    {
        setActionResolver(_actionResolver);
    }

    public enum Param
    {
        rowId,
        type,

        name
    }

    public static class AttributeForm extends ReturnUrlForm
    {
        private int _rowId;
        private String _type;

        private AttributeType _attributeType;
        private AttributeCache.Entry _entry;

        public AttributeForm()
        {
        }

        public int getRowId()
        {
            return _rowId;
        }

        public void setRowId(int rowId)
        {
            _rowId = rowId;
        }

        public String getType()
        {
            return _type;
        }

        public void setType(String type)
        {
            _type = type;
        }

        // Requires type
        public AttributeType getAttributeType()
        {
            if (_attributeType == null)
            {
                if (_type == null)
                    throw new NotFoundException();

                _attributeType = AttributeType.valueOf(_type);
            }

            return _attributeType;
        }

        // Requires type and rowid
        public AttributeCache.Entry getEntry(Container c)
        {
            if (_entry == null && _rowId != 0)
            {
                AttributeType type = getAttributeType();
                _entry = AttributeCache.forType(type).byRowId(c, _rowId);
                if (_entry == null)
                    throw new NotFoundException();
            }
            return _entry;
        }
    }

    public NavTree appendAttributeNavTrail(PageConfig pageConfig, NavTree root, AttributeType type, String title)
    {
        FlowProtocol protocol = FlowProtocol.getForContainer(getContainer());
        NavTree tree = appendFlowNavTrail(pageConfig, root, protocol, null);

        if (type != null)
        {
            ActionURL summaryURL = new ActionURL(SummaryAction.class, getContainer()).addParameter(Param.type.name(), type.name());
            tree.addChild(StringUtils.capitalize(type.name() + " Summary"), summaryURL);
        }

        if (title != null)
            tree.addChild(title);

        return tree;
    }

    @RequiresPermission(AdminPermission.class)
    public class SummaryAction extends SimpleViewAction<AttributeForm>
    {
        AttributeType _type;
        @Override
        public ModelAndView getView(AttributeForm form, BindException errors)
        {
            _type = form.getAttributeType();
            return new JspView<>("/org/labkey/flow/controllers/attribute/summary.jsp", form, errors);
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return appendAttributeNavTrail(getPageConfig(), root, _type, null);
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class DetailsAction extends SimpleViewAction<AttributeForm>
    {
        AttributeCache.Entry _entry;

        @Override
        public ModelAndView getView(AttributeForm form, BindException errors)
        {
            _entry = form.getEntry(getContainer());
            if (getContainer() != _entry.getContainer())
                throw new NotFoundException();

            return new JspView<>("/org/labkey/flow/controllers/attribute/details.jsp", form);
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            AttributeType type = _entry == null ? null : _entry.getType();
            String title = _entry == null ? null : (_entry.getName() + " Details");
            return appendAttributeNavTrail(getPageConfig(), root, type, title);
        }
    }

    public static class EditAttributeForm extends AttributeForm
    {
        private String _name;

        public String getName()
        {
            return _name;
        }

        public void setName(String name)
        {
            _name = name;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class EditAction extends FormViewAction<EditAttributeForm>
    {
        AttributeCache.Entry _entry;

        @Override
        public void validateCommand(EditAttributeForm form, Errors errors)
        {
            _entry = form.getEntry(getContainer());
            if (getContainer() != _entry.getContainer())
                throw new NotFoundException();

            String name = form.getName();
            if (name == null || name.isBlank())
            {
                // check this attribute doesn't have aliases
                Collection aliases = _entry.getAliases();
                if (!aliases.isEmpty())
                    errors.rejectValue("name", ERROR_MSG, _entry.getType() + " '" + _entry.getName() + "' has " + aliases.size() + " aliases and can't be deleted.");

                // check there are no usages of the attribute
                Collection<FlowDataObject> usages = FlowManager.get().getUsages(_entry.getType(), _entry.getRowId());
                if (!usages.isEmpty())
                    errors.rejectValue("name", ERROR_MSG, _entry.getType() + " '" + _entry.getName() + "' has " + usages.size() + " usages and can't be deleted.");
            }
            else
            {
                try
                {
                    // parse the name
                    _entry.getType().createAttribute(name);
                }
                catch (FlowException | IllegalArgumentException e)
                {
                    errors.rejectValue("name", ERROR_MSG, e.getMessage());
                }
            }
        }

        @Override
        public ModelAndView getView(EditAttributeForm form, boolean reshow, BindException errors)
        {
            getPageConfig().setFocusId("name");
            return new JspView<>("/org/labkey/flow/controllers/attribute/edit.jsp", form, errors);
        }

        @Override
        public boolean handlePost(EditAttributeForm form, BindException errors)
        {
            String name = form.getName();
            if (name == null || name.isBlank())
            {
                FlowManager.get().deleteAttribute(getContainer(),
                        _entry.getType(), _entry.getRowId(), true);
            }
            else
            {
                FlowManager.get().updateAttribute(getContainer(),
                        _entry.getType(), _entry.getRowId(), form.getName(),
                        _entry.getAliasedId() == null ? _entry.getRowId() : _entry.getAliasedId(),
                        true);
            }

            return true;
        }

        @Override
        public URLHelper getSuccessURL(EditAttributeForm form)
        {
            return form.getReturnActionURL(new ActionURL(SummaryAction.class, getContainer()));
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            AttributeType type = _entry == null ? null : _entry.getType();
            String title = _entry == null ? null : ("Edit " + _entry.getName());
            return appendAttributeNavTrail(getPageConfig(), root, type, title);
        }
    }

    public static class CreateAliasForm extends AttributeForm
    {
        private String _alias;
        private boolean _allowCaseChangeAlias;

        public String getAlias()
        {
            return _alias;
        }

        public void setAlias(String alias)
        {
            _alias = alias;
        }

        public boolean isAllowCaseChangeAlias()
        {
            return _allowCaseChangeAlias;
        }

        public void setAllowCaseChangeAlias(boolean allowCaseChangeAlias)
        {
            _allowCaseChangeAlias = allowCaseChangeAlias;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class CreateAliasAction extends FormViewAction<CreateAliasForm>
    {
        AttributeCache.Entry _entry;

        @Override
        public void validateCommand(CreateAliasForm form, Errors errors)
        {
            _entry = form.getEntry(getContainer());
            if (getContainer() != _entry.getContainer())
                throw new NotFoundException();

            String alias = form.getAlias();
            if (StringUtils.isBlank(alias))
                errors.rejectValue("alias", ERROR_MSG, "Alias name must not be blank");

            if (alias.equals(_entry.getName()))
                errors.rejectValue("alias", ERROR_MSG, "Alias must not be identical to the original");

            try
            {
                // parse the name
                _entry.getType().createAttribute(alias);
            }
            catch (FlowException | IllegalArgumentException e)
            {
                errors.rejectValue("alias", ERROR_MSG, e.getMessage());
            }
        }

        @Override
        public ModelAndView getView(CreateAliasForm form, boolean reshow, BindException errors)
        {
            getPageConfig().setFocusId("alias");
            return new JspView<>("/org/labkey/flow/controllers/attribute/createAlias.jsp", form, errors);
        }

        @Override
        public boolean handlePost(CreateAliasForm form, BindException errors)
        {
            try
            {
                FlowManager.get().ensureAlias(form.getAttributeType(), form.getRowId(), form.getAlias(), form.isAllowCaseChangeAlias(), true, true);
            }
            catch (FlowCasingMismatchException e)
            {
                errors.rejectValue("alias", ERROR_MSG, e.getMessage());
                return false;
            }

            return true;
        }

        @Override
        public URLHelper getSuccessURL(CreateAliasForm form)
        {
            return form.getReturnActionURL(new ActionURL(SummaryAction.class, getContainer()));
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            AttributeType type = _entry == null ? null : _entry.getType();
            String title = _entry == null ? null : ("Create Alias for " + _entry.getName());
            return appendAttributeNavTrail(getPageConfig(), root, type, title);
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class DeleteAction extends ConfirmAction<AttributeForm>
    {
        @Override
        public ModelAndView getConfirmView(AttributeForm form, BindException errors)
        {
            return null;
        }

        @Override
        public boolean handlePost(AttributeForm form, BindException errors)
        {
            return false;
        }

        @Override
        public void validateCommand(AttributeForm form, Errors errors)
        {

        }

        @Override
        public URLHelper getSuccessURL(AttributeForm form)
        {
            return form.getReturnActionURL(new ActionURL(SummaryAction.class, getContainer()));
        }
    }

    public static class DeleteUnusedBean
    {
        public Collection<FlowManager.FlowEntry> unusedKeywords;
        public Collection<FlowManager.FlowEntry> unusedStats;
        public Collection<FlowManager.FlowEntry> unusedGraphs;
    }

    @RequiresPermission(AdminPermission.class)
    public class DeleteUnusedAction extends ConfirmAction<ReturnUrlForm>
    {
        @Override
        public ModelAndView getConfirmView(ReturnUrlForm form, BindException errors)
        {
            DeleteUnusedBean bean = new DeleteUnusedBean();
            bean.unusedKeywords = FlowManager.get().getUnused(getContainer(), AttributeType.keyword);
            bean.unusedStats = FlowManager.get().getUnused(getContainer(), AttributeType.statistic);
            bean.unusedGraphs = FlowManager.get().getUnused(getContainer(), AttributeType.graph);

            if (bean.unusedKeywords.isEmpty() && bean.unusedStats.isEmpty() && bean.unusedGraphs.isEmpty())
            {
                return new HtmlView("There are no unused attributes in this folder.");
            }
            else
            {
                return new JspView<>("/org/labkey/flow/controllers/attribute/deleteUnused.jsp", bean, errors);
            }
        }

        @Override
        public boolean handlePost(ReturnUrlForm form, BindException errors)
        {
            FlowManager.get().deleteUnused(getContainer());
            return true;
        }

        @Override
        public void validateCommand(ReturnUrlForm form, Errors errors)
        {
        }

        @Override
        public URLHelper getSuccessURL(ReturnUrlForm form)
        {
            return form.getReturnActionURL(new ActionURL(ProtocolController.ShowProtocolAction.class, getContainer()));
        }
    }


    public static class CaseSensitivityForm extends ReturnUrlForm
    {
        private boolean _caseSensitiveKeywords;
        private boolean _caseSensitiveStatsAndGraphs;

        public boolean isCaseSensitiveKeywords()
        {
            return _caseSensitiveKeywords;
        }

        public void setCaseSensitiveKeywords(boolean caseSensitiveKeywords)
        {
            _caseSensitiveKeywords = caseSensitiveKeywords;
        }

        public boolean isCaseSensitiveStatsAndGraphs()
        {
            return _caseSensitiveStatsAndGraphs;
        }

        public void setCaseSensitiveStatsAndGraphs(boolean caseSensitiveStatsAndGraphs)
        {
            _caseSensitiveStatsAndGraphs = caseSensitiveStatsAndGraphs;
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class CaseSensitivityAction extends FormViewAction<CaseSensitivityForm>
    {
        @Override
        public void validateCommand(CaseSensitivityForm form, Errors errors)
        {
        }

        @Override
        public ModelAndView getView(CaseSensitivityForm form, boolean reshow, BindException errors) throws Exception
        {
            FlowProtocol protocol = FlowProtocol.getForContainer(getContainer());
            CaseSensitivityForm bean = new CaseSensitivityForm();
            bean._caseSensitiveKeywords = protocol.isCaseSensitiveKeywords();
            bean._caseSensitiveStatsAndGraphs = protocol.isCaseSensitiveStatsAndGraphs();

            return new JspView<>("/org/labkey/flow/controllers/attribute/caseSensitivity.jsp", bean, errors);
        }

        @Override
        public boolean handlePost(CaseSensitivityForm form, BindException errors) throws Exception
        {
            FlowProtocol protocol = FlowProtocol.getForContainer(getContainer());
            protocol.setCaseSensitiveKeywords(getUser(), form.isCaseSensitiveKeywords());
            protocol.setCaseSensitiveStatsAndGraphs(getUser(), form.isCaseSensitiveStatsAndGraphs());
            return true;
        }

        @Override
        public URLHelper getSuccessURL(CaseSensitivityForm form)
        {
            return form.getReturnActionURL(new ActionURL(ProtocolController.ShowProtocolAction.class, getContainer()));
        }

        @Override
        public NavTree appendNavTrail(NavTree root)
        {
            return root.addChild("Case Sensitivity");
        }
    }

    @RequiresPermission(AdminPermission.class)
    public class MakePrimaryAction extends ConfirmAction<AttributeForm>
    {

        @Override
        public ModelAndView getConfirmView(AttributeForm form, BindException errors)
        {
            return null;
        }

        @Override
        public boolean handlePost(AttributeForm form, BindException errors)
        {
            return false;
        }

        @Override
        public void validateCommand(AttributeForm form, Errors errors)
        {

        }

        @Override
        public URLHelper getSuccessURL(AttributeForm form)
        {
            return form.getReturnActionURL(new ActionURL(SummaryAction.class, getContainer()));
        }
    }

}
