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
package org.fuin.ddd4j.eventstore.jpa;

import org.fuin.ddd4j.eventstore.intf.StreamId;

/**
 * Factory for that creates a stream based on a stream identifier.
 */
public interface IdStreamFactory {

    /**
     * Checks if the factory can create a stream based on a given stream
     * identifier.
     * 
     * @param streamId
     *            Identifier to create a stream for.
     * 
     * @return TRUE if a stream can be created using the identifier.
     */
    public boolean containsType(StreamId streamId);

    /**
     * Creates a new stream instance (without persisting it).
     * 
     * @param streamId
     *            Stream identifier to create an instance for.
     * 
     * @return New object.
     */
    public Stream createStream(StreamId streamId);

}
