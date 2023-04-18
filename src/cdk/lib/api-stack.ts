import * as cdk from "aws-cdk-lib";
import {Fn, StackProps} from "aws-cdk-lib";
import {Construct} from "constructs";
import {HttpApi, HttpMethod} from "@aws-cdk/aws-apigatewayv2-alpha";
import {HttpLambdaIntegration, HttpUrlIntegration} from "@aws-cdk/aws-apigatewayv2-integrations-alpha";
import {Function} from "aws-cdk-lib/aws-lambda";
import {globSync} from "glob";
import * as Path from "path";

interface Endpoint {
    lambda: LambdaCategory,
    path: string,
    methods: HttpMethod []
}

enum LambdaCategory {
    item = "item",
    warehouse = "warehouse",
    vendor = "vendor",
    sale = "sale",
    account = "account",
    suppliedItem = "suppliedItem",
    purchaseOrder = "purchaseOrder",
    login = "login",
    optimizer = "optimizer"
}


const endpoints: Endpoint[] = [
    {
        lambda: LambdaCategory.item,
        path: "/items",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.item,
        path: "/itemSearch",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.item,
        path: "/item/{itemID}",
        methods: [HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE]
    }, {
        lambda: LambdaCategory.item,
        path: "/addItem",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouses",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouseSearch",
        methods: [HttpMethod.PUT]
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/warehouse/{warehouseID}",
        methods: [HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE]
    }, {
        lambda: LambdaCategory.warehouse,
        path: "/addWarehouse",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendors",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendorSearch",
        methods: [HttpMethod.PUT]
    }, {
        lambda: LambdaCategory.vendor,
        path: "/vendor/{vendorId}",
        methods: [HttpMethod.GET, HttpMethod.PUT, HttpMethod.DELETE]
    }, {
        lambda: LambdaCategory.vendor,
        path: "/addVendor",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.sale,
        path: "/salesHistory",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.account,
        path: "/accountSettings",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.account,
        path: "/updateAccount",
        methods: [HttpMethod.PUT]
    }, {
        lambda: LambdaCategory.sale,
        path: "/makeSale",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/suppliedItems",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/suppliedItemSearch",
        methods: [HttpMethod.PUT]
    }, {
        lambda: LambdaCategory.suppliedItem,
        path: "/addSuppliedItem",
        methods: [HttpMethod.POST]
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/purchaseOrders",
        methods: [HttpMethod.GET]
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/purchaseOrderSearch",
        methods: [HttpMethod.PUT]
    }, {
        lambda: LambdaCategory.purchaseOrder,
        path: "/addPurchaseOrder",
        methods: [HttpMethod.POST]
    },{
        lambda: LambdaCategory.login,
        path: "/login",
        methods: [HttpMethod.POST]
    },{
        lambda: LambdaCategory.login,
        path: "/logout",
        methods: [HttpMethod.POST]
    },
];

export class ApiStack extends cdk.Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
        super(scope, id, props);

        const cfIntegration = new HttpUrlIntegration("CfURLIntegration", "https://d1ldvqy0co6rs2.cloudfront.net/")
        const httpAPI = new HttpApi(this, "Api", {
            defaultIntegration: cfIntegration
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

        let integrations: any = categories.map((value, index) => {
            return {
                [value as LambdaCategory]: new HttpLambdaIntegration(`${value}Integration`, Function.fromFunctionArn(this, `${value}Fn`, arns[index])),
            }
        }).reduce((previousValue, currentValue) => Object.assign(previousValue, currentValue))

        endpoints.forEach(value =>
            httpAPI.addRoutes({
                path: value.path,
                methods: value.methods,
                integration: integrations[value.lambda]
            })
        )
    }
}

function toPosix(path: string): string {
    return path.split(Path.sep).join(Path.posix.sep)
}