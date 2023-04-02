import * as cdk from 'aws-cdk-lib';
import {Construct} from 'constructs';
import {Code, Function, Runtime} from "aws-cdk-lib/aws-lambda";
import {SecurityGroup, Vpc} from "aws-cdk-lib/aws-ec2";
import * as Path from "path";
import {DatabaseInstance, DatabaseInstanceEngine} from "aws-cdk-lib/aws-rds";
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from 'aws-cdk-lib/aws-iam';
import {Duration} from "aws-cdk-lib";

interface LambdaStackProps extends cdk.StackProps {
    vpcId: string,
    dbId: string
    lambdaDbUser: string,
    dbEndpointAddress: string
    securityGroupId: string
    dbResourceId: string,

}

export class LambdaStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: LambdaStackProps) {
        super(scope, id, props);

        const vpc = Vpc.fromLookup(this, props!!.vpcId, {
            vpcId: props.vpcId,
            vpcName: ""
        });

        const securityGroup = SecurityGroup.fromLookupById(this, "SecurityGroup", props.securityGroupId);
        const db = DatabaseInstance.fromDatabaseInstanceAttributes(
            this,
            "Database",
            {
                port: 3306,
                instanceEndpointAddress: props.dbEndpointAddress,
                instanceIdentifier: props.dbId,
                securityGroups: [
                    securityGroup
                ],
                engine: DatabaseInstanceEngine.MYSQL
            }
        )

        const dbPolicy = new PolicyStatement({
            effect: Effect.ALLOW,
            actions: [
                "rds-db:connect",
            ],
            resources: [
                `arn:aws:rds-db:${this.region}:${this.account}:dbuser:${props.dbResourceId}/${props.lambdaDbUser}`
            ]
        })


        const testLambdaRole = new Role(this, 'GenerateFunctionRole', {
            assumedBy: new ServicePrincipal('lambda.amazonaws.com'),
            description: 'Role used by the test lambda.',
        });

        testLambdaRole.addManagedPolicy(ManagedPolicy.fromManagedPolicyArn(this, "LambdaPolicy", "arn:aws:iam::aws:policy/service-role/AWSLambdaVPCAccessExecutionRole"));
        testLambdaRole.addToPolicy(dbPolicy)

        const testLambda = new Function(this, "TestFunction", {
            runtime: Runtime.JAVA_11,
            code: Code.fromAsset(Path.join("..", "lambda/build/distributions", "lambda-1.0.0.zip")),
            handler: "TestLambdaHandler::handleRequest",
            vpc,
            allowPublicSubnet: true,
            role: testLambdaRole,
            timeout: Duration.minutes(5),
            memorySize:1024,
            environment: {
                DB_HOST_NAME:db.dbInstanceEndpointAddress,
                DB_PORT:db.dbInstanceEndpointPort,
                DB_USERNAME:props.lambdaDbUser
            }
        });
    }
}
