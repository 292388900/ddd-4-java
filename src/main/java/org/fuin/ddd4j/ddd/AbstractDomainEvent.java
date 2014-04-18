/**
 * Copyright (C) 2013 Future Invent Informationsmanagement GmbH. All rights
 * reserved. <http://www.fuin.org/>
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 3 of the License, or (at your option) any
 * later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this library. If not, see <http://www.gnu.org/licenses/>.
 */
package org.fuin.ddd4j.ddd;

import javax.xml.bind.annotation.XmlAttribute;

/**
 * Base class for domain events.
 * 
 * @param <ID>
 *            Type of the entity identifier.
 */
public abstract class AbstractDomainEvent<ID extends EntityId> extends
		AbstractEvent implements DomainEvent<ID> {

	private static final long serialVersionUID = 1000L;

	@XmlAttribute(name = "entity-id-path")
	private EntityIdPath entityIdPath;

	/**
	 * Protected default constructor for deserialization.
	 */
	protected AbstractDomainEvent() {
		super();
	}

	/**
	 * Constructor with entity identifier path.
	 * 
	 * @param entityIdPath
	 *            Identifier path from aggregate root to the entity that emitted
	 *            the event.
	 */
	public AbstractDomainEvent(final EntityIdPath entityIdPath) {
		super();
		this.entityIdPath = entityIdPath;
	}

	@Override
	public final EntityIdPath getEntityIdPath() {
		return entityIdPath;
	}

	@Override
	public final ID getEntityId() {
		return entityIdPath.last();
	}

}
