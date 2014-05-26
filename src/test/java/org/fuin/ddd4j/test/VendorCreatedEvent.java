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

import javax.validation.constraints.NotNull;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.fuin.ddd4j.ddd.AbstractDomainEvent;
import org.fuin.ddd4j.ddd.EntityIdPath;
import org.fuin.ddd4j.ddd.EventType;
import org.fuin.objects4j.common.NeverNull;

/**
 * A vendor entity was created.
 */
@XmlRootElement(name = "vendor-created-event")
public final class VendorCreatedEvent extends AbstractDomainEvent<VendorId> {

    private static final long serialVersionUID = 1000L;

    /** Unique name of the event used to store it - Should never change. */
    public static final EventType TYPE = new EventType(
            VendorCreatedEvent.class.getSimpleName());

    @XmlElement(name = "vendor")
    private VendorRef vendorRef;

    /**
     * Default constructor only for deserialization.
     */
    protected VendorCreatedEvent() {
        super();
    }

    /**
     * Constructor with event data.
     * 
     * @param vendorRef
     *            Vendor reference.
     */
    public VendorCreatedEvent(@NotNull final VendorRef vendorRef) {
        super(new EntityIdPath(vendorRef.getId()));
        this.vendorRef = vendorRef;
    }

    @Override
    public final EventType getEventType() {
        return TYPE;
    }

    /**
     * Returns the vendor reference.
     * 
     * @return Vendor reference.
     */
    @NeverNull
    public final VendorRef getVendorRef() {
        return vendorRef;
    }

    @Override
    public final String toString() {
        return "Created vendor '" + vendorRef + "'";
    }

}
