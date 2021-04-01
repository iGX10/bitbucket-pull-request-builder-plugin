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
import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.common.UsernamePasswordCredentials;
import com.cloudbees.plugins.credentials.domains.URIRequirementBuilder;
import hudson.model.*;

import java.io.IOException;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;

import org.jenkinsci.plugins.bitbucket.api.BitbucketApi;
import org.jenkinsci.plugins.bitbucket.api.BitbucketApiService;
import org.scribe.model.OAuthConfig;
import org.scribe.model.Token;

class BitbucketHelper {
    private static final String bitbucket_api_endpoint="https://api.bitbucket.org/2.0/repositories";


    public static StandardUsernamePasswordCredentials getCredentials(String credentialsId, Job<?,?> owner) {
        if (credentialsId != null) {
            for (StandardUsernamePasswordCredentials c : CredentialsProvider.lookupCredentials(
                    StandardUsernamePasswordCredentials.class, owner, null,
                    URIRequirementBuilder.fromUri(BitbucketApi.OAUTH_ENDPOINT).build())) {
                if (c.getId().equals(credentialsId)) {
                    return c;
                }
            }
        }

        return null;
    }

    public static Token getToken(UsernamePasswordCredentials credentials) {
        OAuthConfig config=new OAuthConfig(credentials.getUsername(),credentials.getPassword().getPlainText());
        BitbucketApiService apiService = (BitbucketApiService) new BitbucketApi().createService(config);
        return apiService.getAccessToken();
    }

    public static boolean declinePullRequest(String token, String workspaceAndRepo, String idPullRequest) throws IOException {
        HttpClient httpclient = HttpClients.createDefault();

        HttpPost httpPost=new HttpPost(bitbucket_api_endpoint+"/"+workspaceAndRepo+"/pullrequests/"+idPullRequest+"/decline");

        httpPost.setHeader("Content-Type","application/x-www-form-urlencoded ");

        List<NameValuePair> params = new ArrayList();
        params.add(new BasicNameValuePair("access_token", token));
        httpPost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        HttpResponse response = httpclient.execute(httpPost);

        return response.getStatusLine().getStatusCode() == 200;
    }

    public static String getWorkspaceAndRepoName(String pr_link) {
        String[] tab = pr_link.split("/");
        return tab[3]+"/"+tab[4];
    }
}
