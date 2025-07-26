package com.myorg;

import software.amazon.awscdk.App;
import software.amazon.awscdk.Environment;
import software.amazon.awscdk.RemovalPolicy;
import software.amazon.awscdk.Stack;
import software.amazon.awscdk.StackProps;
import software.amazon.awscdk.CfnOutput;
import software.constructs.Construct;

import software.amazon.awscdk.services.s3.Bucket;
import software.amazon.awscdk.services.s3.BucketProps;
import software.amazon.awscdk.services.s3.BlockPublicAccess;
import software.amazon.awscdk.services.s3.EventType;

import software.amazon.awscdk.services.dynamodb.Attribute;
import software.amazon.awscdk.services.dynamodb.AttributeType;
import software.amazon.awscdk.services.dynamodb.Table;
import software.amazon.awscdk.services.dynamodb.TableProps;

import software.amazon.awscdk.services.lambda.Runtime;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSource;
import software.amazon.awscdk.services.lambda.eventsources.S3EventSourceProps;
import software.amazon.awscdk.services.iam.ManagedPolicy;

import software.amazon.awscdk.services.lambda.Function;
import software.amazon.awscdk.services.lambda.Code;
import software.amazon.awscdk.services.lambda.FunctionProps;

import java.util.List;
import java.util.Map;

public class TextractInvoiceProcessingStack extends Stack {
    public TextractInvoiceProcessingStack(final Construct scope, final String id, final StackProps props) {
        super(scope, id, props);

        Bucket bucket = new Bucket(this, "invoice-images-input-bucket", BucketProps.builder()
                .blockPublicAccess(BlockPublicAccess.BLOCK_ALL)
                .removalPolicy(RemovalPolicy.DESTROY)
                .autoDeleteObjects(true)
                .build());

        Table table = new Table(this, "invoice-output-table", TableProps.builder()
                .partitionKey(Attribute.builder()
                        .name("source_file")
                        .type(AttributeType.STRING)
                        .build())
                .tableName(bucket.getBucketName() + "_invoice_output")
                .removalPolicy(RemovalPolicy.DESTROY)
                .build());

        Function function = new Function(this, "textract-function", FunctionProps.builder()
                .runtime(Runtime.GO_1_X)
                .handler("main") // Adjust based on your Go Lambda's handler
                .code(Code.fromAsset("../function")) // Path to compiled Go binary
                .environment(Map.of("TABLE_NAME", table.getTableName()))
                .build());

        table.grantWriteData(function);
        bucket.grantRead(function);

        function.getRole().addManagedPolicy(
                ManagedPolicy.fromAwsManagedPolicyName("AmazonTextractFullAccess")
        );

        function.addEventSource(new S3EventSource(bucket, S3EventSourceProps.builder()
                .events(List.of(EventType.OBJECT_CREATED))
                .build()));

        CfnOutput.Builder.create(this, "invoice-input-bucket-name")
                .exportName("invoice-input-bucket-name")
                .value(bucket.getBucketName())
                .build();

        CfnOutput.Builder.create(this, "invoice-output-table-name")
                .exportName("invoice-output-table-name")
                .value(table.getTableName())
                .build();
    }

    public static void main(final String[] args) {
        App app = new App();

        new TextractInvoiceProcessingStack(app, "TextractInvoiceProcessingJavaStack", StackProps.builder()
                .env(Environment.builder().build()) // Replace with .account()/.region() as needed
                .build());

        app.synth();
    }
}
