import * as cdk from "aws-cdk-lib";
import {Construct} from "constructs";
import {CfnOutput, RemovalPolicy, StackProps} from "aws-cdk-lib";
import {AllowedMethods, Distribution, OriginAccessIdentity, ViewerProtocolPolicy} from "aws-cdk-lib/aws-cloudfront";
import {BlockPublicAccess, Bucket} from "aws-cdk-lib/aws-s3";
import {CanonicalUserPrincipal, PolicyStatement} from "aws-cdk-lib/aws-iam";
import {S3Origin} from "aws-cdk-lib/aws-cloudfront-origins";
import {BucketDeployment, Source} from "aws-cdk-lib/aws-s3-deployment";

const lambda_arns = [
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-account636CB76F-iP1j8kZk5AcM",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-commonFE63BF5B-eIj2pOsZAqrN",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-itemDD1DC579-6vyJV0FK8fkI",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-login68DC89FC-Pa2QdzGh816V",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-purchaseOrder390ACECC-JCshvOyVhweT",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-sale0056A4DF-yyUY7PctSkA6",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-salesHistory68094C7B-3p5O1l3r1hbn",
    "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-vendor1515F034-NhPtk6gLHjk8"
]

export class FrontendStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const cloudfrongOAI = new OriginAccessIdentity(this, 'cloudFront-OAI', {
            comment: `OAI for ${id}`
        });

        const siteBucket = new Bucket(this, 'SiteBucket', {
            bucketName: `frontend-bucket${props.env?.account}`,
            publicReadAccess: false,
            blockPublicAccess: BlockPublicAccess.BLOCK_ALL,
            removalPolicy: RemovalPolicy.DESTROY,
            autoDeleteObjects: true
        });

        siteBucket.addToResourcePolicy(
            new PolicyStatement({
                actions: ['s3:GetObject'],
                resources: [siteBucket.arnForObjects('*')],
                principals: [new CanonicalUserPrincipal(cloudfrongOAI.cloudFrontOriginAccessIdentityS3CanonicalUserId)]
            })
        );

        const distribution = new Distribution(this, "SiteDistribution", {
            defaultRootObject: "index.html",
            defaultBehavior: {
                origin: new S3Origin(siteBucket, {originAccessIdentity: cloudfrongOAI}),
                compress: true,
                allowedMethods: AllowedMethods.ALLOW_GET_HEAD_OPTIONS,
                viewerProtocolPolicy: ViewerProtocolPolicy.ALLOW_ALL
            }
        });


        new BucketDeployment(this, "BucketDeployment", {
            sources: [Source.asset("../frontend/build/")],
            destinationBucket: siteBucket,
            distribution,
            distributionPaths: ['/*']
        });

        new CfnOutput(this, `DistributionUrl`, {
            value: distribution.distributionDomainName
        });
    }
}