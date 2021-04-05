package org.jenkinsci.plugins.bitbucket.services.impls;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.jenkinsci.plugins.bitbucket.Exceptions.BitbucketPullRequestBuilderException;
import org.jenkinsci.plugins.bitbucket.constantes.Endpoints;
import org.jenkinsci.plugins.bitbucket.constantes.Messages;
import org.jenkinsci.plugins.bitbucket.dto.MessageDto;
import org.jenkinsci.plugins.bitbucket.services.AuthService;
import org.jenkinsci.plugins.bitbucket.services.BitbucketFactory;
import org.jenkinsci.plugins.bitbucket.services.PullRequestService;
import org.scribe.model.Token;

import java.io.IOException;

public class PullRequestServiceImpl implements PullRequestService {
    private final AuthService authService;

    public PullRequestServiceImpl() {
        authService = BitbucketFactory.getInstance().getAuthService();
    }

    @Override
    public void decline(String credentialsId, String workspaceAndRepo, String idPullRequest, String message) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(Endpoints.BITBUCKET_API_ENDPOINT + "/" + workspaceAndRepo + "/pullrequests/" + idPullRequest + "/decline");
        prepareRequest(httpPost, credentialsId);

        MessageDto messageDto = new MessageDto(message);
        ObjectMapper mapper = new ObjectMapper();

        StringEntity entity = new StringEntity(mapper.writeValueAsString(messageDto));
        httpPost.setEntity(entity);

        HttpResponse response = httpclient.execute(httpPost);
        boolean declineReponse = response.getStatusLine().getStatusCode() == 200;

        if (!declineReponse) {
            throw new BitbucketPullRequestBuilderException(Messages.MESSAGE_PULL_REQUEST_WAS_NOT_DECLINED);
        }
    }

    @Override
    public void approve(String credentialsId, String workspaceAndRepo, String idPullRequest) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost = new HttpPost(Endpoints.BITBUCKET_API_ENDPOINT + "/" + workspaceAndRepo + "/pullrequests/" + idPullRequest + "/approve");

        prepareRequest(httpPost, credentialsId);
        StringEntity entity = new StringEntity("{}");
        httpPost.setEntity(entity);

        HttpResponse response = httpclient.execute(httpPost);
        boolean declineReponse = response.getStatusLine().getStatusCode() == 200;

        if (!declineReponse) {
            throw new BitbucketPullRequestBuilderException(Messages.MESSAGE_PULL_REQUEST_WAS_NOT_APPROVED);
        }
    }

    private void prepareRequest(HttpPost httpPost, String credentialsId) {
        Token accessToken = authService.getToken(credentialsId, null);
        if (accessToken.isEmpty()) {
            throw new BitbucketPullRequestBuilderException(Messages.MESSAGE_INVALID_BITBUCKET_OAUTH_CREDENTIALS);
        }
        String token = accessToken.getToken();
        httpPost.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        httpPost.setHeader(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");
    }
}
