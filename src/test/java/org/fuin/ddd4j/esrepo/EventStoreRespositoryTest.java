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
package org.fuin.ddd4j.esrepo;

import static org.fest.assertions.Assertions.assertThat;

import org.fuin.ddd4j.common.JPATest;
import org.fuin.ddd4j.ddd.SimpleDeserializerRegistry;
import org.fuin.ddd4j.ddd.XmlDeSerializer;
import org.fuin.ddd4j.eventstore.intf.StreamEventsSlice;
import org.fuin.ddd4j.eventstore.intf.StreamId;
import org.fuin.ddd4j.eventstore.jpa.JpaEventStore;
import org.fuin.ddd4j.eventstore.jpa.Stream;
import org.fuin.ddd4j.eventstore.jpa.VendorStream;
import org.fuin.ddd4j.test.Vendor;
import org.fuin.ddd4j.test.VendorCreatedEvent;
import org.fuin.ddd4j.test.VendorId;
import org.fuin.ddd4j.test.VendorKey;
import org.fuin.ddd4j.test.VendorName;
import org.junit.Test;

//CHECKSTYLE:OFF
public class EventStoreRespositoryTest extends JPATest {

	@Test
	public void testCreateAggregate() throws Exception {

		// PREPARE
		final JpaEventStore eventStore = new JpaEventStore(getEm(),
				new JpaEventStore.StreamFactory() {
					@Override
					public Stream create(final StreamId streamId) {
						final VendorId vendorId = streamId
								.getSingleParamValue();
						return new VendorStream(vendorId);
					}
				});
		final XmlDeSerializer vcds = new XmlDeSerializer(
				VendorCreatedEvent.TYPE.asBaseType(), VendorCreatedEvent.class);
		final SimpleDeserializerRegistry registry = new SimpleDeserializerRegistry();
		registry.add(vcds.getType(), vcds.getVersion(), vcds.getMimeType(),
				vcds.getEncoding(), vcds);
		final VendorRepository repo = new VendorRepository(eventStore, vcds,
				registry);

		final VendorId vendorId = new VendorId();
		final VendorKey vendorKey = new VendorKey("V00001");
		final VendorName vendorName = new VendorName(
				"Hazards International Inc.");
		final Vendor vendor = new Vendor(vendorId, vendorKey, vendorName);

		// TEST
		beginTransaction();
		repo.update(vendor, null);
		commitTransaction();

		// VERIFY
		beginTransaction();
		final AggregateStreamId<VendorId> streamId = new AggregateStreamId<VendorId>(
				VendorId.ENTITY_TYPE, "vendorId", vendorId);
		final StreamEventsSlice slice = eventStore.readStreamEventsForward(
				streamId, 1, 1);
		commitTransaction();
		assertThat(slice.getEvents()).hasSize(1);

	}

}
// CHECKSTYLE:ON
