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
package br.com.surittec.surifaces.listener;

import java.util.List;
import java.util.Map;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseEvent;
import javax.faces.event.PhaseId;
import javax.faces.event.PhaseListener;

import br.com.surittec.surifaces.util.FacesUtils;

/**
 * Enables messages to be rendered on different pages from which they were set.
 *
 * After each phase where messages may be added, this moves the messages from
 * the page-scoped FacesContext to the session-scoped session map.
 *
 * Before messages are rendered, this moves the messages from the session-scoped
 * session map back to the page-scoped FacesContext.
 *
 * Only global messages, not associated with a particular component, are moved.
 * Component messages cannot be rendered on pages other than the one on which
 * they were added.
 *
 * To enable multi-page messages support, add a <code>lifecycle</code> block to
 * your faces-config.xml file. That block should contain a single
 * <code>phase-listener</code> block containing the fully-qualified classname of
 * this file.
 *
 * @author Jesse Wilson jesse[AT]odel.on.ca
 * @secondaryAuthor Lincoln Baxter III lincoln[AT]ocpsoft.com
 */
public class MultiPageMessagesListener implements PhaseListener {

	private static final long serialVersionUID = 1L;

	public static final String sessionToken = "MULTI_PAGE_MESSAGES_SUPPORT";

	@Override
	public PhaseId getPhaseId() {
		return PhaseId.ANY_PHASE;
	}

	/*
	 * Check to see if we are "naturally" in the RENDER_RESPONSE phase. If we
	 * have arrived here and the response is already complete, then the page is
	 * not going to show up: don't display messages yet.
	 */
	@Override
	public void beforePhase(final PhaseEvent event) {
		FacesUtils.saveMessages();

		if (PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
			FacesContext facesContext = event.getFacesContext();
			if (!facesContext.getResponseComplete()) {
				this.restoreMessages(facesContext);
			}
		}
	}

	/*
	 * Save messages into the session after every phase.
	 */
	@Override
	public void afterPhase(final PhaseEvent event) {
		if (!PhaseId.RENDER_RESPONSE.equals(event.getPhaseId())) {
			FacesUtils.saveMessages();
		}
	}
	
	@SuppressWarnings("unchecked")
	private int restoreMessages(final FacesContext facesContext) {
		Map<String, Object> sessionMap = facesContext.getExternalContext().getSessionMap();
		List<FacesMessage> messages = (List<FacesMessage>) sessionMap.remove(sessionToken);

		if (messages == null) {
			return 0;
		}

		int restoredCount = messages.size();
		for (Object element : messages) {
			facesContext.addMessage(null, (FacesMessage) element);
		}
		return restoredCount;
	}

}
