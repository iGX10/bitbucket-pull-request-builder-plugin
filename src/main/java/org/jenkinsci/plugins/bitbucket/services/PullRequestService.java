package org.jenkinsci.plugins.bitbucket.services;

import java.io.IOException;

public interface PullRequestService {
    void decline(String credentialsId, String workspaceAndRepo, String idPullRequest, String message) throws IOException;
    void approve(String credentialsId, String workspaceAndRepo, String idPullRequest) throws IOException;
}
