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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.validation.constraints.NotNull;

import org.fuin.ddd4j.eventstore.intf.EventData;
import org.fuin.ddd4j.eventstore.intf.EventNotFoundException;
import org.fuin.ddd4j.eventstore.intf.EventStore;
import org.fuin.ddd4j.eventstore.intf.StreamDeletedException;
import org.fuin.ddd4j.eventstore.intf.StreamEventsSlice;
import org.fuin.ddd4j.eventstore.intf.StreamId;
import org.fuin.ddd4j.eventstore.intf.StreamNotFoundException;
import org.fuin.ddd4j.eventstore.intf.StreamVersionConflictException;
import org.fuin.objects4j.common.Contract;
import org.fuin.objects4j.vo.KeyValue;

/**
 * JPA Implementation of the event store.
 */
public final class JpaEventStore implements EventStore {

    private EntityManager em;

    private StreamFactory streamFactory;

    /**
     * Constructor with all mandatory data.
     * 
     * @param em
     *            Entity manager.
     * @param streamFactory
     *            Stream factory.
     */
    public JpaEventStore(@NotNull final EntityManager em,
	    @NotNull final StreamFactory streamFactory) {
	super();
	Contract.requireArgNotNull("em", em);
	Contract.requireArgNotNull("streamFactory", streamFactory);
	this.em = em;
	this.streamFactory = streamFactory;
    }

    @Override
    public void open() {
	// Do nothing
    }

    @Override
    public void close() {
	// Do nothing
    }

    @Override
    public final int appendToStream(final StreamId streamId,
	    final int expectedVersion, final EventData... events)
	    throws StreamVersionConflictException {
	return appendToStream(streamId, expectedVersion, Arrays.asList(events));
    }

    @Override
    public final int appendToStream(final StreamId streamId,
	    final int expectedVersion, final List<EventData> events)
	    throws StreamVersionConflictException {

	if (streamId.isProjection()) {
	    throw new IllegalArgumentException("Projections are read only: "
		    + streamId);
	}

	final String sql = createStreamSelect(streamId);
	final TypedQuery<Stream> query = em.createQuery(sql, Stream.class);
	setParameters(query, streamId);
	query.setLockMode(LockModeType.PESSIMISTIC_WRITE);
	final List<Stream> streams = query.getResultList();
	final Stream stream;
	if (streams.size() == 0) {
	    stream = streamFactory.create(streamId);
	    em.persist(stream);
	} else {
	    stream = streams.get(0);
	    if (stream.isDeleted()) {
		throw new StreamDeletedException(streamId);
	    }
	    if (stream.getVersion() != expectedVersion) {
		throw new StreamVersionConflictException(streamId,
			expectedVersion, stream.getVersion());
	    }
	}
	for (int i = 0; i < events.size(); i++) {
	    final EventEntry eventEntry = asEventEntry(events.get(i));
	    em.persist(eventEntry);
	    final StreamEvent streamEvent = stream.createEvent(eventEntry);
	    em.persist(streamEvent);
	}
	return stream.getVersion();

    }

    @Override
    public final EventData readEvent(final StreamId streamId,
	    final int eventNumber) throws EventNotFoundException {

	final StringBuilder sb = new StringBuilder(createEventSelect(streamId));
	if (streamId.getParameters().size() == 0) {
	    sb.append(" WHERE ");
	} else {
	    sb.append(" AND ");
	}
	sb.append("t.eventNumber=:eventNumber");

	final TypedQuery<StreamEvent> query = em.createQuery(sb.toString(),
		StreamEvent.class);
	setParameters(query, streamId);

	try {
	    final StreamEvent result = query.getSingleResult();
	    return asEventData(result.getEventEntry());
	} catch (final NoResultException ex) {
	    throw new EventNotFoundException(streamId, eventNumber);
	}
    }

    @Override
    public final StreamEventsSlice readStreamEventsForward(
	    final StreamId streamId, final int start, final int count) {

	return readStreamEvents(streamId, start, count, true);

    }

    @Override
    public final StreamEventsSlice readStreamEventsBackward(
	    final StreamId streamId, final int start, final int count) {

	return readStreamEvents(streamId, start, count, false);

    }

