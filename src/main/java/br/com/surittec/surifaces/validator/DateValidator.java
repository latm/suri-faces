/*
 * SURITTEC
 * Copyright 2015, TTUS TECNOLOGIA DA INFORMACAO LTDA, 
 * and individual contributors as indicated by the @authors tag
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package br.com.surittec.surifaces.validator;

import java.util.Calendar;
import java.util.Date;

import javax.faces.application.FacesMessage;
import javax.faces.component.EditableValueHolder;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import br.com.surittec.surifaces.util.FacesUtils;

@FacesValidator("br.com.surittec.surifaces.validator.DateValidator")
public class DateValidator implements Validator {

	private static final String BEFORE_THEN_ATTR = "beforeThen";
	private static final String BEFORE_THEN_MESSAGE_ATTR = "beforeThenMessage";
	private static final String BEFORE_EQUALS_THEN_ATTR = "beforeEqualsThen";
	private static final String BEFORE_EQUALS_THEN_MESSAGE_ATTR = "beforeEqualsThenMessage";

	private static final String AFTER_THEN_ATTR = "afterThen";
	private static final String AFTER_THEN_MESSAGE_ATTR = "afterThenMessage";
	private static final String AFTER_EQUALS_THEN_ATTR = "afterEqualsThen";
	private static final String AFTER_EQUALS_THEN_MESSAGE_ATTR = "afterEqualsThenMessage";

	@Override
	public void validate(FacesContext context, UIComponent component, Object value) throws ValidatorException {
		if (value == null)
			return;

		Date date = (Date) value;

		validateRange(component, date, BEFORE_THEN_ATTR, BEFORE_THEN_MESSAGE_ATTR, true, false);
		validateRange(component, date, BEFORE_EQUALS_THEN_ATTR, BEFORE_EQUALS_THEN_MESSAGE_ATTR, true, true);
		validateRange(component, date, AFTER_THEN_ATTR, AFTER_THEN_MESSAGE_ATTR, false, false);
		validateRange(component, date, AFTER_EQUALS_THEN_ATTR, AFTER_EQUALS_THEN_MESSAGE_ATTR, false, true);

		Calendar sqlServerEarlier = Calendar.getInstance();
		Calendar sqlServerAfter = Calendar.getInstance();
		sqlServerEarlier.set(1752, 11, 31, 23, 59);
		sqlServerAfter.set(9999, 11, 31, 23, 59);
		if (date.before(sqlServerEarlier.getTime()) || date.after(sqlServerAfter.getTime())) {
			String label = (String) component.getAttributes().get("label");
			if (label != null && !label.trim().equals("")) {
				throw new ValidatorException(FacesUtils.createMessage(FacesMessage.SEVERITY_ERROR, "javax.faces.validator.Date.detail", label));
			} else {
				throw new ValidatorException(FacesUtils.createMessage(FacesMessage.SEVERITY_ERROR, "javax.faces.validator.Date"));
			}
		}
	}

	private void validateRange(UIComponent component, Date date, String referenceAttr, String messageAttr, boolean before, boolean equals) {
		String field = (String) component.getAttributes().get(referenceAttr);
		if (field != null && !isValid(component, date, field, before, equals)) {
			String message = (String) component.getAttributes().get(messageAttr);
			if (message != null && !message.trim().equals("")) {
				throw new ValidatorException(FacesUtils.createMessage(FacesMessage.SEVERITY_ERROR, message));
			}

			String label = (String) component.getAttributes().get("label");
			if (label != null && !label.trim().equals("")) {
				throw new ValidatorException(FacesUtils.createMessage(FacesMessage.SEVERITY_ERROR, "javax.faces.validator.Date.detail", label));
			} else {
				throw new ValidatorException(FacesUtils.createMessage(FacesMessage.SEVERITY_ERROR, "javax.faces.validator.Date"));
			}
		}
	}

	private boolean isValid(UIComponent component, Date value, String then, boolean before, boolean equals) {

		Date other = null;

		if (then.equalsIgnoreCase("today") || then.equalsIgnoreCase("tomorrow") || then.equalsIgnoreCase("yesterday")) {

			Calendar calendar = Calendar.getInstance();
			if (before) {
				calendar.set(Calendar.HOUR_OF_DAY, 0);
				calendar.set(Calendar.MINUTE, 0);
				calendar.set(Calendar.SECOND, 0);
				calendar.set(Calendar.MILLISECOND, 0);
			} else {
				calendar.set(Calendar.HOUR_OF_DAY, 23);
				calendar.set(Calendar.MINUTE, 59);
				calendar.set(Calendar.SECOND, 59);
				calendar.set(Calendar.MILLISECOND, 999);
			}

			if (then.equalsIgnoreCase("tomorrow"))
				calendar.add(Calendar.DAY_OF_MONTH, 1);
			else if (then.equalsIgnoreCase("yesterday"))
				calendar.add(Calendar.DAY_OF_MONTH, -1);

			other = calendar.getTime();

		} else {
			EditableValueHolder otherComponent = ((EditableValueHolder) component.findComponent(then));
			other = otherComponent.isValid() ? (Date) otherComponent.getLocalValue() : null;
		}

		if (other == null)
			return true;

		if (before)
			return equals ? !value.after(other) : value.before(other);
		else
			return equals ? !value.before(other) : value.after(other);
	}

}
