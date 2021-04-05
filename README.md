# Bitbucket Cloud Pull Request Builder plugin for Jenkins - [![Build Status][jenkins-status]][jenkins-builds]

The idea behind creating this plugin is to automate the code reviewing by devs. Instead of creating a pull request 
and waiting for the team lead to review/approve it, the pull request can be automatically declined if it does not 
pass the quality conditions defined in SonarQube as a use case for example.

Note: This plugin aims at the Atlassian-hosted BitBucket Cloud solution, not BitBucket Server (formerly known as Stash).

## Features

* Declining a pull request in Bitbucket

## Dependencies
This plugin depends on other Jenkins plugins:

* [Credentials Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Credentials+Plugin)

Please install them before if they are still not installed on your Jenkins server.

## Instructions

### Create a OAuth Consumer
First you need to get a OAuth consumer key/secret from Bitbucket.

1. Login into your Bitbucket account.
2. Click your account name and then in **Settings** from the menu bar.
3. Click **OAuth** from the menu bar.
4. Press the **Add consumer** button.
6. The system requests the following information:
 1. Give a representative **name** to the consumer e.g. Jenkins build status notifier.
 2. Although is not used, a **Callback URL** must be set e.g. ci.your-domain.com.
 2. Leave blank the **URL** field.
 3. Add **Read** and **Write** permissions to **Repositories**.
 4. Click **Save** button and a **Key** and **Secret** will be automatically generated.

### Ensure Jenkins URL is set
Second, ensure that Jenkins URL is properly set:

1. Open Jenkins **Manage Jenkins** page.
2. Click **Configure System** page.
3. Got to the section **Jenkins Location**.
4. Set correct URL to **Jenkins URL**.
5. Click **Save** button.

### Add OAuth Credentials to Jenkins
Third, you need to add the Bitbucket OAuth Consumer credentials. You have two ways to configure it globally or locally:

#### Global

1. Open Jenkins **Manage Jenkins** page.
2. Click **Configure System**.
3. Go to the section **Bitbucket Pull Request Builder plugin**
4. If you still don't have stored the credentials click **Add**, otherwise you can skip this step.
 1. Select **Username with password**.
 2. Set the the OAuth consumer **key** in **Username**.
 3. Set the the OAuth consumer **secret** in **Password**.
 4. Click **Add** button.
5. Select the desired credentials.
6. Click **Save** button.

### Pipeline step to manage pull requests in Bitbucket

Once you have configured the credential, you can manage a pull request in BitBucket from your Pipeline script through the `bitbucketPullRequestBuilder` step.

#### Usage

The `bitbucketPullRequestBuilder` step manages a pull request based on the `actionType` provided. Also, it's identified by credentials id, pull request id and pull request link.

The different valid action types that can be used for the time being are :

* `decline`


```groovy
  ...
  stage 'Decline Pull Request'
    steps
    bitbucketPullRequestBuilder(
          credentialsId: "your-credentials-id",
          actionType: "decline",
          pullRequestId: env.BITBUCKET_PULL_REQUEST_ID,
          pullRequestLink: env.BITBUCKET_PULL_REQUEST_LINK,
          message : 'Your message'       
        )
  ...
```
* `approve`


```groovy
  ...
  stage 'Approve Pull Request'
    steps
    bitbucketPullRequestBuilder(
          credentialsId: "your-credentials-id",
          actionType: "approve",
          pullRequestId: env.BITBUCKET_PULL_REQUEST_ID,
          pullRequestLink: env.BITBUCKET_PULL_REQUEST_LINK,  
        )
  ...
```

Notes : 

* The environment variables used in this example (`BITBUCKET_PULL_REQUEST_ID` and `BITBUCKET_PULL_REQUEST_LINK`) are from the [Bitbucket Push and Pull Request Plugin][bitbucket-push-and-pull-request-plugin]. They can be replaced by raw values.
* Other actions can be added to the plugin later on if needed (when releasing new features).


#### API Summary

Parameter:

| Name | Type | Optional | Description |
| --- | --- | --- | --- |
| `credentialsId` | String | yes | The jenkins credential id
| `actionType` | String | no | The action to perform. (IT CAN ONLY TAKE `decline` and `approve` FOR NOW! more can be added in future updates, like  `merge`)
| `pullRequestId` | String | no | The id identifying the pull request
| `pullRequestLink` | String | no | The link of the pull request (used to extract the workspace/repository names)
| `message` | String | yes | The message to define the reason why you declined the Pull Request

## Contributions

Contributions are welcome! For feature requests and bug reports please read the following Wiki page for guidelines on [how to submit an issue][how-to-submit-issue].

## License

The MIT License (MIT)

Copyright (c) 2021 Adria Business & Technology

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit
persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of
the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

[bitbucket-push-and-pull-request-plugin]: https://plugins.jenkins.io/bitbucket-push-and-pull-request/
[jenkins-builds]: https://ci.jenkins.io/job/plugins/job/bitbucket-pull-request-builder-plugin/job/master
[jenkins-status]: https://ci.jenkins.io/buildStatus/icon?job=plugins/bitbucket-pull-request-builder-plugin/master
[how-to-submit-issue]: https://wiki.jenkins-ci.org/display/JENKINS/How+to+report+an+issue
