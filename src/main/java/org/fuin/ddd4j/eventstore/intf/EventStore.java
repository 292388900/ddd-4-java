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
package org.fuin.ddd4j.eventstore.intf;

import java.io.Closeable;
import java.util.List;

import javax.validation.constraints.NotNull;

/**
 * Stores events.
 */
// CHECKSTYLE:OFF:RedundantThrows
public interface EventStore extends Closeable {

	/**
	 * Opens a connection to the repository.
	 */
	public void open();

	/**
	 * Closes the connection to the repository.
	 */
	public void close();

	/**
	 * Deletes a stream from the event store.
	 * 
	 * @param streamId
	 *            The unique identifier of the stream to be deleted
	 * @param expectedVersion
	 *            The version the stream should have when being deleted.
	 * 
	 * @throws StreamNotFoundException
	 *             A stream with the given name does not exist in the
	 *             repository.
	 * @throws StreamDeletedException
	 *             A stream with the given name previously existed but was
	 *             deleted.
	 * @throws StreamVersionConflictException
	 *             The expected version didn't match the actual version.
	 */
	public void deleteStream(@NotNull StreamId streamId, int expectedVersion)
			throws StreamNotFoundException, StreamVersionConflictException,
			StreamDeletedException;

	/**
	 * Appends a list of events to a stream.
	 * 
	 * @param streamId
	 *            The unique identifier of the stream to append the events to.
	 * @param expectedVersion
	 *            The version the stream should have.
	 * @param events
	 *            List of events to write to the stream
	 * 
	 * @return The next expected version for the stream.
	 * 
	 * @throws StreamNotFoundException
	 *             The stream does not exist in the repository.
	 * @throws StreamDeletedException
	 *             The stream previously existed but was deleted.
	 * @throws StreamVersionConflictException
	 *             The expected version didn't match the actual version.
	 */
	public int appendToStream(@NotNull StreamId streamId, int expectedVersion,
			@NotNull List<EventData> events) throws StreamNotFoundException,
			StreamVersionConflictException, StreamDeletedException;

	/**
	 * Appends one or more events to a stream.
	 * 
	 * @param streamId
	 *            The unique identifier of the stream to append the events to.
	 * @param expectedVersion
	 *            The version the stream should have.
	 * @param events
	 *            Array of events to write to the stream
	 * 
	 * @return The next expected version for the stream.
	 * 
	 * @throws StreamNotFoundException
	 *             A stream with the given name does not exist in the
	 *             repository.
	 * @throws StreamDeletedException
	 *             A stream with the given name previously existed but was
	 *             deleted.
	 * @throws StreamVersionConflictException
	 *             The expected version didn't match the actual version.
	 */
	public int appendToStream(@NotNull StreamId streamId, int expectedVersion,
			@NotNull EventData... events) throws StreamNotFoundException,
			StreamVersionConflictException, StreamDeletedException;

	/**
	 * Reads a single event from a stream.
	 * 
	 * @param streamId
	 *            The stream to read from
	 * @param eventNumber
	 *            The event number to read.
	 * 
	 * @return A result containing the results of the read operation
	 * 
	 * @throws EventNotFoundException
	 *             An event with the given number was not found in the stream.
	 * @throws StreamNotFoundException
	 *             A stream with the given name does not exist in the
	 *             repository.
	 * @throws StreamDeletedException
	 *             A stream with the given name previously existed but was
	 *             deleted.
	 */
	public EventData readEvent(@NotNull StreamId streamId, int eventNumber)
			throws EventNotFoundException, StreamNotFoundException,
			StreamDeletedException;

	/**
	 * Reads count Events from an Event Stream forwards (e.g. oldest to newest)
	 * starting from position start
	 * 
	 * @param streamId
	 *            The stream to read from
	 * @param start
	 *            The starting point to read from
	 * @param count
	 *            The count of items to read
	 * 
	 * @return A slice containing the results of the read operation
	 * 
	 * @throws StreamNotFoundException
	 *             A stream with the given name does not exist in the
	 *             repository.
	 * @throws StreamDeletedException
	 *             A stream with the given name previously existed but was
	 *             deleted.
	 */
	public StreamEventsSlice readStreamEventsForward(
			@NotNull StreamId streamId, int start, int count)
			throws StreamNotFoundException, StreamDeletedException;

	/**
	 * Reads count events from an Event Stream backwards (e.g. newest to oldest)
	 * from position
	 * 
	 * @param streamId
	 *            The Event Stream to read from
	 * @param start
	 *            The position to start reading from
	 * @param count
	 *            The count to read from the position
	 * 
	 * @return An slice containing the results of the read operation
	 * 
	 * @throws StreamNotFoundException
	 *             A stream with the given name does not exist in the
	 *             repository.
	 * @throws StreamDeletedException
	 *             A stream with the given name previously existed but was
	 *             deleted.
	 */
	public StreamEventsSlice readStreamEventsBackward(
			@NotNull StreamId streamId, int start, int count)
			throws StreamNotFoundException, StreamDeletedException;

}
//CHECKSTYLE:ON:RedundantThrows