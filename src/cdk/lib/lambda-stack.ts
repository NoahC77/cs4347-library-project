import * as cdk from 'aws-cdk-lib';
import {CfnOutput, Duration} from 'aws-cdk-lib';
import {Construct} from 'constructs';
import {Code, Function, FunctionUrlAuthType, Runtime} from "aws-cdk-lib/aws-lambda";
import {IVpc, SecurityGroup, Vpc} from "aws-cdk-lib/aws-ec2";
import * as Path from "path";
import {DatabaseInstance, DatabaseInstanceEngine, IDatabaseInstance} from "aws-cdk-lib/aws-rds";
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from 'aws-cdk-lib/aws-iam';
import * as fs from "fs";
import * as path from "path";

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

        const lambdaPropsBase = {
            vpc,
            role: testLambdaRole,
            db,
            lambdaDbUser: "iam_user",
            handler: "Handler::handleRequest"
        }

        fs.readdir(Path.join("..", "lambda/build/distributions"), (err, files) => {
            if (err) {
                console.log(err);
            } else {
                files.forEach((file) => {
                    if (Path.extname(file) === ".zip") {
                        const name = Path.parse(file).name;
                        const fn = createLambda(this, name, {
                            artifactName: file,
                            ...lambdaPropsBase
                        });
                        const endpointUrl = fn.addFunctionUrl({
                            authType: FunctionUrlAuthType.NONE
                        });

                        new CfnOutput(this, `${name}-EndpointURL`, {
                            value: endpointUrl.url
                        })
                    }
                })
            }
        });


    }
}

interface LambdaProps {
    handler: string,
    artifactName: string
    vpc: IVpc,
    role: Role,
    db: IDatabaseInstance,
    lambdaDbUser: string

}

function createLambda(parent: Construct, id: string, lambdaProps: LambdaProps): Function {
    return new Function(parent, id, {
        runtime: Runtime.JAVA_11,
        code: Code.fromAsset(Path.join("..", "lambda/build/distributions", lambdaProps.artifactName)),
        handler: lambdaProps.handler,
        vpc: lambdaProps.vpc,
        allowPublicSubnet: true,
        role: lambdaProps.role,
        timeout: Duration.minutes(1),
        memorySize: 1024,
        environment: {
            DB_HOST_NAME: lambdaProps.db.dbInstanceEndpointAddress,
            DB_PORT: lambdaProps.db.dbInstanceEndpointPort,
            DB_USERNAME: lambdaProps.lambdaDbUser
        }
    })
}