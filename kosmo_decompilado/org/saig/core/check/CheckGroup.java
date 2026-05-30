/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.apache.commons.collections.CollectionUtils
 *  org.apache.log4j.Logger
 */
package org.saig.core.check;

import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.task.TaskMonitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.saig.core.check.Check;
import org.saig.core.check.self.SelfCheck;
import org.saig.core.dao.datasource.filedatasource.shape.ShapeFileDataSource;
import org.saig.jump.lang.I18N;
import org.saig.jump.widgets.summary.SummaryMessage;

public class CheckGroup {
    private Map<String, Object> metadata;
    private List<Check> checkList;
    private List<SelfCheck> selfCheckList;
    public static final Logger LOGGER = Logger.getLogger((String)"org.saig.core.check.CheckGroup");
    public static final String SUMMARY_KEY = I18N.getString("org.saig.core.check.CheckGroup.Summary");
    public static final String ERROR_KEY = I18N.getString("org.saig.core.check.CheckGroup.Errors");
    public static final String WARNING_KEY = I18N.getString("org.saig.core.check.CheckGroup.Incidents");
    public static final String OK_KEY = I18N.getString("org.saig.core.check.CheckGroup.No-incidents");

    public CheckGroup() {
        this.metadata = new HashMap<String, Object>();
        this.checkList = new ArrayList<Check>();
        this.selfCheckList = new ArrayList<SelfCheck>();
    }

    public CheckGroup(Map<String, Object> metadata, ArrayList<Check> checks, ArrayList<SelfCheck> selfChecks) {
        this.metadata = metadata;
        this.checkList = checks;
        this.selfCheckList = selfChecks;
    }

    public List<Check> getCheckList() {
        return this.checkList;
    }

    public void setCheckList(List<Check> checkList) {
        this.checkList = checkList;
    }

    public List<SelfCheck> getSelfCheckList() {
        return this.selfCheckList;
    }

    public void setSelfCheckList(List<SelfCheck> selfCheckList) {
        this.selfCheckList = selfCheckList;
    }

    public Map<String, Object> getMetadata() {
        return this.metadata;
    }

    public void setMetadata(Map<String, Object> metadata) {
        this.metadata = metadata;
    }

    public Set<String> checkAllGroup(Map<String, List<SummaryMessage>> messageMap, TaskMonitor monitor) {
        if (CollectionUtils.isEmpty(this.checkList) && CollectionUtils.isEmpty(this.selfCheckList)) {
            LOGGER.warn((Object)I18N.getString("org.saig.core.check.CheckGroup.There-is-no-checks-to-make"));
            return new HashSet<String>();
        }
        int contador = 0;
        long t1 = System.currentTimeMillis();
        ArrayList<Check> checksToProcess = new ArrayList<Check>();
        checksToProcess.addAll(this.checkList);
        checksToProcess.addAll(this.selfCheckList);
        TreeSet<String> filesToLoad = new TreeSet<String>();
        LinkedHashMap<String, Integer> checkToIncidents = new LinkedHashMap<String, Integer>();
        LinkedHashMap<String, List<SummaryMessage>> checkToErrors = new LinkedHashMap<String, List<SummaryMessage>>();
        Iterator itChecks = checksToProcess.iterator();
        while (itChecks.hasNext() && !monitor.isCancelRequested()) {
            Check currentCheck = (Check)itChecks.next();
            String checkID = I18N.getMessage("org.saig.core.check.CheckGroup.Check-{0}", new Object[]{new Integer(++contador)});
            ArrayList<SummaryMessage> messageList = new ArrayList<SummaryMessage>();
            int numIncidencias = 0;
            if (!this.checkFilePath(currentCheck.getFilePath())) {
                LOGGER.warn((Object)I18N.getMessage("org.saig.core.check.CheckGroup.Invalid-incident-file-for-the-check-{0}", new Object[]{new Integer(contador)}));
                messageList.add(this.buildFilePathErrorMessage(checkID, currentCheck.getFilePath(), null));
                checkToIncidents.put(checkID, new Integer(numIncidencias));
                checkToErrors.put(checkID, messageList);
                continue;
            }
            FeatureCollection fc = currentCheck.check(messageList);
            if (fc != null) {
                int size = 0;
                try {
                    size = fc.size();
                }
                catch (Exception e) {
                    LOGGER.error((Object)"", (Throwable)e);
                }
                numIncidencias = size;
                try {
                    ShapeFileDataSource.toShape(fc, currentCheck.getFilePath(), ShapeFileDataSource.DEFAULT_STRING_CHARSET);
                    filesToLoad.add(currentCheck.getFilePath());
                }
                catch (Exception e) {
                    LOGGER.error((Object)I18N.getMessage("org.saig.core.check.CheckGroup.Error-generating-the-file-{0}", new Object[]{currentCheck.getFilePath()}), (Throwable)e);
                    messageList.add(this.buildFilePathErrorMessage(checkID, currentCheck.getFilePath(), e));
                }
            }
            checkToIncidents.put(checkID, new Integer(numIncidencias));
            checkToErrors.put(checkID, messageList);
            monitor.report(contador, this.size(), I18N.getString("org.saig.core.check.CheckGroup.Processed-checks"));
        }
        LOGGER.info((Object)I18N.getMessage("org.saig.core.check.CheckGroup.Processed-{0}-checks-in-{1}-ms", new Object[]{new Integer(this.size()), new Long(System.currentTimeMillis() - t1)}));
        this.buildSummary(filesToLoad.size(), checkToIncidents, checkToErrors, messageMap);
        return filesToLoad;
    }

