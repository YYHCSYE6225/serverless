package com;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

public class SendEmail implements RequestHandler<SNSEvent, Object> {
    // Replace sender@example.com with your "From" address.
    // This address must be verified with Amazon SES.
    static final String FROM = "no-reply@prod.joci.me";

    // The subject line for the email.
    static final String SUBJECT = "Email address verification";

    static final BasicAWSCredentials awsCreds = new BasicAWSCredentials("AKIATWM5J7ATTTV7DDQ3", "iSKeQ+ShXXUa6BjmPGNgqj8njIMKiN73AkJo7R3c");

    static final AWSCredentialsProvider awsCredentialsProvider = new AWSStaticCredentialsProvider(awsCreds);

    @Override
    public Object handleRequest(SNSEvent snsEvent, Context context) {
        String message=snsEvent.getRecords().get(0).getSNS().getMessage();
        String[] contents=message.split("_");
        String emailAddress=contents[1];

        String token=contents[3];

        String verifyLink="https://prod.joci.me/v1/user/verifyUserEmail/"+emailAddress+"/"+token;

        // Replace recipient@example.com with a "To" address. If your account
        // is still in the sandbox, this address must be verified.
        String TO = emailAddress;
        // The email body for recipients with non-HTML email clients.
        String TEXTBODY = "This email was sent to verify the email address, please use this link to "
                + "verify your address. Link: "+verifyLink;
        try {
            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard()
                            // Replace US_WEST_2 with the AWS Region you're using for
                            // Amazon SES.
                            .withRegion(Regions.US_EAST_1)
                            .withCredentials(awsCredentialsProvider)
                            .build();
            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(TO))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData(TEXTBODY)))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(SUBJECT)))
                    .withSource(FROM);

            client.sendEmail(request);
            System.out.println("Email sent!");
        } catch (Exception ex) {
            System.out.println("The email was not sent. Error message: "
                    + ex.getMessage());
        }
        return null;
    }
}
