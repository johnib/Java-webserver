package Root;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.factory.MessageFactory;
import com.twilio.sdk.resource.instance.Message;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jonathan Rubin Yaniv and Nitsan Bracha on 10/02/2016.
 * Copyright (c) 2015 Jonathan Yaniv and Nitsan Bracha . All rights reserved.
 */
public class SmsSender {
    // Find your Account Sid and Token at twilio.com/user/account
    public static final String ACCOUNT_SID = "ACa2daa1acae98b1f6f701fc933fd0a450";
    public static final String AUTH_TOKEN = "a2e3108a4e4e7b6cacafd8a7f9229ead";

    public static void sendSms(String number, CrawlerResult result) {
        try {
            Logger.writeVerbose("Sending Sms Start.");
            TwilioRestClient client = new TwilioRestClient(ACCOUNT_SID, AUTH_TOKEN);


            // Build the parameters
            List<NameValuePair> params = new ArrayList<>();
            params.add(new BasicNameValuePair("To", number));
            params.add(new BasicNameValuePair("From", "+16466307208"));
            params.add(new BasicNameValuePair("Body", result.toString()));

            MessageFactory messageFactory = client.getAccount().getMessageFactory();
            Message message = messageFactory.create(params);
            Logger.writeVerbose("Sms sent, GUID:" + message.getSid());
        } catch (Exception ex) {
            Logger.writeException(ex);
        }
    }
}
