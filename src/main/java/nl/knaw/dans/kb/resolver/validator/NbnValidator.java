package nl.knaw.dans.kb.resolver.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@FacesValidator("nl.knaw.dans.kb.resolver.validator.NbnValidator")
public class NbnValidator implements Validator {

  private static final String NBN_PATTERN = "^[uU][rR][nN]:[nN][bB][nN]:[nN][lL](:([a-zA-Z]{2}))?:\\d{2}-.+";
  private Pattern pattern;
  private Matcher matcher;

  public NbnValidator() {
    pattern = Pattern.compile(NBN_PATTERN);
  }

  @Override
  public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

    matcher = pattern.matcher(value.toString());
    if (!matcher.matches()) {

      FacesMessage msg = new FacesMessage("urn:nbn-identifier validation failed.", "Invalid URI format.");
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    }

  }
}
