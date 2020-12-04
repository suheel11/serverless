package com.aws.service.handler;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;


public class LogEvent implements RequestHandler<SNSEvent,Object> {

    static final String domain= "api.prod.suheel.me";
    static final String dynamoTable ="csye6225";

    static final String FROM = "no-reply@api.prod.suheel.me";



    DynamoDB dynamoDB;


    public String handleRequest(SNSEvent request, Context context) {
        //logger.info("logger------ inside handle request");
        String timeStamp = new SimpleDateFormat("yyyy-MM-dd_HH:mm:ss").format(Calendar.getInstance().getTime());
        long now = Calendar.getInstance().getTimeInMillis()/1000;
        long TTL = 15 * 60;
        long totalTTL = TTL + now ;
        try {
            //logger.info("logger ------ trying to connect to dynamodb");
            context.getLogger().log("trying to connect to dynamodb");
            long ttlDBValue = 0;
            context.getLogger().log("Before init");
            init();
            context.getLogger().log("after init");
            //long unixTime = Instant.now().getEpochSecond()+15*60;
            Table table = dynamoDB.getTable(dynamoTable);


            String To = request.getRecords().get(0).getSNS().getMessage();
            //logger.info("Request"+To);
            //Map<String,String> m = new HashMap<String, String>();
            //m.put("id",To);
            //table.putItem((Item) m);
            context.getLogger().log("Request"+To);

//            Item item = table.getItem("id", To);
//            if (item != null) {
//                //logger.info("Checking for time stamp");
//                context.getLogger().log("Checking for timestamp");
//                ttlDBValue = item.getLong("TimeToLive");
//
//            }
//            if(item==null|| (ttlDBValue < now && ttlDBValue != 0)) {
//
//                Item itemPut = new Item()
//                        .withPrimaryKey("id", To)
//                        .withString("token", context.getAwsRequestId())
//                        .withNumber("TimeToLive", totalTTL);
//
//                //logger.info("inside email");
//                context.getLogger().log("inside email");
//                table.putItem(itemPut);

                try {
                    String TO = request.getRecords().get(0).getSNS().getMessage();
                    String token = context.getAwsRequestId();
                    AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.defaultClient();
                    SendEmailRequest req = new SendEmailRequest().withDestination(new Destination().withToAddresses(TO))
                            .withMessage(new Message().withBody(new Body().withHtml(new Content().withCharset("UTF-8").withData("Please click on the below link to view the question<br/>"+ "<p><a href='#'>http://"+domain+"/questions</a></p>")))
                                    .withSubject(
                                            new Content().withCharset("UTF-8")
                                                    .withData("View Question data for Webapp")))
                            .withSource(FROM);
                    client.sendEmail(req);
                    context.getLogger().log ("Email sent!");
                } catch (Exception ex) {
                    context.getLogger().log ("The email was not sent. Error message: "
                            + ex.getMessage());
                }
            //}
        }
        catch(AmazonServiceException ase){
            //logger.error("Could not complete operation");
            context.getLogger().log("Could not complete operation");
            context.getLogger().log("Error Message: " + ase.getMessage());
            context.getLogger().log("HTTP Status: " + ase.getStatusCode());
            context.getLogger().log("AWS Error Code: " + ase.getErrorCode());
            context.getLogger().log("Error Type: " + ase.getErrorType());
            context.getLogger().log("Request ID: " + ase.getRequestId());
        }
        catch (AmazonClientException ace) {
            context.getLogger().log("Internal error occured communicating with DynamoDB");
            context.getLogger().log("Error Message: " + ace.getMessage());
        }
        catch(Exception e){
            context.getLogger().log("this is the exception"+e);
        }

        context.getLogger().log("Invocation completed: " + timeStamp);

        return null;

    }

    private void init() {
        AmazonDynamoDB aDBclient = AmazonDynamoDBClientBuilder.defaultClient();
        dynamoDB = new DynamoDB(aDBclient);


    }


}