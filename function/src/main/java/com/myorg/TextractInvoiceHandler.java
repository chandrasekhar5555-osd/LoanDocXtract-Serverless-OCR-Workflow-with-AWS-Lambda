package com.myorg;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.*;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;

import java.util.HashMap;
import java.util.Map;

public class TextractInvoiceHandler implements RequestHandler<S3Event, Void> {

    private final TextractClient textractClient;
    private final DynamoDbClient dynamoDbClient;
    private final String tableName;

    public TextractInvoiceHandler() {
        this.textractClient = TextractClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv("AWS_REGION")))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();

        this.tableName = System.getenv("TABLE_NAME");

        if (this.tableName == null || this.tableName.isEmpty()) {
            throw new RuntimeException("Missing TABLE_NAME environment variable");
        }
    }

    @Override
    public Void handleRequest(S3Event s3Event, Context context) {
        s3Event.getRecords().forEach(record -> {
            String bucketName = record.getS3().getBucket().getName();
            String objectKey = record.getS3().getObject().getKey();

            System.out.printf("Processing file: %s from bucket: %s%n", objectKey, bucketName);

            try {
                processInvoice(bucketName, objectKey);
            } catch (Exception e) {
                System.err.printf("Error processing %s: %s%n", objectKey, e.getMessage());
                throw new RuntimeException(e);
            }
        });
        return null;
    }

    private void processInvoice(String bucket, String key) {
        AnalyzeExpenseRequest request = AnalyzeExpenseRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder()
                                .bucket(bucket)
                                .name(key)
                                .build())
                        .build())
                .build();

        AnalyzeExpenseResponse response = textractClient.analyzeExpense(request);

        for (ExpenseDocument doc : response.expenseDocuments()) {
            Map<String, AttributeValue> item = new HashMap<>();
            item.put("source_file", AttributeValue.builder().s(key).build());

            for (ExpenseField field : doc.summaryFields()) {
                if (field.type() != null && field.type().text() != null && field.valueDetection() != null) {
                    String fieldType = field.type().text();
                    String fieldValue = field.valueDetection().text();

                    switch (fieldType) {
                        case "INVOICE_RECEIPT_ID":
                            item.put("receipt_id", AttributeValue.builder().s(fieldValue).build());
                            break;
                        case "TOTAL":
                            item.put("total", AttributeValue.builder().s(fieldValue).build());
                            break;
                        case "INVOICE_RECEIPT_DATE":
                            item.put("receipt_date", AttributeValue.builder().s(fieldValue).build());
                            break;
                        case "DUE_DATE":
                            item.put("due_date", AttributeValue.builder().s(fieldValue).build());
                            break;
                        default:
                            break;
                    }
                }
            }

            PutItemRequest putReq = PutItemRequest.builder()
                    .tableName(tableName)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(putReq);
            System.out.println("Invoice record stored in DynamoDB.");
        }
    }
}
