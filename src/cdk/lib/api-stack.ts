import * as cdk from "aws-cdk-lib";
import {Fn, StackProps} from "aws-cdk-lib";
import {Construct} from "constructs";
import {HttpApi, HttpMethod, HttpRouteIntegration} from "@aws-cdk/aws-apigatewayv2-alpha";
import {HttpLambdaIntegration, HttpUrlIntegration} from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import {Function, IFunction} from "aws-cdk-lib/aws-lambda";
import {globSync} from "glob";
import * as Path from "path";

interface Endpoint {
    lambda: LambdaCategory,
    path: string
}

enum LambdaCategory {
    item = "item",
    warehouse = "warehouse",
    vendor = "vendor",
    sale = "sale",
    account = "account",
    suppliedItem = "suppliedItem",
    purchaseOrder = "purchaseOrder"
}


const endpoints: Endpoint[] = [
    {
        lambda: LambdaCategory.item,
        path: "/items"
    }, {
        lambda: LambdaCategory.item,
        path: "/itemSearch"
    }, {
        lambda: LambdaCategory.item,
        path: "/item/{itemID}"
    }, {
        lambda: LambdaCategory.item,
        path: "/addItem"
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouses"
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouseSearch"
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouse/{warehouseID}"
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/addWarehouse"
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendors"
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendorSearch"
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendor/{vendorId}"
    }, {
        lambda: LambdaCategory.vendor,
        path: "/addVendor"
    }, {
        lambda: LambdaCategory.sale,
        path: "/salesHistory"
    }, {
        lambda: LambdaCategory.account,
        path: "/accountSettings"
    }, {
        lambda: LambdaCategory.account,
        path: "/updateAccount"
    }, {
        lambda: LambdaCategory.sale,
        path: "/makeSale"
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/suppliedItems"
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/suppliedItemSearch"
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/addSuppliedItem"
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/purchaseOrders"
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/purchaseOrderSearch"
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/addPurchaseOrder"
    },

]

export class ApiStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const cfIntegration = new HttpUrlIntegration("CfURLIntegration", "https://d1ldvqy0co6rs2.cloudfront.net/")
        const httpAPI = new HttpApi(this, "Api", {
            defaultIntegration: cfIntegration
        });
        //const endpointFunctions = JSON.parse(this.resolve(cdk.Fn.importValue("EndpointFunctionArns")));
        const loginEndpoint = Function.fromFunctionArn(this, "login", "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-login68DC89FC-Pa2QdzGh816V")

        const lambdaHttpIntegration = new HttpLambdaIntegration("LoginLambdaIntegration", loginEndpoint)

        httpAPI.addRoutes({
            path: "/login/hello",
            methods: [HttpMethod.GET],
            integration: lambdaHttpIntegration
        });


        httpAPI.addRoutes({
            path: "/",
            methods: [HttpMethod.GET],
            integration: cfIntegration
        })


        const files = globSync("../frontend/build/**/*");
        const staticEndpoints = files
            .map(value => Path.relative("../frontend/build/", value))
            .map(path => Path.parse(path))
            .map(path => {
                path.dir = toPosix(path.dir);
                return path;
            })
            .filter(value => value.ext !== "")
            .map(value => value.dir + Path.posix.sep + value.base)
            .map(value => {
                if (!value.startsWith("/")) {
                    return `/${value}`
                }
                return value
            });

        console.log(staticEndpoints)


        const categories = Object.keys(LambdaCategory)
            .map(key => LambdaCategory[key as keyof typeof LambdaCategory])
            .filter(value => typeof value === 'string') as string[];
        const arns = categories
            .map(value => `${value}ARN`)
            .map(value => Fn.importValue(value))
        // .map(value => this.resolve(value));

        let integrations: any = categories.map((value, index) => {
            return {
                [value as LambdaCategory]: new HttpLambdaIntegration(`${value}Integration`, Function.fromFunctionArn(this, `${value}Fn`, arns[index])),

            }
        }).reduce((previousValue, currentValue) => Object.assign(previousValue, currentValue))
        console.log(LambdaCategory)
        console.log(categories)
        console.log(arns)
        console.log(integrations)

        httpAPI.addRoutes({
            path: "/items2",
            methods: [HttpMethod.GET],
            integration: new HttpLambdaIntegration(`Integration`, Function.fromFunctionArn(this, "items2", "arn:aws:lambda:us-east-2:240617664661:function:LambdaStack-itemDD1DC579-6vyJV0FK8fkI"))
        });

        endpoints.forEach(value =>
            httpAPI.addRoutes({
                path: value.path,
                methods: [HttpMethod.GET],
                integration: integrations[value.lambda]
            })
        )
    }
}

function toPosix(path: string): string {
    return path.split(Path.sep).join(Path.posix.sep)
}