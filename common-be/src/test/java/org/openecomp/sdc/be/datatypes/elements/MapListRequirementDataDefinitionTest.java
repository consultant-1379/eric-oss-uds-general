/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
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
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.be.datatypes.elements;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MapListRequirementDataDefinitionTest {

	private MapListRequirementDataDefinition createTestSubject() {
		Map map = new HashMap<>();
		return new MapListRequirementDataDefinition(map);
	}

	@Test
	public void testAdd() throws Exception {
		MapListRequirementDataDefinition testSubject = new MapListRequirementDataDefinition();
		RequirementDataDefinition value = null;

		testSubject.add("key1", value);
		testSubject.add("key2", value);
		testSubject.add("key2", value);
		Map<String, ListRequirementDataDefinition> result = testSubject.getMapToscaDataDefinition();
		assertEquals(2, result.size());
		assertEquals(2, result.get("key2").getListToscaDataDefinition().size());
	}
}
