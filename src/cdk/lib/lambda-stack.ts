import * as cdk from 'aws-cdk-lib';
import {CfnOutput, Duration, Token} from 'aws-cdk-lib';
import {Construct} from 'constructs';
import {Code, Function, FunctionUrlAuthType, Runtime} from "aws-cdk-lib/aws-lambda";
import {IVpc, SecurityGroup, Vpc} from "aws-cdk-lib/aws-ec2";
import * as Path from "path";
import {DatabaseInstance, DatabaseInstanceEngine, IDatabaseInstance} from "aws-cdk-lib/aws-rds";
import {Effect, ManagedPolicy, PolicyStatement, Role, ServicePrincipal} from 'aws-cdk-lib/aws-iam';
import * as fs from "fs";
import * as path from "path";
import {util} from "prettier";
import skipSpaces = util.skipSpaces;

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

        const fnArns: any[] = []
        const files = fs.promises.readdir(Path.join("..", "lambda/build/distributions"))
            .then((values) => {
                return values
                    .map(value => Path.join("..", "lambda/build/distributions", value))
                    .map((value) => Path.parse(value))
                    .filter(value => value.ext === ".zip")
            }).then((paths) => {
                console.log(paths)
                let functions: [string, Function][] = []
                paths.forEach(path => {
                    const name = path.name

                    functions.push([name, createLambda(this, name, {
                        artifactName: path.base,
                        ...lambdaPropsBase
                    })]);
                });
                return functions
            }).then(fns => {
                fns.forEach(fn => {
                    const endpointUrl = fn[1].addFunctionUrl({
                        authType: FunctionUrlAuthType.NONE
                    });
                    new CfnOutput(this, `${fn[0]}-EndpointURL`, {
                        value: endpointUrl.url
                    });
                });
                return fns;
            }).then(fns => {
                fns.forEach(fn => {
                    new CfnOutput(this, `${fn[0]}ARN`, {
                        value: fn[1].functionArn,
                        exportName: `${fn[0]}ARN`
                    })
                });
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
    });
}