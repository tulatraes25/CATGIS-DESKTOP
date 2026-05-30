/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.deegree.framework.log.ILogger
 *  org.deegree.framework.log.LoggerFactory
 *  org.deegree.framework.xml.ElementList
 *  org.deegree.framework.xml.NamespaceContext
 *  org.deegree.framework.xml.XMLFragment
 *  org.deegree.framework.xml.XMLParsingException
 *  org.deegree.model.filterencoding.capabilities.FilterCapabilities
 *  org.deegree.model.filterencoding.capabilities.Operator
 *  org.deegree.model.filterencoding.capabilities.OperatorFactory110
 *  org.deegree.model.filterencoding.capabilities.SpatialCapabilities
 *  org.deegree.model.filterencoding.capabilities.SpatialOperator
 *  org.deegree.ogcbase.CommonNamespaces
 *  org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException
 */
package org.deegree.model.filterencoding.capabilities;

import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.deegree.framework.log.ILogger;
import org.deegree.framework.log.LoggerFactory;
import org.deegree.framework.xml.ElementList;
import org.deegree.framework.xml.NamespaceContext;
import org.deegree.framework.xml.XMLFragment;
import org.deegree.framework.xml.XMLParsingException;
import org.deegree.framework.xml.XMLTools;
import org.deegree.model.filterencoding.capabilities.FilterCapabilities;
import org.deegree.model.filterencoding.capabilities.Operator;
import org.deegree.model.filterencoding.capabilities.OperatorFactory100;
import org.deegree.model.filterencoding.capabilities.OperatorFactory110;
import org.deegree.model.filterencoding.capabilities.ScalarCapabilities;
import org.deegree.model.filterencoding.capabilities.SpatialCapabilities;
import org.deegree.model.filterencoding.capabilities.SpatialOperator;
import org.deegree.ogcbase.CommonNamespaces;
import org.deegree.ogcwebservices.getcapabilities.UnknownOperatorNameException;
import org.w3c.dom.Element;