    private StreamEventsSlice readStreamEvents(final StreamId streamId,
	    final int start, final int count, final boolean forward) {

	if (streamId.isProjection() && !projectionExists(streamId)) {
	    // The projection does not exist at all or it's currently
	    // being created, but not yet available
	    return new StreamEventsSlice(start, new ArrayList<EventData>(),
		    start, true);
	}

	// Prepare SQL
	final String sql = createEventSelect(streamId)
		+ createOrderBy(streamId, forward);
	final TypedQuery<StreamEvent> query = em.createQuery(sql,
		StreamEvent.class);
	setParameters(query, streamId);
	query.setFirstResult(start - 1);
	query.setMaxResults(count);

	// Execute query
	final List<StreamEvent> resultList = query.getResultList();

	// Return result
	final List<EventData> events = asEventData(resultList);
	final int fromEventNumber = start;
	final int nextEventNumber = (start + events.size());
	final boolean endOfStream = (events.size() < count);

	return new StreamEventsSlice(fromEventNumber, events, nextEventNumber,
		endOfStream);
    }

    private boolean projectionExists(final StreamId streamId) {
	final TypedQuery<Projection> query = em.createQuery(
		"select p from Projection p where p.name=:name",
		Projection.class);
	query.setParameter("name", streamId.asString());
	final List<Projection> resultList = query.getResultList();
	return !resultList.isEmpty();
    }

    private String createOrderBy(final StreamId streamId, final boolean asc) {
	final StringBuilder sb = new StringBuilder(" ORDER BY ");
	final List<KeyValue> params = streamId.getParameters();
	if (params.size() > 0) {
	    for (int i = 0; i < params.size(); i++) {
		final KeyValue param = params.get(i);
		sb.append("t." + param.getKey());
		sb.append(", ");
	    }
	}
	sb.append("t.eventEntry.id");
	if (asc) {
	    sb.append(" ASC");
	} else {
	    sb.append(" DESC");
	}
	return sb.toString();
    }

    @Override
    public final void deleteStream(final StreamId streamId,
	    final int expectedVersion) throws StreamVersionConflictException {

	final Stream stream = em.find(Stream.class, streamId.getName(),
		LockModeType.PESSIMISTIC_WRITE);
	if (stream == null) {
	    throw new StreamNotFoundException(streamId);
	}
	if (stream.isDeleted()) {
	    throw new StreamDeletedException(streamId);
	}
	if (stream.getVersion() != expectedVersion) {
	    throw new StreamVersionConflictException(streamId, expectedVersion,
		    stream.getVersion());
	}
	stream.delete();

    }

    private String createStreamSelect(final StreamId streamId) {
	final List<KeyValue> params = streamId.getParameters();
	final StringBuilder sb = new StringBuilder("SELECT t FROM "
		+ streamId.getName() + "Stream t");
	if (params.size() > 0) {
	    sb.append(" WHERE ");
	    for (int i = 0; i < params.size(); i++) {
		final KeyValue param = params.get(i);
		if (i > 0) {
		    sb.append(" AND ");
		}
		sb.append("t." + param.getKey() + "=:" + param.getKey());
	    }
	}
	return sb.toString();
    }

    private String createEventSelect(final StreamId streamId) {
	final List<KeyValue> params = streamId.getParameters();
	final StringBuilder sb = new StringBuilder("SELECT t FROM "
		+ streamId.getName() + "Event t");
	if (params.size() > 0) {
	    sb.append(" WHERE ");
	    for (int i = 0; i < params.size(); i++) {
		final KeyValue param = params.get(i);
		if (i > 0) {
		    sb.append(" AND ");
		}
		sb.append("t." + param.getKey() + "=:" + param.getKey());
	    }
	}
	return sb.toString();
    }

    private void setParameters(final Query query, final StreamId streamId) {
	final List<KeyValue> params = streamId.getParameters();
	if (params.size() > 0) {
	    for (int i = 0; i < params.size(); i++) {
		final KeyValue param = params.get(i);
		query.setParameter(param.getKey(), param.getValue());
	    }
	}
    }

    private List<EventData> asEventData(final List<StreamEvent> resultList) {
	final List<EventData> events = new ArrayList<EventData>();
	for (StreamEvent result : resultList) {
	    final EventEntry eventEntry = result.getEventEntry();
	    events.add(asEventData(eventEntry));
	}
	return events;
    }

    private EventData asEventData(final EventEntry eventEntry) {
	return new EventData(eventEntry.getEventId(),
		eventEntry.getTimestamp(), eventEntry.getData(),
		eventEntry.getMeta());
    }

    private EventEntry asEventEntry(final EventData eventData) {
	return new EventEntry(eventData.getEventId(), eventData.getTimestamp(),
		eventData.getEventData(), eventData.getMetaData());
    }

    /**
     * Creates a new instance of a stream identified by a stream identifier.
     */
    public static interface StreamFactory {

	/**
	 * Creates a new instance (without persisting it).
	 * 
	 * @param streamId
	 *            Stream identifier to create an instance for.
	 * 
	 * @return New object.
	 */
	public Stream create(StreamId streamId);

    }

}