    private void buildSummary(int numFilesToLoad, Map<String, Integer> checkToIncidents, Map<String, List<SummaryMessage>> checkToErrors, Map<String, List<SummaryMessage>> messageMap) {
        ArrayList<SummaryMessage> summaryMessageList = new ArrayList<SummaryMessage>();
        ArrayList<SummaryMessage> okMessageList = new ArrayList<SummaryMessage>();
        ArrayList<SummaryMessage> warningMessageList = new ArrayList<SummaryMessage>();
        ArrayList<SummaryMessage> errorMessageList = new ArrayList<SummaryMessage>();
        int numTotalIncidencias = 0;
        int numTotalErrores = 0;
        String basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Number-of-processed-checks-{0}", new Object[]{new Integer(this.size())});
        String extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-checks-have-been-processed", new Object[]{new Integer(this.size())});
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 0);
        summaryMessageList.add(message);
        basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Number-of-incident-layers-generated-{0}", new Object[]{new Integer(numFilesToLoad)});
        extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-incident-layers-have-been-generated-from-the-{1}-checks-of-the-file", new Object[]{new Integer(numFilesToLoad), new Integer(this.size())});
        message = new SummaryMessage(basicMessage, extendedMessage, 0);
        summaryMessageList.add(message);
        for (String currentCheckName : checkToIncidents.keySet()) {
            Integer numIncidencias = checkToIncidents.get(currentCheckName);
            List<SummaryMessage> errorList = checkToErrors.get(currentCheckName);
            numTotalIncidencias += numIncidencias.intValue();
            numTotalErrores += errorList.size();
            if (errorList.size() == 0) {
                if (numIncidencias == 0) {
                    basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Check-{0}-processed-without-incidents", new Object[]{currentCheckName});
                    extendedMessage = I18N.getString("org.saig.core.check.CheckGroup.Invalid-elements-have-not-been-found");
                    message = new SummaryMessage(basicMessage, extendedMessage, 0);
                    okMessageList.add(message);
                    continue;
                }
                basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Check-{0}-processed-with-incidents", new Object[]{currentCheckName});
                extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-invalid-elements-have-been-found", new Object[]{new Integer(numIncidencias)});
                message = new SummaryMessage(basicMessage, extendedMessage, 1);
                warningMessageList.add(message);
                continue;
            }
            basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Check-{0}-with-errors", new Object[]{currentCheckName});
            extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-errors-have-found-while-processing-the-check", new Object[]{new Integer(errorList.size())});
            message = new SummaryMessage(basicMessage, extendedMessage, 2);
            errorMessageList.add(message);
            Iterator<SummaryMessage> iterator = errorList.iterator();
            while (iterator.hasNext()) {
                errorMessageList.add(iterator.next());
            }
        }
        basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Total-number-of-processed-checks-without-incidents-{0}", new Object[]{new Integer(okMessageList.size())});
        extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-checks-have-been-processed-without-incidents", new Object[]{new Integer(okMessageList.size())});
        message = new SummaryMessage(basicMessage, extendedMessage, 0);
        summaryMessageList.add(message);
        basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Total-number-of-detected-incidents{0}", new Object[]{new Integer(numTotalIncidencias)});
        extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-incidents-have-been-detected-from-{1}-checks", new Object[]{new Integer(numTotalIncidencias), new Integer(this.size())});
        message = new SummaryMessage(basicMessage, extendedMessage, 1);
        summaryMessageList.add(message);
        basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Total-number-of-detected-errors-{0}", new Object[]{new Integer(numTotalErrores)});
        extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.{0}-errors-have-been-detected-from-{1}-checks", new Object[]{new Integer(numTotalErrores), new Integer(this.size())});
        message = new SummaryMessage(basicMessage, extendedMessage, 2);
        summaryMessageList.add(message);
        messageMap.put(SUMMARY_KEY, summaryMessageList);
        messageMap.put(String.valueOf(OK_KEY) + " (" + okMessageList.size() + ")", okMessageList);
        messageMap.put(String.valueOf(WARNING_KEY) + " (" + warningMessageList.size() + ")", warningMessageList);
        messageMap.put(String.valueOf(ERROR_KEY) + " (" + (errorMessageList.size() - numTotalErrores) + ")", errorMessageList);
    }

    private boolean checkFilePath(String filePath) {
        boolean ok = false;
        if (filePath != null) {
            ok = true;
        }
        return ok;
    }

    public int size() {
        int size = 0;
        if (this.checkList != null) {
            size = this.checkList.size();
        }
        if (this.selfCheckList != null) {
            size += this.selfCheckList.size();
        }
        return size;
    }

    protected SummaryMessage buildFilePathErrorMessage(String checkID, String filePath, Exception e) {
        String basicMessage = I18N.getMessage("org.saig.core.check.CheckGroup.Error-while-processing-the-check-{0}", new Object[]{checkID});
        String extendedMessage = I18N.getMessage("org.saig.core.check.CheckGroup.The-path-{0}-is-not-valid", new Object[]{filePath});
        if (e != null) {
            extendedMessage = String.valueOf(extendedMessage) + ": " + e.getMessage();
        }
        SummaryMessage message = new SummaryMessage(basicMessage, extendedMessage, 2);
        return message;
    }
}

