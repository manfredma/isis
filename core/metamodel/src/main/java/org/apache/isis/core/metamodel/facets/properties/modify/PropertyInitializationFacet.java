/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.isis.core.metamodel.facets.properties.modify;

import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.propcoll.access.PropertyOrCollectionAccessorFacet;

/**
 * The mechanism by which the value of the property can be initialised.
 * 
 * <p>
 * This differs from the {@link PropertySetterFacet} in that it is only called when object is set up (after persistence)
 * and not every time a property changes; hence it will not be made part of a transaction.
 * 
 * <p>
 * In the standard Apache Isis Programming Model, corresponds to invoking the mutator method for a property.
 * 
 * @see PropertyOrCollectionAccessorFacet
 * @see PropertySetterFacet
 * @see PropertyClearFacet
 */
public interface PropertyInitializationFacet extends Facet {

    /**
     * Sets the value of this property.
     */
    public void initProperty(ObjectAdapter inObject, ObjectAdapter value);
}
