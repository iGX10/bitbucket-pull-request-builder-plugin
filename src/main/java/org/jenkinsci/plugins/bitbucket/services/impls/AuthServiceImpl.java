package org.jenkinsci.plugins.bitbucket.services.impls;

import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.Job;
import org.jenkinsci.plugins.bitbucket.Exceptions.BitbucketPullRequestBuilderException;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApi;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApiService;
import org.jenkinsci.plugins.bitbucket.constantes.Endpoints;
import org.jenkinsci.plugins.bitbucket.constantes.Messages;
import org.jenkinsci.plugins.bitbucket.services.AuthService;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;

public class AuthServiceImpl implements AuthService {
    @Override
    public Token getToken(String credentialsId, Job<?, ?> owner) {
        if (credentialsId == null || credentialsId.isEmpty()) {
            throw new BitbucketPullRequestBuilderException(Messages.MESSAGE_CREDENTIALS_ID_EMPTY);
        }
        StandardUsernamePasswordCredentials credentials = getCredentials(credentialsId, owner);
        OAuthConfig config = new OAuthConfig(credentials.getUsername(), credentials.getPassword().getPlainText());
        BitbucketApiService apiService = (BitbucketApiService) new BitbucketApi().createService(config);
        Token accessToken = apiService.getAccessToken();

        return accessToken;
    }

    private StandardUsernamePasswordCredentials getCredentials(String credentialsId, Job<?, ?> owner) {
        if (credentialsId != null) {
            for (StandardUsernamePasswordCredentials c : CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class, owner, null,
                    URIRequirementBuilder.fromUri(Endpoints.BITBUCKET_OAUTH_ENDPOINT).build())) {
                if (c.getId().equals(credentialsId)) {
                    return c;
                }
            }
        }
        return null;
    }
}
