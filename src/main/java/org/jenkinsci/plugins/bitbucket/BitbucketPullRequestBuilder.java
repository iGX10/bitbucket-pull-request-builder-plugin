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

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernameListBoxModel;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Notifier;
import hudson.tasks.Publisher;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import java.util.List;
import jenkins.model.Jenkins;
import net.sf.json.JSONObject;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApi;
import org.jenkinsci.plugins.bitbucket.constantes.Endpoints;
import org.jenkinsci.plugins.bitbucket.services.AuthService;
import org.jenkinsci.plugins.bitbucket.services.BitbucketFactory;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;
import org.scribe.model.*;

public class BitbucketPullRequestBuilder extends Notifier {

    private final String credentialsId;



    @DataBoundConstructor
    public BitbucketPullRequestBuilder(final String credentialsId) {
        super();

        this.credentialsId = credentialsId;
    }





    public String getCredentialsId() {
        return this.credentialsId != null ? this.credentialsId : this.getDescriptor().getGlobalCredentialsId();
    }
    

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) {
        return true;
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return Jenkins.getInstance().getDescriptorByType(DescriptorImpl.class);
    }

    @Override
    public boolean needsToRunAfterFinalized() {
        //This is here to ensure that the reported build status is actually correct. If we were to return false here,
        //other build plugins could still modify the build result, making the sent out HipChat notification incorrect.
        return true;
    }

    @Extension // This indicates to Jenkins that this is an implementation of an extension point.
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        private  String globalCredentialsId;

        public DescriptorImpl() {
            load();
        }

        public  String getGlobalCredentialsId() {
            return globalCredentialsId;
        }

        public void setGlobalCredentialsId(String globalCredentialsId) {
            this.globalCredentialsId = globalCredentialsId;
        }

        @Override
        public String getDisplayName() {
            return "bitbucket  pull request builder";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {

            req.bindJSON(this, formData.getJSONObject("bitbucket-pull-request-builder"));
            save();

            return true;
        }

        public ListBoxModel doFillGlobalCredentialsIdItems() {
            Job owner = null;
            List<DomainRequirement> apiEndpoint = URIRequirementBuilder.fromUri(Endpoints.BITBUCKET_OAUTH_ENDPOINT).build();

            return new StandardUsernameListBoxModel()
                    .withEmptySelection()
                    .withAll(CredentialsProvider.lookupCredentials(StandardUsernamePasswordCredentials.class, owner, null, apiEndpoint));
        }

        public FormValidation doCheckGlobalCredentialsId(@QueryParameter final String globalCredentialsId) {
            if (globalCredentialsId.isEmpty()) {
                return FormValidation.ok();
            }
            Job owner = null;
            return this.checkCredentials(globalCredentialsId, owner);
        }

        private FormValidation checkCredentials(String globalCredentialsId,Job owner) {
            try {
                AuthService authService = BitbucketFactory.getInstance().getAuthService();
                Token token = authService.getToken(globalCredentialsId, owner);

                if (token.isEmpty()) {
                    return FormValidation.error("Invalid Bitbucket OAuth credentials");
                }
            } catch (Exception e) {
                return FormValidation.error(e.getClass() + e.getMessage());
            }

            return FormValidation.ok();
        }
    }
}
