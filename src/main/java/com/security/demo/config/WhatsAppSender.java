package com.security.demo.config;

import com.security.demo.DBmodel.MatchInfoEntity;
import com.security.demo.repo.Matchrepo;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WhatsAppSender {

    // 🔐 Replace these with your actual credentials
    public static final String ACCOUNT_SID = "AC318f0bf0ac507e62fe43ade7bc1a0274";
    public static final String AUTH_TOKEN = "23591c955315025fcc52269e99f688f5";
    private static final String FROM        = "whatsapp:+14155238886";
    public WhatsAppSender(){
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }
    @Autowired
    Matchrepo matchrepo;

    // Simple text message
    public  void sendTextMessage(String toNumber, String body) {
        try {
            Message message = Message.creator(
                    new PhoneNumber("whatsapp:+" + toNumber),
                    new PhoneNumber(FROM),
                    body
            ).create();

            System.out.println("✅ Text Message Sent! SID: " + message.getSid());

        } catch (Exception e) {
            System.out.println("❌ Failed to send text: " + e.getMessage());
        }
    }
    public static String APP_URL= "https://fantasy-frontend-loqr.vercel.app/";
    public String generateString(Integer matchid) {
       MatchInfoEntity m = matchrepo.findById(matchid).get();
        String variables = String.format(
                "{\"1\":\"%s\",\"2\":\"%s\",\"3\":\"%s\",\"4\":\"%s\",\"5\":\"%s\"}",
                "IPL 2026 " + m.getMatchDesc(),
                m.getTeam1().getTeamSName(),
                m.getTeam2().getTeamSName(),
                m.getStatus(),
                m.getVenueInfo().getGround()// ← your vercel URL here
        );
        return variables;
    }
    // Template message
    public  void sendTemplateMessage(String toNumber , Integer matchid) {
        try {
            Message message = Message.creator(
                            new PhoneNumber("whatsapp:+" + toNumber),
                            new PhoneNumber(FROM)
                    ,"IPL 2026"

                    )
                    .setContentSid("HXc020d0143a314171a6f12ca89443cd92" )
                    .setContentVariables(generateString(matchid))
                    .create();

            System.out.println("✅ Template Message Sent! SID: " + message.getSid());

        } catch (Exception e) {
            System.out.println("❌ Failed to send template: " + e.getMessage());
        }
    }
}