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
package org.fuin.ddd4j.eventstore.memory;

import static org.fest.assertions.Assertions.assertThat;

import java.nio.charset.Charset;
import java.util.UUID;

import org.fuin.ddd4j.esrepo.AggregateStreamId;
import org.fuin.ddd4j.eventstore.intf.Data;
import org.fuin.ddd4j.eventstore.intf.EventData;
import org.fuin.ddd4j.eventstore.intf.StreamDeletedException;
import org.fuin.ddd4j.eventstore.intf.StreamEventsSlice;
import org.fuin.ddd4j.eventstore.intf.StreamNotFoundException;
import org.fuin.ddd4j.eventstore.intf.StreamVersionConflictException;
import org.fuin.ddd4j.test.VendorId;
import org.joda.time.DateTime;
import org.junit.Test;

// CHECKSTYLE:OFF
public class InMemoryEventStoreTest {

    @Test
    public void testAppendSingleSuccess() throws 
            StreamNotFoundException, StreamDeletedException,
            StreamVersionConflictException {

        // PREPARE
        final InMemoryEventStore testee = new InMemoryEventStore();
        testee.open();
        try {
            final VendorId vendorId = new VendorId();
            final String xml = "<vendor-created-event id=\"" + vendorId + "\"/>";
            final AggregateStreamId streamId = new AggregateStreamId(VendorId.ENTITY_TYPE, "vendorId", vendorId);
            final String eventId = UUID.randomUUID().toString();
            final DateTime timestamp = new DateTime();
            final Charset charset = Charset.forName("utf-8");
            final Data data = new Data("VendorCreatedEvent", 1, "application/xml", charset, xml.getBytes(charset));
            final Data meta = null;
            final EventData eventData = new EventData(eventId, timestamp, data, meta);

            // TEST
            final int version = testee.appendToStream(streamId, 0, eventData);

            // VERIFY
            assertThat(version).isEqualTo(1);
            final StreamEventsSlice slice = testee.readStreamEventsForward(streamId, 1, 2);
            assertThat(slice.getFromEventNumber()).isEqualTo(1);
            assertThat(slice.getNextEventNumber()).isEqualTo(2);
            assertThat(slice.isEndOfStream()).isTrue();
            assertThat(slice.getEvents()).hasSize(1);
            final EventData ed = slice.getEvents().get(0);
            assertThat(ed.getEventId()).isEqualTo(eventId);

        } finally {
            testee.close();
        }

    }
    
}
