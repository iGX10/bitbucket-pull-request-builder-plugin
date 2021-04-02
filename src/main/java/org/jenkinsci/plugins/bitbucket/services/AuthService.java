package org.jenkinsci.plugins.bitbucket.services;

import hudson.model.Job;
import org.scribe.model.Token;

public interface AuthService {
    Token getToken(String credentialsId, Job<?, ?> owner);
}
