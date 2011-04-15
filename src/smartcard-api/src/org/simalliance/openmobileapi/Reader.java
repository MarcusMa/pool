/*
 * Copyright 2011 Giesecke & Devrient GmbH.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.simalliance.openmobileapi;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Instances of this class represent Secure Element Readers connected to this
 * device. These Readers can be physical devices or virtual devices. They can be
 * removable or not. They can contain Secure Element that can or cannot be
 * removed.
 */
public class Reader {

	String _name;
	SEService _service;

	private final ArrayList<Session> _sessions = new ArrayList<Session>();

	Reader(String name, SEService service) {
		_name = name;
		_service = service;
	}

	/**
	 * The name of this Reader
	 * 
	 * @return name of this Reader
	 */
	public String getName() {
		return _name;
	}

	/**
	 * Connects to a secure element in this reader. <br>
	 * This method prepares (initializes) the Secure Element for communication
	 * before the Session object is returned (e.g. powers the Secure Element by
	 * ICC ON). There might be multiple sessions opened at the same time on the
	 * same reader. The system ensures the interleaving of APDUs between the
	 * respective sessions.
	 * 
	 * @throws IOException
	 *             if something went wrong with the communicating to the Secure
	 *             Element or the reader.
	 * 
	 * @return a Session object to be used to create Channels.
	 */
	public Session openSession() throws IOException {

		synchronized (_sessions) {
			Session session = new Session(getName(), this);
			_sessions.add(session);
			return session;
		}
	}

	/**
	 * Check if a Secure Element is present in this reader.
	 * 
	 * @return <code>true</code> if the SE is present.
	 */
	public boolean isSecureElementPresent() {

		return _service.isSecureElementPresent(this);
	}

	/**
	 * Return the Secure Element service this reader is bound to.
	 * 
	 * @return the SEService object.
	 */
	public SEService getSEService() {
		return _service;
	}

	/**
	 * Close all the sessions opened on this reader. All the channels opened by
	 * all these sessions will be closed.
	 */
	public void closeSessions() {
		synchronized (_sessions) {

			for (Session session : _sessions)
				closeSession(session);
		}
	}

	// ******************************************************************
	// package private methods
	// ******************************************************************

	/**
	 * Closes the defined Session and all its allocated resources. <br>
	 * After calling this method the Session can not be used for the
	 * communication with the Secure Element any more.
	 * 
	 * @param session
	 *            the Session that should be closed
	 * 
	 * @throws NullPointerException
	 *             if Session is null
	 */
	void closeSession(Session session) {
		if (session == null)
			throw new NullPointerException("session is null");

		synchronized (_sessions) {

			if (!session.isClosed()) {
				session.setClosed();
			}
			_sessions.remove(session);
		}
	}

}