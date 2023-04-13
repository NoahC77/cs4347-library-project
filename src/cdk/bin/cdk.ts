#!/usr/bin/env node
import 'source-map-support/register';
import * as cdk from 'aws-cdk-lib';
import {LambdaStack} from '../lib/lambda-stack';
import {FrontendStack} from "../lib/frontend-stack";
import {ApiStack} from "../lib/api-stack";

const app = new cdk.App();
new LambdaStack(app, 'LambdaStack', {
    env: {account: "240617664661", region: "us-east-2"},
    vpcId: "vpc-0ccaf6e735ae858d4",
    securityGroupId: "sg-04f5949df32163eca",
    dbId: "database-1",
    dbEndpointAddress: "database-1.cwgxm1r5zwpg.us-east-2.rds.amazonaws.com",
    lambdaDbUser: "iam_user",
    dbResourceId: "db-3IYIWZR3IEULC7ZUC3F37GH4NI"
});

new FrontendStack(app, "FrontendStack", {
    env: {account: "240617664661", region: "us-east-2"}
});

new ApiStack(app, "ApiStack", {
    env: {account: "240617664661", region: "us-east-2"}
});