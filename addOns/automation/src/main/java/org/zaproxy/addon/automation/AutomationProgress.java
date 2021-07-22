/*
 * Zed Attack Proxy (ZAP) and its related class files.
 *
 * ZAP is an HTTP/HTTPS proxy for assessing web application security.
 *
 * Copyright 2021 The ZAP Development Team
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
package org.zaproxy.addon.automation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.parosproxy.paros.CommandLine;

public class AutomationProgress {

    private List<String> errors = new ArrayList<>();
    private List<String> warnings = new ArrayList<>();
    private List<String> infos = new ArrayList<>();
    private List<String> allMessages = new ArrayList<>();
    private List<AutomationJob> runJobs = new ArrayList<>();
    private boolean outputToStdout = false;
    private Map<String, JobResultData> jobResultDataMap = new HashMap<>();
    private Map<AutomationJob, JobResults> jobResults = new HashMap<>();
    private int errorMark;
    private int warningMark;
    private int infoMark;

    public AutomationProgress() {}

    public void error(String error) {
        this.errors.add(error);
        this.allMessages.add(error);
        if (outputToStdout) {
            CommandLine.error(error);
        }
        AutomationEventPublisher.publishMessageEvent(
                AutomationEventPublisher.PLAN_ERROR_MESSAGE, error);
    }

    public void warn(String warning) {
        this.warnings.add(warning);
        this.allMessages.add(warning);
        if (outputToStdout) {
            CommandLine.info(warning);
        }
        AutomationEventPublisher.publishMessageEvent(
                AutomationEventPublisher.PLAN_WARNING_MESSAGE, warning);
    }

    public void info(String info) {
        this.infos.add(info);
        this.allMessages.add(info);
        if (outputToStdout) {
            CommandLine.info(info);
        }
        AutomationEventPublisher.publishMessageEvent(
                AutomationEventPublisher.PLAN_INFO_MESSAGE, info);
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public List<String> getInfos() {
        return infos;
    }

    public List<String> getAllMessages() {
        return allMessages;
    }

    public boolean hasErrors() {
        return errors.size() > 0;
    }

    public boolean hasWarnings() {
        return warnings.size() > 0;
    }

    public boolean isOutputToStdout() {
        return outputToStdout;
    }

    public void setOutputToStdout(boolean outputToStdout) {
        this.outputToStdout = outputToStdout;
    }

    public void addJobResultData(JobResultData data) {
        this.jobResultDataMap.put(data.getKey(), data);
    }

    public void addJobResultData(List<JobResultData> list) {
        for (JobResultData data : list) {
            this.jobResultDataMap.put(data.getKey(), data);
        }
    }

    public JobResultData getJobResultData(String key) {
        return this.jobResultDataMap.get(key);
    }

    public Collection<JobResultData> getAllJobResultData() {
        return this.jobResultDataMap.values();
    }

    void addRunJob(AutomationJob job) {
        this.runJobs.add(job);

        // Record any new errors, warnings or infos which will have been generated by this job
        this.jobResults.put(
                job,
                new JobResults(
                        new ArrayList<>(errors.subList(errorMark, errors.size())),
                        new ArrayList<>(warnings.subList(warningMark, warnings.size())),
                        new ArrayList<>(infos.subList(infoMark, infos.size()))));

        errorMark = errors.size();
        warningMark = warnings.size();
        infoMark = infos.size();
    }

    public List<AutomationJob> getRunJobs() {
        return this.runJobs;
    }

    public JobResults getJobResults(AutomationJob job) {
        JobResults res = this.jobResults.get(job);
        if (res == null) {
            return new JobResults(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
        return res;
    }

    public List<String> getErrors(AutomationJob job) {
        JobResults results = this.jobResults.get(job);
        if (results != null) {
            return results.getErrors();
        }
        return new ArrayList<>();
    }

    public List<String> getWarnings(AutomationJob job) {
        JobResults results = this.jobResults.get(job);
        if (results != null) {
            return results.getWarnings();
        }
        return new ArrayList<>();
    }

    public List<String> getInfos(AutomationJob job) {
        JobResults results = this.jobResults.get(job);
        if (results != null) {
            return results.getInfos();
        }
        return new ArrayList<>();
    }

    public Map<String, String> toMap() {
        Map<String, String> map = new HashMap<>();
        map.put("errors.count", Integer.toString(this.errors.size()));
        map.put("warnings.count", Integer.toString(this.warnings.size()));
        map.put("infos.count", Integer.toString(this.infos.size()));
        return map;
    }

    public class JobResults {

        private List<String> errors;
        private List<String> warnings;
        private List<String> infos;

        JobResults(List<String> errors, List<String> warnings, List<String> infos) {
            this.errors = errors;
            this.warnings = warnings;
            this.infos = infos;
        }

        public List<String> getErrors() {
            return errors;
        }

        public List<String> getWarnings() {
            return warnings;
        }

        public List<String> getInfos() {
            return infos;
        }

        public Map<String, String> toMap() {
            Map<String, String> map = new HashMap<>();
            map.put("errors.count", Integer.toString(this.errors.size()));
            map.put("warnings.count", Integer.toString(this.warnings.size()));
            map.put("infos.count", Integer.toString(this.infos.size()));
            return map;
        }
    }
}