public class FilterCapabilities100Fragment
extends XMLFragment {
    private static final long serialVersionUID = 2430362135205814360L;
    private static final URI OGCNS = CommonNamespaces.OGCNS;
    private static final ILogger LOG = LoggerFactory.getLogger(FilterCapabilities100Fragment.class);
    private static NamespaceContext nsContext = CommonNamespaces.getNamespaceContext();

    public FilterCapabilities100Fragment(Element element, URL systemId) {
        super(element);
        this.setSystemId(systemId);
    }

    public FilterCapabilities parseFilterCapabilities() throws XMLParsingException {
        Element e1 = (Element)XMLTools.getRequiredNode(this.getRootElement(), "ogc:Scalar_Capabilities", nsContext);
        Element e2 = (Element)XMLTools.getRequiredNode(this.getRootElement(), "ogc:Spatial_Capabilities", nsContext);
        return new FilterCapabilities(this.parseScalarCapabilities(e1), this.parseSpatialCapabilities(e2));
    }

    private SpatialCapabilities parseSpatialCapabilities(Element spatialElement) throws XMLParsingException {
        Map<String, Element> operatorMap = this.parseOperators((Element)XMLTools.getRequiredNode(spatialElement, "ogc:Spatial_Operators", nsContext));
        ArrayList<SpatialOperator> operators = new ArrayList<SpatialOperator>();
        for (String next : operatorMap.keySet()) {
            try {
                operators.add(OperatorFactory100.createSpatialOperator(next));
            }
            catch (UnknownOperatorNameException e) {
                LOG.logWarning("Operator name not found. Trying again with filter encoding 1.1.0 names...");
                try {
                    operators.add(OperatorFactory110.createSpatialOperator((String)next));
                }
                catch (UnknownOperatorNameException e2) {
                    LOG.logError("Still not found. Here's two stack traces:");
                    LOG.logError(e.getMessage(), (Throwable)e);
                    LOG.logError(e2.getMessage(), (Throwable)e2);
                }
            }
        }
        return new SpatialCapabilities(operators.toArray(new SpatialOperator[operators.size()]));
    }

    private ScalarCapabilities parseScalarCapabilities(Element scalarElement) throws XMLParsingException {
        boolean supportsLogicalOperators = false;
        if (XMLTools.getChildElement("Logical_Operators", OGCNS, scalarElement) != null) {
            supportsLogicalOperators = true;
        }
        Element elem = XMLTools.getChildElement("Comparison_Operators", OGCNS, scalarElement);
        ArrayList<Object> operators = new ArrayList<Object>();
        Map<String, Element> operatorMap = null;
        if (elem != null) {
            operatorMap = this.parseOperators(elem);
            for (String next : operatorMap.keySet()) {
                try {
                    operators.add(OperatorFactory100.createComparisonOperator(next));
                }
                catch (UnknownOperatorNameException e) {
                    LOG.logWarning("Operator name not found. Trying again with filter encoding 1.1.0 names...");
                    try {
                        operators.add(OperatorFactory110.createComparisonOperator((String)next));
                    }
                    catch (UnknownOperatorNameException e2) {
                        LOG.logError("Still not found. Here's two stack traces:");
                        LOG.logError(e.getMessage(), (Throwable)e);
                        LOG.logError(e2.getMessage(), (Throwable)e2);
                    }
                }
            }
        }
        Operator[] comparionsOperators = operators.toArray(new Operator[operators.size()]);
        operators = null;
        elem = XMLTools.getChildElement("Arithmetic_Operators", OGCNS, scalarElement);
        if (elem != null) {
            operatorMap = this.parseOperators(elem);
            operators = new ArrayList();
            for (String operatorName : operatorMap.keySet()) {
                try {
                    if (operatorName.equals("Functions")) {
                        Element functionsElement = operatorMap.get(operatorName);
                        Element functionNamesElement = XMLTools.getRequiredChildElement("Function_Names", OGCNS, functionsElement);
                        List<Element> functionNameList = XMLTools.getRequiredElements(functionNamesElement, "ogc:Function_Name", nsContext);
                        int i = 0;
                        while (i < functionNameList.size()) {
                            Element functionNameElement = functionNameList.get(i);
                            String name = XMLTools.getStringValue(functionNameElement);
                            String argumentCount = XMLTools.getRequiredAttrValue("nArgs", null, functionNameElement);
                            if (name == null || name.length() == 0) {
                                throw new XMLParsingException("Error parsing a 'Function_Name' (namespace: '" + OGCNS + "') element: text node is empty.");
                            }
                            try {
                                operators.add(OperatorFactory100.createArithmeticFunction(name, Integer.parseInt(argumentCount)));
                            }
                            catch (NumberFormatException e) {
                                throw new XMLParsingException("Error parsing 'Function_Name' (namespace: '" + OGCNS + "') element: attribute 'nArgs'" + " does not contain a valid integer value.");
                            }
                            ++i;
                        }
                        continue;
                    }
                    operators.add(OperatorFactory100.createArithmeticOperator(operatorName));
                }
                catch (UnknownOperatorNameException e) {
                    LOG.logWarning("Operator name not found. Trying again with filter encoding 1.1.0 names...");
                    try {
                        operators.add(OperatorFactory110.createComparisonOperator((String)operatorName));
                    }
                    catch (UnknownOperatorNameException e2) {
                        LOG.logError("Still not found. Here's two stack traces:");
                        LOG.logError(e.getMessage(), (Throwable)e);
                        LOG.logError(e2.getMessage(), (Throwable)e2);
                    }
                }
            }
        }
        Operator[] arithmeticOperators = operators != null ? operators.toArray(new Operator[operators.size()]) : new Operator[]{};
        return new ScalarCapabilities(supportsLogicalOperators, comparionsOperators, arithmeticOperators);
    }

    private Map<String, Element> parseOperators(Element operatorsElement) {
        HashMap<String, Element> operators = new HashMap<String, Element>();
        ElementList operatorList = XMLTools.getChildElements(operatorsElement);
        int i = 0;
        while (i < operatorList.getLength()) {
            String namespaceURI = operatorList.item(i).getNamespaceURI();
            if (namespaceURI != null && namespaceURI.equals(OGCNS.toASCIIString())) {
                operators.put(operatorList.item(i).getLocalName(), operatorList.item(i));
            }
            ++i;
        }
        return operators;
    }
}

