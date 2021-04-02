package org.jenkinsci.plugins.bitbucket.services;

import org.jenkinsci.plugins.bitbucket.services.impls.AuthServiceImpl;
import org.jenkinsci.plugins.bitbucket.services.impls.PullRequestServiceImpl;

public final class BitbucketFactory {
    private static final BitbucketFactory INSTANCE = new BitbucketFactory();


    private BitbucketFactory() {
        super();
    }

    public static BitbucketFactory getInstance() {
        return BitbucketFactory.INSTANCE;
    }


    public AuthService getAuthService() {
        return new AuthServiceImpl();
    }

    public PullRequestService getPullRequestService() {
        return new PullRequestServiceImpl();
    }
}
