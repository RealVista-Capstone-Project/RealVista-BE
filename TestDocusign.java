import com.docusign.esign.model.*;

public class TestDocusign {
    public static void main(String[] args) {
        TemplateRole role = new TemplateRole();
        role.setRequireIdLookup("true");
        role.setIdCheckConfigurationName("SMS Auth $");
        RecipientSMSAuthentication auth = new RecipientSMSAuthentication();
        role.setSmsAuthentication(auth);
        System.out.println("Methods exist!");
    }
}
