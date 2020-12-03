/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

  public static final String NBN_PATTERN = "^[uU][rR][nN]:[nN][bB][nN]:[nN][lL](:([a-zA-Z]{2}))?:\\d{2}-.+";
  private Pattern pattern;
  private Matcher matcher;

  public NbnValidator() {
    pattern = Pattern.compile(NBN_PATTERN);
  }

  @Override
  public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {

    matcher = pattern.matcher(value.toString());
    if (!matcher.matches()) {

      FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Given nbn-identifier is not valid.", value.toString());
      msg.setSeverity(FacesMessage.SEVERITY_ERROR);
      throw new ValidatorException(msg);
    }

  }
}
