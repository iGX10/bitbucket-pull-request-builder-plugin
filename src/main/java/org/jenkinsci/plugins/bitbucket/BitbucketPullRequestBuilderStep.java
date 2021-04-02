/*
 * The MIT License
 *
 * Copyright 2021 ADRIA BUSINESS & TECHNOLOGY.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jenkinsci.plugins.bitbucket;

import com.google.inject.Inject;

import hudson.Extension;
import hudson.XmlFile;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

import jenkins.model.Jenkins;

import org.jenkinsci.plugins.bitbucket.Exceptions.BitbucketPullRequestBuilderException;
import org.jenkinsci.plugins.bitbucket.constantes.Messages;
import org.jenkinsci.plugins.bitbucket.services.BitbucketFactory;
import org.jenkinsci.plugins.bitbucket.services.PullRequestService;
import org.jenkinsci.plugins.bitbucket.utils.BitbucketHelper;
import org.jenkinsci.plugins.workflow.steps.AbstractStepDescriptorImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractStepImpl;
import org.jenkinsci.plugins.workflow.steps.AbstractSynchronousNonBlockingStepExecution;

import org.jenkinsci.plugins.workflow.steps.StepContextParameter;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.DataBoundConstructor;

public class BitbucketPullRequestBuilderStep extends AbstractStepImpl {

    private static final Logger logger = Logger.getLogger(BitbucketPullRequestBuilderStep.class.getName());

    private String credentialsId;
    public String getCredentialsId() { return this.credentialsId; }
    @DataBoundSetter public void setCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
    }

    private String actionType;
    public String getActionType() { return this.actionType; }
    @DataBoundSetter public void setActionType(String actionType) {
        this.actionType = actionType;
    }

    private String pullRequestLink;
    public String getPullRequestLink() { return this.pullRequestLink; }
    @DataBoundSetter public void setPullRequestLink(String pullRequestLink) {
        this.pullRequestLink = pullRequestLink;
    }

    private String pullRequestId;
    public String getPullRequestId() { return this.pullRequestId; }
    @DataBoundSetter public void setPullRequestId(String pullRequestId) {
        this.pullRequestId = pullRequestId;
    }

    private String message;
    public String getMessage() { return this.message; }
    @DataBoundSetter public void setMessage(String message) {
        this.message = message;
    }

    @DataBoundConstructor
    public BitbucketPullRequestBuilderStep() {

    }
    @Override
    public DescriptorImpl getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    @Extension
    public static class DescriptorImpl extends AbstractStepDescriptorImpl {

        private String globalCredentialsId;

        public String getGlobalCredentialsId() {
            return globalCredentialsId;
        }

        public void setGlobalCredentialsId(String globalCredentialsId) {
            this.globalCredentialsId = globalCredentialsId;
        }

        public DescriptorImpl() {
            super(Execution.class);
        }


        @Override
        protected XmlFile getConfigFile() {
            return new XmlFile(new File(Jenkins.getInstance().getRootDir(), this.getId().replace("Step", "") + ".xml"));
        }

        @Override
        public String getFunctionName() {
            return "bitbucketPullRequestBuilder";
        }

        @Override
        public String getDisplayName() {
            return "Manage a Pull Request in BitBucket.";
        }
    }

    public static class Execution extends AbstractSynchronousNonBlockingStepExecution<Void> {
        private static final long serialVersionUID = 1L;

        @StepContextParameter
        private transient Run<?, ?> build;

        @StepContextParameter
        private transient TaskListener taskListener;


        @Inject
        private transient BitbucketPullRequestBuilderStep step;

        private void readGlobalConfiguration() throws IOException {
            XmlFile config = step.getDescriptor().getConfigFile();
            BitbucketPullRequestBuilder.DescriptorImpl cfg = new BitbucketPullRequestBuilder.DescriptorImpl();
            try {
                config.unmarshal(cfg);
                step.getDescriptor().setGlobalCredentialsId(cfg.getGlobalCredentialsId());
            } catch (IOException e) {
                logger.warning("Unable to read BitbucketPullRequestBuilder configuration");
            }
        }

        @Override
        public Void run() throws BitbucketPullRequestBuilderException, IOException {
            this.readGlobalConfiguration();
            PullRequestService pullRequestService = BitbucketFactory.getInstance().getPullRequestService();

            String credentialsId = step.getCredentialsId()==null ? step.getDescriptor().getGlobalCredentialsId() :  step.getCredentialsId() ;
            String pullRequestLink = step.getPullRequestLink();
            String pullRequestId = step.getPullRequestId();
            String actionType = step.getActionType() != null ? step.getActionType() : "";
            String message = step.getMessage() != null ? step.getMessage() : "";
            String workspace_repo = BitbucketHelper.getWorkspaceAndRepoName(pullRequestLink);

            switch (actionType.toLowerCase()){
                case "decline":
                    {
                        pullRequestService.decline(credentialsId, workspace_repo, pullRequestId, message);
                        taskListener.getLogger().println("Pull-request with ID " + pullRequestId + " was declined");
                        break;
                    }
                default: throw new BitbucketPullRequestBuilderException(Messages.MESSAGE_NO_VALID_ACTION_TYPE);
            }

            return null;
        }
    }
}
