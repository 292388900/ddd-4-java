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
package org.fuin.ddd4j.test;

import java.util.ArrayList;
import java.util.List;

import org.fuin.ddd4j.ddd.AbstractAggregateRoot;
import org.fuin.ddd4j.ddd.AbstractDomainEvent;
import org.fuin.ddd4j.ddd.ChildEntityLocator;
import org.fuin.ddd4j.ddd.EntityType;
import org.fuin.ddd4j.ddd.EventHandler;

// CHECKSTYLE:OFF
public class ARoot extends AbstractAggregateRoot<AId> {

	private AId id;

	private List<BEntity> childs;

	private AbstractDomainEvent<?> lastEvent;

	public ARoot() {
		super();
	}
	
	public ARoot(AId id) {
		super();
		apply(new ACreatedEvent(id));
	}

	@Override
	public AId getId() {
		return id;
	}

	@Override
	public EntityType getType() {
		return AId.TYPE;
	}

	@ChildEntityLocator
	private BEntity find(BId bid) {
		for (BEntity child : childs) {
			if (child.getId().equals(bid)) {
				return child;
			}
		}
		return null;
	}

	public void addB(BId bid) {
		apply(new BAddedEvent(id, bid));
	}

	public void addC(BId bid, CId cid) {
		final BEntity found = find(bid);
		found.add(cid);
	}
	
	public void doItC(BId bid, CId cid) {
		final BEntity found = find(bid);
		found.doIt(cid);
	}
	
	@EventHandler
	public void handle(ACreatedEvent event) {
		this.id = event.getId();
		this.childs = new ArrayList<BEntity>();
		lastEvent = event;
	}
	
	@EventHandler
	public void handle(BAddedEvent event) {
		childs.add(new BEntity(this, event.getBId()));
		lastEvent = event;
	}

	public AbstractDomainEvent<?> getLastEvent() {
		return lastEvent;
	}

	public BEntity getFirstChild() {
		return childs.get(0);
	}
	
}
// CHECKSTYLE:OFF
